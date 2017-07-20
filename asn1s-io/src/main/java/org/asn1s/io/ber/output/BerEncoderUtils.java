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

import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.io.ber.BerUtils;

import java.io.IOException;

final class BerEncoderUtils
{
	private BerEncoderUtils()
	{
	}

	static void writeString( AbstractBerWriter os, byte[] content, Tag tag, boolean writeHeader ) throws IOException
	{
		if( writeHeader )
			os.writeHeader( tag, content.length );
		os.write( content );
	}

	static void writeTagNumber( AbstractBerWriter writer, long tagNumber ) throws IOException
	{
		boolean skipping = true;
		for( int i = 8; i >= 0; i-- )
		{
			//noinspection NumericCastThatLosesPrecision
			byte current = (byte)( ( i > 0 ? ( tagNumber & ~0x1 ) >> i * 7 : tagNumber ) & BerUtils.UNSIGNED_BYTE_MASK );
			if( skipping && current == 0 )
				continue;
			skipping = false;

			if( i > 0 )
				current |= BerUtils.BYTE_SIGN_MASK;
			writer.write( current );
		}
	}
}
