////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2017. Lapinin "lastrix" Sergey.                          /
//                                                                             /
// Permission is hereby granted, free of charge, to any person                 /
// obtaining a copy of this software and associated documentation              /
// files (the "Software"), to deal in the Software without                     /
// restriction, including without limitation the rights to use,                /
// copy, modify, merge, publish, distribute, sublicense, and/or                /
// sell copies of the Software, and to permit persons to whom the              /
// Software is furnished to do so, subject to the following                    /
// conditions:                                                                 /
//                                                                             /
// The above copyright notice and this permission notice shall be              /
// included in all copies or substantial portions of the Software.             /
//                                                                             /
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,             /
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES             /
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                    /
// NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT                /
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,                /
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING                /
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE                  /
// OR OTHER DEALINGS IN THE SOFTWARE.                                          /
////////////////////////////////////////////////////////////////////////////////

package org.asn1s.io.ber.input;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.*;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

abstract class AbstractBerReader implements BerReader
{
	private static final Map<Family, BerDecoder> DECODERS = new EnumMap<>( Family.class );

	static
	{
		DECODERS.put( Family.Boolean, new BooleanBerDecoder() );
		DECODERS.put( Family.Integer, new IntegerBerDecoder() );
		DECODERS.put( Family.Enumerated, new EnumeratedBerDecoder() );
		DECODERS.put( Family.Real, new RealBerDecoder() );
		DECODERS.put( Family.BitString, new BitStringBerDecoder() );
		DECODERS.put( Family.OctetString, new OctetStringBerDecoder() );
		DECODERS.put( Family.Null, new NullBerDecoder() );
		DECODERS.put( Family.Sequence, new SequenceBerDecoder() );
		DECODERS.put( Family.SequenceOf, new SequenceOfBerDecoder() );
		DECODERS.put( Family.SetOf, new SetOfBerDecoder() );
		DECODERS.put( Family.Set, new SetBerDecoder() );
		DECODERS.put( Family.RestrictedString, new StringBerDecoder() );
		DECODERS.put( Family.UTCTime, new UTCTimeBerDecoder() );
		DECODERS.put( Family.GeneralizedTime, new GeneralizedTimeBerDecoder() );
		DECODERS.put( Family.Oid, new ObjectIDBerDecoder() );
	}

	AbstractBerReader( ValueFactory factory )
	{
		this.factory = factory;
	}

	private final ValueFactory factory;

	@NotNull
	@Override
	public ValueFactory getValueFactory()
	{
		return factory;
	}

	@Override
	public Value read( @NotNull Scope scope, @NotNull Ref<Type> typeRef ) throws IOException, Asn1Exception
	{
		Type type = typeRef.resolve( scope );
		Value value = readInternal( scope, type, null, -1, false );
		type.accept( scope, value );
		return value;
	}

	@Override
	@NotNull
	public Value readInternal( @NotNull Scope scope, @NotNull Type type, @Nullable Tag tag, int length, boolean implicit ) throws IOException, Asn1Exception
	{
		if( type.hasConstraint() )
			return readConstrainedType( scope, type, tag, length, implicit );

		if( type.isTagged() && ( (TaggedType)type ).getInstructions() == EncodingInstructions.Tag )
			return readTaggedType( scope, type, tag, length, implicit );

		if( type.getSibling() != null )
			return readInternal( type.getSibling().getScope( scope ), type.getSibling(), tag, length, implicit );

		if( type.getFamily() == Family.Choice )
			return readChoiceType( scope, (CollectionType)type, tag, length, implicit );

		if( type.getFamily() == Family.OpenType )
			return readOpenType( scope, length );

		if( tag == null )
		{
			tag = readTag();
			assertTag( type, tag );

			length = readLength();
		}

		return DECODERS.get( type.getFamily() ).decode( this, scope, type, tag, length );
	}

	private Value readOpenType( @NotNull Scope scope, int length ) throws Asn1Exception, IOException
	{
		InstanceOfTypeSelector selector = scope.getScopeOption( InstanceOfTypeSelector.KEY );
		if( selector == null )
			throw new ResolutionException( "Unable to locate InstanceOfTypeSelector." );

		Type openTypeType = selector.resolveInstanceOfType( scope );
		Value openTypeValue = readInternal( openTypeType.getScope( scope ), openTypeType, null, -1, false );
		if( length == -1 )
			skipToEoc();
		return getValueFactory().openTypeValue( openTypeType, openTypeValue ).resolve( scope );
	}

	@SuppressWarnings( "NumericCastThatLosesPrecision" )
	@Override
	public Tag readTag() throws IOException
	{
		byte value = read();
		TagClass tagClass = TagClass.findByCode( (byte)( value & BerUtils.CLASS_MASK ) );
		boolean constructed = ( value & BerUtils.PC_MASK ) != 0;
		int tag = value & BerUtils.TAG_MASK;

		if( tag == BerUtils.TAG_MASK )
		{
			tag = 0;
			do
			{
				value = read();
				if( value == -1 )
					throw new IOException( "Unexpected EOF" );
				tag = ( tag << 7 ) | ( value & BerUtils.UNSIGNED_BYTE_MASK );
			} while( ( value & BerUtils.BYTE_SIGN_MASK ) != 0 );
		}

		return new Tag( tagClass, constructed, tag );
	}

	@Override
	public int readLength() throws IOException
	{
		byte value = read();
		if( value == BerUtils.FORM_INDEFINITE )
			return -1;

		if( ( value & BerUtils.BYTE_SIGN_MASK ) == 0 )
			return value & BerUtils.UNSIGNED_BYTE_MASK;

		int result = 0;
		int count = value & BerUtils.UNSIGNED_BYTE_MASK;
		for( int i = 0; i < count; i++ )
			result = ( result << 8 ) | ( read() & BerUtils.BYTE_MASK );

		return result;
	}

	private Value readConstrainedType( Scope scope, @NotNull Type type, @Nullable Tag tag, int length, boolean implicit ) throws IOException, Asn1Exception
	{
		if( type.getSibling() == null )
			throw new IOException( "Constrained type must have non null sibling type" );

		scope = type.getSibling().getScope( scope );
		return readInternal( scope, type.getSibling(), tag, length, implicit );
	}

	private Value readTaggedType( Scope scope, @NotNull Type type, @Nullable Tag tag, int length, boolean implicit ) throws IOException, Asn1Exception
	{
		TagEncoding encoding = (TagEncoding)type.getEncoding( EncodingInstructions.Tag );
		if( encoding == null )
			throw new IllegalStateException();

		Type baseType = type.getSibling();
		if( baseType == null )
			throw new IllegalStateException();

		scope = baseType.getScope( scope );
		if( implicit )
		{
			if( encoding.getTagMethod() == TagMethod.Implicit )
				return readInternal( scope, baseType, tag, length, true );

			return readInternal( scope, baseType, null, -1, false );
		}

		implicit = encoding.getTagMethod() == TagMethod.Implicit;
		if( tag != null )
		{
			if( baseType.getFamily() == Family.Choice )
				return readInternal( scope, baseType, null, -1, implicit );
			return readInternal( scope, baseType, tag, length, implicit );
		}


		tag = readTag();
		assertTag( type, tag );
		length = readLength();
		// if constructed then enclosed type has it's own tag
		if( tag.isConstructed() )
		{
			if( !implicit )
				return readInternal( scope, baseType, null, -1, false );

			return readInternal( scope, baseType, tag, length, true );
		}

		return readInternal( scope, baseType, tag, length, implicit );
	}

	@NotNull
	private Value readChoiceType( Scope scope, @NotNull CollectionType type, @Nullable Tag tag, int length, boolean implicit ) throws IOException, Asn1Exception
	{
		if( tag == null )
		{
			tag = readTag();
			length = readLength();
		}

		for( ComponentType component : type.getComponents( true ) )
		{
			TagEncoding encoding = (TagEncoding)component.getEncoding( EncodingInstructions.Tag );
			if( encoding.getTagClass() == tag.getTagClass() && encoding.getTagNumber() == tag.getTagNumber() )
			{
				scope = component.getScope( scope );
				Value value = readInternal( component.getScope( scope ), component, tag, length, implicit );
				NamedValue named = factory.named( component.getName(), value );
				scope.setValueLevel( named );
				return named;
			}
		}

		throw new IOException( "Unable to read choice value, unexpected tag: " + tag );
	}

	private static void assertTag( @NotNull Type type, @Nullable Tag tag ) throws IOException
	{
		if( tag == null )
			throw new IllegalStateException();

		TagEncoding encoding = (TagEncoding)type.getEncoding( EncodingInstructions.Tag );
		if( encoding == null )
			throw new IllegalStateException( "No encoding for type: " + type );

		if( tag.getTagClass() != encoding.getTagClass() || tag.getTagNumber() != encoding.getTagNumber() )
			throw new IOException( "Invalid tag: " + tag );
	}
}
