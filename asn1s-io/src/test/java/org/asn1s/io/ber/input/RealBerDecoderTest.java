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
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.core.value.x680.RealValueBig;
import org.asn1s.core.value.x680.RealValueDouble;
import org.asn1s.core.value.x680.RealValueFloat;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class RealBerDecoderTest
{
	@Test
	public void testDecode_Bin() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Real.ref().resolve( scope );
		RealValueDouble expected = new RealValueDouble( 0.15625d );
		byte[] result = InputUtils.writeValue( scope, type, expected );
		try( ByteArrayInputStream is = new ByteArrayInputStream( result );
		     AbstractBerReader reader = new DefaultBerReader( is, new CoreValueFactory() ) )
		{
			Value value = reader.read( scope, type );
			Assert.assertEquals( "Values are not equal", expected, value );
		}
	}

	@Test
	public void testDecode_NR3() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Real.ref().resolve( scope );
		RealValueBig expected = new RealValueBig( BigDecimal.valueOf( 4.25d ) );
		byte[] result = InputUtils.writeValue( scope, type, expected );
		try( ByteArrayInputStream is = new ByteArrayInputStream( result );
		     AbstractBerReader reader = new DefaultBerReader( is, new CoreValueFactory() ) )
		{
			Value value = reader.read( scope, type );
			Assert.assertTrue( "Values are not equal", expected.isEqualTo( value ) );
		}
	}

	@Test
	public void testDecode_Zero() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Real.ref().resolve( scope );
		Value expected = new RealValueFloat( 0.0f );
		byte[] result = InputUtils.writeValue( scope, type, expected );
		try( ByteArrayInputStream is = new ByteArrayInputStream( result );
		     AbstractBerReader reader = new DefaultBerReader( is, new CoreValueFactory() ) )
		{
			Value value = reader.read( scope, type );
			Assert.assertTrue( "Values are not equal", expected.isEqualTo( value ) );
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
			new RealBerDecoder().decode( new ReaderContext( reader, scope, type, tag, -1, false ) );
			fail( "Must fail" );
		}
	}
}
