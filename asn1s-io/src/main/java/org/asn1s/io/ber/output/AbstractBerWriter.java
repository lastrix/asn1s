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

package org.asn1s.io.ber.output;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.*;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

abstract class AbstractBerWriter implements BerWriter
{
	private static final Map<Family, BerEncoder> ENCODERS = new EnumMap<>( Family.class );

	static
	{
		ENCODERS.put( Family.Boolean, new BooleanBerEncoder() );
		ENCODERS.put( Family.Integer, new IntegerBerEncoder() );
		ENCODERS.put( Family.Enumerated, new EnumeratedBerEncoder() );
		ENCODERS.put( Family.Real, new RealBerEncoder() );
		ENCODERS.put( Family.BitString, new BitStringBerEncoder() );
		ENCODERS.put( Family.OctetString, new OctetStringBerEncoder() );
		ENCODERS.put( Family.Null, new NullBerEncoder() );
		ENCODERS.put( Family.Sequence, new SequenceBerEncoder() );
		ENCODERS.put( Family.SequenceOf, new SequenceOfBerEncoder() );
		ENCODERS.put( Family.Set, new SetBerEncoder() );
		ENCODERS.put( Family.SetOf, new SetOfBerEncoder() );
		ENCODERS.put( Family.RestrictedString, new StringBerEncoder() );
		ENCODERS.put( Family.UTCTime, new UTCTimeBerEncoder() );
		ENCODERS.put( Family.GeneralizedTime, new GeneralizedTimeBerEncoder() );
		ENCODERS.put( Family.Oid, new ObjectIDBerEncoder() );
	}

	@Override
	public void write( @NotNull Scope scope, @NotNull Ref<Type> typeRef, @NotNull Value value ) throws IOException, Asn1Exception
	{
		Type type = typeRef.resolve( scope );
		// resolve value to make sure it's correct
		value = value.resolve( scope );
		type.accept( scope, value );
		writeInternal( scope, type, value, true );
	}

	@Override
	public final void writeInternal( Scope scope, Type type, Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		if( type instanceof ComponentType && !( (ComponentType)type ).isDummy() )
		{
			if( value.getKind() != Kind.Name )
				throw new IllegalValueException( "Named value expected" );

			if( !value.toNamedValue().getName().equals( ( (NamedType)type ).getName() ) )
				throw new IllegalValueException( "Named value has illegal name: " + value.toNamedValue().getName() + ". Expected: " + ( (NamedType)type ).getName() );

			//noinspection ConstantConditions
			value = value.toNamedValue().getValueRef().resolve( scope );
		}

		if( type.hasConstraint() )
			writeConstrainedType( scope, type, value, writeHeader );

		else if( type.isTagged() && ( (TaggedType)type ).getInstructions() == EncodingInstructions.Tag )
			writeTaggedType( scope, type, value, writeHeader );
		else if( type instanceof CollectionType && ( (CollectionType)type ).getKind() == CollectionType.Kind.Choice )
			writeChoiceType( scope, (CollectionType)type, value );
		else if( type.getSibling() != null )
			writeInternal( type.getSibling().getScope( scope ), type.getSibling(), value, writeHeader );
		else if( type.getFamily() == Family.OpenType )
			writeOpenType( scope, value, writeHeader );
		else
		{
			BerEncoder encoder = ENCODERS.get( type.getFamily() );
			if( encoder == null )
				throw new IllegalStateException( "No encoder found for family: " + type.getFamily() );

			encoder.encode( this, scope, type, RefUtils.toBasicValue( scope, value ), writeHeader );
		}
	}

	private void writeOpenType( Scope scope, Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		value = RefUtils.toBasicValue( scope, value );
		Type type = value.toOpenTypeValue().getType().resolve( scope );
		Value openValue = value.toOpenTypeValue().getValueRef().resolve( scope );
		writeInternal( scope, type, openValue, writeHeader );
	}

	@Override
	public void writeTag( @NotNull Tag tag ) throws IOException
	{
		writeTag( tag.getTagClass(), tag.isConstructed(), tag.getTagNumber() );
	}

	@SuppressWarnings( "NumericCastThatLosesPrecision" )
	@Override
	public void writeTag( @NotNull TagClass tagClass, boolean constructed, long tagNumber ) throws IOException
	{
		if( tagNumber < BerUtils.TAG_MASK )
			write( (byte)( ( tagNumber & BerUtils.TAG_MASK ) | ( constructed ? BerUtils.PC_MASK : 0 ) | tagClass.getCode() ) );
		else
		{
			write( (byte)( BerUtils.TAG_MASK | ( constructed ? BerUtils.PC_MASK : 0 ) | tagClass.getCode() ) );
			BerEncoderUtils.writeTagNumber( this, tagNumber );
		}
	}

	@SuppressWarnings( "NumericCastThatLosesPrecision" )
	@Override
	public void writeLength( int length ) throws IOException
	{
		if( length < -1 )
			throw new IllegalArgumentException( "Negative value" );

		if( length == -1 )
			write( BerUtils.FORM_INDEFINITE );
		else if( length <= BerUtils.UNSIGNED_BYTE_MASK )
			write( (byte)( length & BerUtils.UNSIGNED_BYTE_MASK ) );
		else
		{
			//as 8.1.3.5 in X.690-0207 says in 1st content octet bit 8 should be 1 and 7 to 1 should encode amount of bytes,
			// the others - length as unsigned integer LE!
			int size = getLengthPackedSize( length );
			byte totalSizeByte = (byte)( BerUtils.BYTE_SIGN_MASK | ( size & BerUtils.UNSIGNED_BYTE_MASK ) );
			if( totalSizeByte == BerUtils.ILLEGAL_LENGTH_BYTE )
				throw new IllegalStateException( "Unable to write length as FF byte - prohibited." );

			write( totalSizeByte );

			boolean skipping = true;
			for( int i = size - 1; i >= 0; i-- )
			{
				byte current = (byte)( ( length >> ( i * 8 ) ) & BerUtils.BYTE_MASK );
				if( skipping && current == 0 )
					continue;
				skipping = false;
				write( current );
			}
		}
	}

	@SuppressWarnings( "NumericCastThatLosesPrecision" )
	private static int getLengthPackedSize( int length )
	{
		for( int i = 3; i >= 0; i-- )
			if( (byte)( ( length >> ( i * 8 ) ) & BerUtils.BYTE_MASK ) != 0 )
				return i + 1;

		return 0;
	}

	private void writeConstrainedType( Scope scope, Type type, Value value, boolean writeHeader ) throws Asn1Exception, IOException
	{
		if( type.getSibling() == null )
			throw new IllegalStateException();

		writeInternal( type.getSibling().getScope( scope ), type.getSibling(), value, writeHeader );
	}

	private void writeTaggedType( @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		TagEncoding encoding = (TagEncoding)type.getEncoding( EncodingInstructions.Tag );
		if( encoding == null )
			throw new IOException( "No encoding for tagged type defined" );

		Type baseType = type.getSibling();
		if( baseType == null )
			throw new IllegalStateException();

		scope = baseType.getScope( scope );
		boolean constructed = encoding.getTagMethod() != TagMethod.Implicit || type.isConstructedValue( scope, value );
		Tag tag = new Tag( encoding.getTagClass(), constructed, encoding.getTagNumber() );
		if( !writeHeader )
			writeInternal( scope, baseType, value, encoding.getTagMethod() == TagMethod.Explicit );
		else if( isBufferingAvailable() )
		{
			startBuffer( -1 );
			writeInternal( scope, baseType, value, encoding.getTagMethod() != TagMethod.Implicit );
			stopBuffer( tag );
		}
		else if( getRules() == BerRules.Der )
			throw new IOException( "Encoding rules requires definite length forms" );
		else
		{
			writeHeader( tag, -1 );
			writeInternal( scope, baseType, value, encoding.getTagMethod() != TagMethod.Implicit );
			write( (byte)0 );
			write( (byte)0 );
		}
	}

	private void writeChoiceType( @NotNull Scope scope, @NotNull CollectionType type, @NotNull Value value ) throws IOException, Asn1Exception
	{
		value = value.resolve( scope );
		if( value.getKind() != Kind.Name )
			throw new IllegalValueException( "Named value expected for choice type" );

		NamedValue namedValue = value.toNamedValue();
		if( namedValue.getValueRef() == null )
			throw new IllegalStateException();

		ComponentType componentType = type.getComponent( namedValue.getName(), true );
		if( componentType == null )
			throw new ResolutionException( "Unknown component: " + namedValue.getName() );

		scope.setValueLevel( namedValue );
		scope = componentType.getScope( scope );

		writeInternal( scope, componentType, namedValue, true );
	}
}
