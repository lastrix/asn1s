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

package org.asn1s.io.tag;

@SuppressWarnings( {"UtilityClassCanBeEnum", "UtilityClass"} )
final class TestUtils
{

	private static final int BYTES_FOR_NEWLINE = 16;
	private static final int BYTES_FOR_TAB = 4;

	private TestUtils()
	{
	}

	public static String toHexString( byte[] bytes )
	{
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for( byte item : bytes )
		{
			if( count != 0 )
			{
				if( count % BYTES_FOR_NEWLINE == 0 )
					sb.append( System.lineSeparator() );
				else if( count % BYTES_FOR_TAB == 0 )
					sb.append( '\t' );
				else
					sb.append( ' ' );
			}
			count++;
			//noinspection MagicNumber
			String str = Integer.toHexString( item & 0xFF ).toUpperCase();
			if( str.length() == 1 )
				sb.append( '0' );
			sb.append( str );
		}
		return sb.toString();
	}
}
