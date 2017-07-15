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

import org.asn1s.api.Scope;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.IntegerValue;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

final class RealBerDecoder implements BerDecoder
{
	@Override
	public Value decode( @NotNull BerReader is, @NotNull Scope scope, @NotNull Type type, @NotNull Tag tag, int length ) throws IOException
	{
		if( tag.isConstructed() )
			throw new IOException( "Real must not be constructed" );

		if( length == 0 )
			return is.getValueFactory().rZero();

		byte first = is.read();
		if( first == 0 )
			return is.getValueFactory().rZero();

		if( ( first & BerUtils.BYTE_SIGN_MASK ) != 0 )
			return readBinary( is, first, length );

		switch( first )
		{
			case BerUtils.REAL_ISO_6093_NR1:
			case BerUtils.REAL_ISO_6093_NR2:
			case BerUtils.REAL_ISO_6093_NR3:
				return is.getValueFactory().real( readString( is, length - 1 ) );

			case BerUtils.REAL_NEGATIVE_INF:
				return is.getValueFactory().rNegativeInfinity();

			case BerUtils.REAL_POSITIVE_INF:
				return is.getValueFactory().rPositiveInfinity();

			case BerUtils.REAL_NAN:
				return is.getValueFactory().rNan();

			case BerUtils.REAL_MINUS_ZERO:
				return is.getValueFactory().rNegativeZero();

			default:
				throw new IllegalStateException( String.format( "Illegal real configuration byte: %02X", first ) );
		}
	}

	private static Value readBinary( BerReader is, byte first, int length ) throws IOException
	{
		int size = ( first & 0x03 ) + 1;
		if( size == 4 )
			size = is.read();

		if( length < size + 1 )
			throw new IOException( "Corrupted data" );

		IntegerValue exponentValue = IntegerBerDecoder.readInteger( is, size );
		IntegerValue mantissaValue = IntegerBerDecoder.readInteger( is, length - 1 - size );
		int exponent = exponentValue.isInt()
				? exponentValue.asInt()
				: exponentValue.asBigInteger().intValueExact();

		int base = ( first & BerUtils.REAL_BASE_MASK ) >> 4;
		switch( base )
		{
			case 1:
			case 2:
				base += 2;
			case 0:
				exponent <<= base;
				break;

			default:
				throw new IllegalStateException( String.format( "Illegal base value: %02X", first & BerUtils.REAL_BASE_MASK ) );
		}
		int scalingFactor = ( first & BerUtils.REAL_SCALING_FACTOR_MASK ) >> 2;
		exponent += scalingFactor;

		boolean negative = ( first & BerUtils.REAL_SIGN_MASK ) != 0;
		return is.getValueFactory().real( mantissaValue, false, is.getValueFactory().integer( exponent ), negative );
	}

	private static String readString( BerReader is, int length ) throws IOException
	{
		byte[] bytes = new byte[length];
		if( length != is.read( bytes ) )
			throw new IOException( "Unexpected EOF" );
		return new String( bytes, "UTF-8" ).trim();
	}
}
