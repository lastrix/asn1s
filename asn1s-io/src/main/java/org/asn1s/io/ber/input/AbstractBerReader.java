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
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

abstract class AbstractBerReader implements Asn1Reader
{
	private static final Map<Family, BerDecoder> DECODERS = new EnumMap<>( Family.class );

	static
	{
		DECODERS.put( Family.BOOLEAN, new BooleanBerDecoder() );
		DECODERS.put( Family.INTEGER, new IntegerBerDecoder() );
		DECODERS.put( Family.ENUMERATED, new EnumeratedBerDecoder() );
		DECODERS.put( Family.REAL, new RealBerDecoder() );
		DECODERS.put( Family.BIT_STRING, new BitStringBerDecoder() );
		DECODERS.put( Family.OCTET_STRING, new OctetStringBerDecoder() );
		DECODERS.put( Family.NULL, new NullBerDecoder() );
		DECODERS.put( Family.SEQUENCE, new SequenceBerDecoder() );
		DECODERS.put( Family.SEQUENCE_OF, new SequenceOfBerDecoder() );
		DECODERS.put( Family.SET_OF, new SetOfBerDecoder() );
		DECODERS.put( Family.SET, new SetBerDecoder() );
		DECODERS.put( Family.RESTRICTED_STRING, new StringBerDecoder() );
		DECODERS.put( Family.UTC_TIME, new UTCTimeBerDecoder() );
		DECODERS.put( Family.GENERALIZED_TIME, new GeneralizedTimeBerDecoder() );
		DECODERS.put( Family.OID, new ObjectIDBerDecoder() );
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
		Value value = readInternal( new ReaderContext( this, scope, type, null, -1, false ) );
		type.accept( scope, value );
		return value;
	}

	@NotNull
	Value readInternal( @NotNull ReaderContext context ) throws IOException, Asn1Exception
	{
		if( context.getType().hasConstraint() )
			return readInternal( context.toSiblingContext() );

		if( context.getType().isTagged() && ( (TaggedType)context.getType() ).getInstructions() == EncodingInstructions.TAG )
			return readTaggedType( context );

		if( context.getType().getSibling() != null )
			return readInternal( context.toSiblingContext() );

		if( context.getType().getFamily() == Family.CHOICE )
			return readChoiceType( context );

		if( context.getType().getFamily() == Family.OPEN_TYPE )
			return readOpenType( context );

		if( context.getTag() == null )
			context.readTagInfo( true );

		return DECODERS.get( context.getType().getFamily() ).decode( context );
	}

	private Value readOpenType( @NotNull ReaderContext context ) throws Asn1Exception, IOException
	{
		InstanceOfTypeSelector selector = context.getScope().getScopeOption( TypeUtils.INSTANCE_OF_TYPE_KEY );
		if( selector == null )
			throw new ResolutionException( "Unable to locate InstanceOfTypeSelector." );

		int contextLength = context.getLength();
		Type openTypeType = selector.resolveInstanceOfType( context.getScope() );
		context.resetTagInfo( false );
		Value openTypeValue = readInternal( context.toSiblingContext( openTypeType ) );
		if( contextLength == -1 )
			skipToEoc();
		return getValueFactory().openTypeValue( openTypeType, openTypeValue ).resolve( context.getScope() );
	}

	@SuppressWarnings( "NumericCastThatLosesPrecision" )
	Tag readTag() throws IOException
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

	int readLength() throws IOException
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

	private Value readTaggedType( @NotNull ReaderContext context ) throws IOException, Asn1Exception
	{
		TagEncoding encoding = (TagEncoding)context.getType().getEncoding( EncodingInstructions.TAG );
		if( encoding == null )
			throw new IllegalStateException();

		Type baseType = context.getType().getSibling();
		if( baseType == null )
			throw new IllegalStateException();

		if( context.isImplicit() )
		{
			if( encoding.getTagMethod() == TagMethod.IMPLICIT )
				return readInternal( context.toSiblingContext() );

			context.resetTagInfo( false );
			return readInternal( context.toSiblingContext() );
		}

		context.setImplicit( encoding.getTagMethod() == TagMethod.IMPLICIT );
		if( context.hasTag() )
		{
			if( baseType.getFamily() != Family.CHOICE )
				return readInternal( context.toSiblingContext() );

			context.resetTagInfo( context.isImplicit() );
			return readInternal( context.toSiblingContext() );
		}

		context.readTagInfo( true );
		// if constructed then enclosed type has it's own tag
		if( context.getTag().isConstructed() && !context.isImplicit() )
		{
			context.resetTagInfo( false );
			return readInternal( context.toSiblingContext() );
		}

		return readInternal( context.toSiblingContext() );
	}

	@NotNull
	private Value readChoiceType( @NotNull ReaderContext context ) throws IOException, Asn1Exception
	{
		if( !context.hasTag() )
			context.readTagInfo( false );

		CollectionType type = (CollectionType)context.getType();
		for( ComponentType component : type.getComponents( true ) )
		{
			TagEncoding encoding = (TagEncoding)component.getEncoding( EncodingInstructions.TAG );
			if( context.isSameTagEncoding( encoding ) )
			{
				context = context.toSiblingContext( component );
				Value value = readInternal( context );
				return factory.named( component.getName(), value );
			}
		}

		throw new IOException( "Unable to read choice value, unexpected tag: " + context.getTag() );
	}

	void ensureConstructedRead( int start, int length, @Nullable Tag tag ) throws IOException
	{
		int end = length == -1 ? 0 : start + length;
		int position = position();
		if( length == -1 && tag != null && !tag.isEoc() )
			skipToEoc();
		else if( length != -1 && position != end )
			skip( end - position );
	}

	protected abstract void skipToEoc() throws IOException;

	protected abstract void skip( int amount ) throws IOException;

	protected abstract int position();

	protected abstract byte read() throws IOException;

	protected abstract int read( byte[] buffer ) throws IOException;
}
