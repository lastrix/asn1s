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
import org.asn1s.api.value.ValueFactory;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.core.value.x680.RealValueBig;
import org.asn1s.core.value.x680.RealValueDouble;
import org.asn1s.io.ber.BerUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

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
		     BerReader reader = new DefaultBerReader( is, new DefaultObjectFactory() ) )
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
		try( BerReader reader = mock( BerReader.class ) )
		{
			ValueFactory factory = mock( CoreValueFactory.class );
			when( reader.getValueFactory() ).thenReturn( factory );
			when( reader.read() ).thenReturn( BerUtils.REAL_ISO_6093_NR3 );
			when( reader.read( any( byte[].class ) ) ).thenAnswer( invocationOnMock -> {
				System.arraycopy( result, 3, invocationOnMock.getArguments()[0], 0, result.length - 3 );
				return result.length - 3;
			} );
			when( factory.real( any( String.class ) ) )
					.then( invocationOnMock -> new RealValueBig( BigDecimal.valueOf( Double.parseDouble( (String)invocationOnMock.getArguments()[0] ) ) ) );

			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.Tag ) ).toTag( false );
			Value value = new RealBerDecoder().decode( reader, scope, type, tag, result.length - 2 );
			verify( reader ).read();
			verify( reader ).read( any( byte[].class ) );
			verify( reader ).getValueFactory();
			verify( factory ).real( any( String.class ) );
			verifyNoMoreInteractions( reader );
			Assert.assertEquals( "Values are not equal", expected, value );
		}
	}

	@Test
	public void testDecode_Zero() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Real.ref().resolve( scope );
		try( BerReader reader = mock( BerReader.class ) )
		{
			ValueFactory factory = mock( ValueFactory.class );
			when( reader.getValueFactory() ).thenReturn( factory );
			when( reader.read() ).thenReturn( (byte)0 );
			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.Tag ) ).toTag( false );
			new RealBerDecoder().decode( reader, scope, type, tag, 1 );
			verify( reader ).read();
			verify( reader ).getValueFactory();
			verify( factory ).rZero();
			verifyNoMoreInteractions( reader );
		}
	}

	@Test
	public void testDecode_Zero_Empty() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Real.ref().resolve( scope );
		try( BerReader reader = mock( BerReader.class ) )
		{
			ValueFactory factory = mock( ValueFactory.class );
			when( reader.getValueFactory() ).thenReturn( factory );
			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.Tag ) ).toTag( false );
			new RealBerDecoder().decode( reader, scope, type, tag, 0 );
			verify( reader ).getValueFactory();
			verify( factory ).rZero();
			verifyNoMoreInteractions( reader );
		}
	}

	@Test( expected = AssertionError.class )
	public void testDecode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		try( BerReader reader = mock( BerReader.class ) )
		{
			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.Tag ) ).toTag( false );
			new RealBerDecoder().decode( reader, scope, type, tag, -1 );
			fail( "Must fail" );
		}
	}
}
