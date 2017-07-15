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

package org.asn1s.io.ber;

@SuppressWarnings( "NumericCastThatLosesPrecision" )
public final class BerUtils
{
	public static final int BYTE_MASK = 0x00FF;
	public static final byte BYTE_MASK_B = (byte)0xFF;
	public static final int BYTE_SIGN_MASK = 0x0080;
	public static final byte BYTE_SIGN_MASK_B = (byte)0x80;
	public static final int UNSIGNED_BYTE_MASK = 0x007F;

	public static final byte REAL_ISO_6093_NR1 = 0x01;
	public static final byte REAL_ISO_6093_NR2 = 0x02;
	public static final byte REAL_ISO_6093_NR3 = 0x03;
	public static final byte REAL_POSITIVE_INF = 0x40;
	public static final byte REAL_NEGATIVE_INF = 0x41;
	public static final byte REAL_NAN = 0x42;
	public static final byte REAL_MINUS_ZERO = 0x43;
	public static final byte REAL_BASE_MASK = 0x30;
	public static final byte REAL_SIGN_MASK = 0x40;
	public static final byte REAL_SCALING_FACTOR_MASK = 0x0C;
	public static final byte REAL_BINARY_FLAG = (byte)0x80;

	public static final byte BOOLEAN_TRUE = (byte)0xFF;
	public static final byte BOOLEAN_FALSE = 0x00;

	public static final byte ILLEGAL_LENGTH_BYTE = (byte)0xFF;
	public static final byte FORM_INDEFINITE = (byte)0x80;
	public static final int PC_MASK = 0x20;
	public static final int CLASS_MASK = 0xC0;
	public static final int TAG_MASK = 0x1F;
	public static final int OID_FIRST_BYTE_MULTIPLIER = 40;

	private BerUtils()
	{
	}

}
