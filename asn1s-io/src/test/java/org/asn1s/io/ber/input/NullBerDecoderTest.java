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
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NullValue;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.module.CoreModule;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class NullBerDecoderTest
{
	@Test
	public void testDecode_true() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Null.ref().resolve( scope );
		Value expected = NullValue.INSTANCE;
		byte[] result = InputUtils.writeValue( scope, type, expected );
		try( ByteArrayInputStream is = new ByteArrayInputStream( result );
		     AbstractBerReader reader = new DefaultBerReader( is, new DefaultObjectFactory() ) )
		{
			Value value = reader.read( scope, type );
			Assert.assertEquals( "Values are not equal", expected, value );
		}
	}

	@Test( expected = AssertionError.class )
	public void testDecode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		try( AbstractBerReader reader = mock( DefaultBerReader.class ) )
		{
			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.Tag ) ).toTag( false );
			new NullBerDecoder().decode( new ReaderContext( reader, scope, type, tag, 0, false ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testDecode_fail_length() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Null.ref().resolve( scope );
		try( AbstractBerReader reader = mock( DefaultBerReader.class ) )
		{
			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.Tag ) ).toTag( false );
			new NullBerDecoder().decode( new ReaderContext( reader, scope, type, tag, 1, false ) );
			fail( "Must fail" );
		}
	}
}
