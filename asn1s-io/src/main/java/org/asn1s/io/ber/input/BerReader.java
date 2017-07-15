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
import org.asn1s.api.value.Value;
import org.asn1s.io.Asn1Reader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

interface BerReader extends Asn1Reader
{
	default void ensureConstructedRead( int start, int length, @Nullable Tag tag ) throws IOException
	{
		int end = length == -1 ? 0 : start + length;
		int position = position();
		if( length == -1 && tag != null && !tag.isEoc() )
			skipToEoc();
		else if( length != -1 && position != end )
			skip( end - position );
	}

	int position();

	void skipToEoc() throws IOException;

	void skip( int amount ) throws IOException;

	byte read() throws IOException;

	int read( byte[] buffer ) throws IOException;

	Value readInternal( @NotNull Scope scope, @NotNull Type type, @Nullable Tag tag, int length, boolean implicit ) throws IOException, Asn1Exception;

	Tag readTag() throws IOException;

	/**
	 * Read length from input stream
	 *
	 * @return length
	 * @throws IOException if IO fails
	 */
	int readLength() throws IOException;
}
