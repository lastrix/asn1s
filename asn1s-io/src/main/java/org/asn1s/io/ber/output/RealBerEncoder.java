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

import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.NRxUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.IntegerValue;
import org.asn1s.api.value.x680.RealValue;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

@SuppressWarnings( "NumericCastThatLosesPrecision" )
final class RealBerEncoder implements BerEncoder
{
	static final Tag TAG = new Tag( TagClass.Universal, false, UniversalType.Real.tagNumber() );
	private static final long ZERO_DOUBLE_BITS = Double.doubleToLongBits( 0.0d );
	private static final long NEGATIVE_ZERO_DOUBLE_BITS = Double.doubleToLongBits( -0.0d );

	@Override
	public void encode( @NotNull BerWriter os, @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		assert type.getFamily() == Family.Real;
		assert value.getKind() == Kind.Real || value.getKind() == Kind.Integer;
		if( value.getKind() == Kind.Real )
			writeRealValue( os, value.toRealValue(), writeHeader );
		else
			writeIntegerValue( os, value.toIntegerValue(), writeHeader );
	}

	private static void writeRealValue( BerWriter os, RealValue realValue, boolean writeHeader ) throws IOException
	{
		if( realValue.isDouble() )
			writeDouble( os, realValue.asDouble(), writeHeader );
		else
			writeNR3( os, realValue.asBigDecimal(), writeHeader );
	}

	private static void writeIntegerValue( BerWriter os, IntegerValue integerValue, boolean writeHeader ) throws IOException
	{
		if( integerValue.isDouble() )
			writeDouble( os, integerValue.asDouble(), writeHeader );
		else
			writeNR3( os, integerValue.asBigDecimal(), writeHeader );
	}

	@SuppressWarnings( "MagicNumber" )
	private static void writeDouble( BerWriter os, double value, boolean writeHeader ) throws IOException
	{
		// encode binary value
		long bits = Double.doubleToLongBits( value );
		if( Double.isInfinite( value ) )
		{
			if( writeHeader )
				os.writeHeader( TAG, 1 );
			os.write( value < 0 ? BerUtils.REAL_NEGATIVE_INF : BerUtils.REAL_POSITIVE_INF );
		}
		else if( Double.isNaN( value ) )
		{
			if( writeHeader )
				os.writeHeader( TAG, 1 );
			os.write( BerUtils.REAL_NAN );
		}
		else if( bits == ZERO_DOUBLE_BITS )
		{
			if( writeHeader )
				os.writeHeader( TAG, 0 );
		}
		else if( bits == NEGATIVE_ZERO_DOUBLE_BITS )
		{
			if( writeHeader )
				os.writeHeader( TAG, 1 );
			os.write( BerUtils.REAL_MINUS_ZERO );
		}
		else
		{
			byte sign = ( bits & 0x8000000000000000L ) == 0 ? (byte)0 : BerUtils.REAL_SIGN_MASK;
			long mantissa = bits & 0X000FFFFFFFFFFFFFL;
			long exponent = ( bits & 0X7FF0000000000000L ) >> 52;
			if( mantissa > 0L )
			{
				mantissa |= 0x0010000000000000L;
				exponent -= 1023L + 52L;
			}
			else
				exponent -= 1022L + 52L;

			if( mantissa > 0 )
			{
				while( ( mantissa & 255L ) == 0L )
				{
					mantissa >>= 8;
					exponent += 8L;
				}

				while( ( mantissa & 1L ) == 0L )
				{
					mantissa >>= 1;
					++exponent;
				}
			}

			byte[] exponentBytes = IntegerBerEncoder.toByteArray( exponent );
			byte[] mantisBytes = IntegerBerEncoder.toByteArray( mantissa );
			byte first = (byte)( BerUtils.REAL_BINARY_FLAG | sign | Math.min( 3, exponentBytes.length - 1 ) );

			if( writeHeader )
				os.writeHeader( TAG, 1 + exponentBytes.length + mantisBytes.length );
			os.write( first );
			if( exponentBytes.length >= 4 )
				os.write( exponentBytes.length );
			os.write( exponentBytes );
			os.write( mantisBytes );
		}
	}

	private static void writeNR3( BerWriter os, BigDecimal bigDecimal, boolean writeHeader ) throws IOException
	{
		String content = NRxUtils.toCanonicalNR3( bigDecimal.toString() );
		byte[] bytes;
		try
		{
			bytes = content.getBytes( "UTF-8" );
		} catch( UnsupportedEncodingException e )
		{
			throw new IllegalStateException( e );
		}
		if( writeHeader )
			os.writeHeader( TAG, 1 + bytes.length );

		os.write( 3 );
		os.write( bytes );
	}
}
