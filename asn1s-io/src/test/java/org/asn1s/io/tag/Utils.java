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

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.input.DefaultBerReader;
import org.asn1s.io.ber.output.DefaultBerWriter;
import org.junit.Assert;

import java.io.ByteArrayInputStream;

final class Utils
{
	private Utils()
	{
	}

	static void performWriteTest( Scope scope, String message, Ref<Type> type, Value value, byte[] expected ) throws Exception
	{
		byte[] result = writeValue( scope, type, value );
		Assert.assertArrayEquals( message, expected, result );
	}

	static void performReadTest( Scope scope, String message, Ref<Type> type, Value value ) throws Exception
	{
		byte[] expected = writeValue( scope, type, value );
		Value iValue;
		try( ByteArrayInputStream bis = new ByteArrayInputStream( expected );
		     Asn1Reader is = new DefaultBerReader( bis, new CoreValueFactory() ) )
		{
			iValue = is.read( scope, type );
		}
		byte[] actual = writeValue( scope, type, iValue );
		Assert.assertArrayEquals( message, expected, actual );
	}

	private static byte[] writeValue( Scope scope, Ref<Type> type, Value value ) throws Exception
	{
		byte[] result;
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.Der ) )
		{
			writer.write( scope, type, value );
			result = writer.toByteArray();
		}
		return result;
	}
}
