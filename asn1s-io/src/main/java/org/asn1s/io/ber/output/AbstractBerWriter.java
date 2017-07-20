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
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.CollectionType.Kind;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.TaggedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

abstract class AbstractBerWriter implements Asn1Writer
{
	private static final Map<Family, BerEncoder> ENCODERS = new EnumMap<>( Family.class );

	public abstract BerRules getRules();

	/**
	 * Should return true if writer supports stacked buffering
	 *
	 * @return boolean
	 */
	public abstract boolean isBufferingAvailable();

	/**
	 * Add level of buffering, allowing coders to write data and compute length after all jobs done
	 *
	 * @param sizeHint probable amount of bytes. If set to -1, then buffer size must be unlimited
	 */
	public abstract void startBuffer( int sizeHint );

	/**
	 * Write buffer data into underlying stream, write tag and length before any data copying.
	 *
	 * @param tag the data tag
	 * @throws IOException in case of I/O failure
	 */
	public abstract void stopBuffer( @NotNull Tag tag ) throws IOException;

	public abstract void write( int aByte ) throws IOException;

	public abstract void write( byte[] bytes ) throws IOException;

	public void writeHeader( Tag tag, int length ) throws IOException
	{
		writeTag( tag );
		writeLength( length );
	}

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
		writeInternal( new WriterContext( this, scope, type, value, true ) );
	}

	public final void writeInternal( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		if( context == null )
		{
			int k = 0;
		}
		context = context.narrow();
		Type type = context.getType();

		if( type.isTagged() && ( (TaggedType)type ).getInstructions() == EncodingInstructions.Tag )
			writeTaggedType( context );
		else if( type instanceof CollectionType && ( (CollectionType)type ).getKind() == Kind.Choice )
			writeChoiceType( context );
		else if( type.getSibling() != null )
			writeInternal( context.toSiblingContext() );
		else if( type.getFamily() == Family.OpenType )
			writeOpenType( context );
		else
		{
			BerEncoder encoder = ENCODERS.get( type.getFamily() );
			if( encoder == null )
				throw new IllegalStateException( "No encoder found for family: " + type.getFamily() );

			encoder.encode( context );
		}
	}

	private void writeOpenType( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		Value value = RefUtils.toBasicValue( context.getScope(), context.getValue() );
		Type type = value.toOpenTypeValue().getType().resolve( context.getScope() );
		Value openValue = value.toOpenTypeValue().getValueRef().resolve( context.getScope() );
		writeInternal( context.toSiblingContext( type, openValue, context.isWriteHeader() ) );
	}

	public void writeTag( @NotNull Tag tag ) throws IOException
	{
		writeTag( tag.getTagClass(), tag.isConstructed(), tag.getTagNumber() );
	}

	@SuppressWarnings( "NumericCastThatLosesPrecision" )
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

	private void writeTaggedType( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		TagEncoding encoding = (TagEncoding)context.getType().getEncoding( EncodingInstructions.Tag );
		if( encoding == null )
			throw new IOException( "No encoding for tagged type defined" );

		boolean constructed =
				encoding.getTagMethod() != TagMethod.Implicit
						|| context.getType().isConstructedValue( context.getScope(), context.getValue() );
		Tag tag = new Tag( encoding.getTagClass(), constructed, encoding.getTagNumber() );
		if( !context.isWriteHeader() )
			writeInternal( context.toSiblingContext( encoding.getTagMethod() == TagMethod.Explicit ) );
		else if( isBufferingAvailable() )
		{
			startBuffer( -1 );
			writeInternal( context.toSiblingContext( encoding.getTagMethod() != TagMethod.Implicit ) );
			stopBuffer( tag );
		}
		else if( getRules() == BerRules.Der )
			throw new IOException( "Encoding rules requires definite length forms" );
		else
		{
			writeHeader( tag, -1 );
			writeInternal( context.toSiblingContext( encoding.getTagMethod() != TagMethod.Implicit ) );
			write( (byte)0 );
			write( (byte)0 );
		}
	}

	private void writeChoiceType( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == Family.Choice;
		context.getScope().setValueLevel( context.getValue() );

		NamedValue namedValue = context.getValue().toNamedValue();
		ComponentType componentType = ( (CollectionType)context.getType() ).getComponent( namedValue.getName(), true );
		if( componentType == null )
			throw new ResolutionException( "Unknown component: " + namedValue.getName() );

		writeInternal( context.toComponentContext( componentType, namedValue ) );
	}
}
