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
import org.asn1s.api.util.TimeUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.x680.DateValueImpl;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class GeneralizedTimeBerDecoderTest
{
	private static final String TIME_VALUE = "20170601115700Z";

	@Test
	public void testDecode() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.GENERALIZED_TIME.ref().resolve( scope );
		Value value = new DateValueImpl( TimeUtils.parseGeneralizedTime( TIME_VALUE ) );
		byte[] result = InputUtils.writeValue( scope, type, value );
		int totalWritten = result.length - 2;
		byte[] noHeader = new byte[totalWritten];
		System.arraycopy( result, 2, noHeader, 0, noHeader.length );
		try( AbstractBerReader reader = mock( AbstractBerReader.class ) )
		{
			ValueFactory factory = mock( ValueFactory.class );
			when( reader.getValueFactory() ).thenReturn( factory );
			when( reader.read( any() ) ).then( invocationOnMock -> {
				System.arraycopy( result, 2, invocationOnMock.getArguments()[0], 0, totalWritten );
				return totalWritten;
			} );
			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.TAG ) ).toTag( false );
			new GeneralizedTimeBerDecoder().decode( new ReaderContext( reader, scope, type, tag, totalWritten, false ) );
			verify( reader ).getValueFactory();
			verify( reader ).read( any( byte[].class ) );
			verifyNoMoreInteractions( reader );
		}
	}

	@Test( expected = AssertionError.class )
	public void testDecode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.INTEGER.ref().resolve( scope );
		try( AbstractBerReader reader = mock( DefaultBerReader.class ) )
		{
			Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.TAG ) ).toTag( false );
			new GeneralizedTimeBerDecoder().decode( new ReaderContext( reader, scope, type, tag, -1, false ) );
			fail( "Must fail" );
		}
	}
}
