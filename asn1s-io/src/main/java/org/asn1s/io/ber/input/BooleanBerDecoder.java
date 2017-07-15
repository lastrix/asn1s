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
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

final class BooleanBerDecoder implements BerDecoder
{
	@Override
	public Value decode( @NotNull BerReader is, @NotNull Scope scope, @NotNull Type type, @NotNull Tag tag, int length ) throws IOException
	{
		if( length != 1 )
			throw new IOException( "Boolean length must be 1, got: " + length );

		byte content = is.read();
		if( content == BerUtils.BOOLEAN_TRUE )
			return BooleanValue.TRUE;

		if( content == BerUtils.BOOLEAN_FALSE )
			return BooleanValue.FALSE;

		throw new IOException( "Unable to convert to boolean: " + Integer.toHexString( content ) );
	}
}
