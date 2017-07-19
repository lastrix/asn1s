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
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OctetStringBerDecoder implements BerDecoder
{
	@Override
	public Value decode( @NotNull BerReader is, @NotNull Scope scope, @NotNull Type type, @NotNull Tag tag, int length ) throws IOException, Asn1Exception
	{
		assert type.getFamily() == Family.OctetString;
		assert !tag.isConstructed();
		if( length == -1 )
			return readByteArrayValueIndefinite( is, 0 );
		if( length == 0 )
			return is.getValueFactory().emptyByteArray();
		return readByteArrayValue( is, length, 0 );
	}

	static Value readByteArrayValueIndefinite( BerReader is, int unusedBits ) throws IOException
	{
		byte[] bytes;
		try( ByteArrayOutputStream stream = new ByteArrayOutputStream() )
		{
			byte b1 = is.read();
			byte b2 = is.read();
			while( b1 != 0 || b2 != 0 )
			{
				stream.write( b1 );
				b1 = b2;
				b2 = is.read();
			}
			bytes = stream.toByteArray();
		}
		return is.getValueFactory().byteArrayValue( bytes.length * 8 - unusedBits, bytes );
	}

	static Value readByteArrayValue( BerReader is, int length, int unusedBits ) throws IOException
	{
		byte[] bytes = new byte[length];
		if( is.read( bytes ) != length )
			throw new IOException( "Unexpected EOF" );
		return is.getValueFactory().byteArrayValue( bytes.length * 8 - unusedBits, bytes );
	}
}
