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

import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.TimeUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.x680.DateValueImpl;
import org.asn1s.io.ber.BerRules;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@SuppressWarnings( {"resource", "MagicNumber"} )
public class GeneralizedTimeBerEncoderTest
{
	private static final Tag TAG = new Tag( TagClass.UNIVERSAL, false, UniversalType.GENERALIZED_TIME.tagNumber() );
	private static final String TIME_VALUE = "20170601115700Z";

	@Test
	public void testWrite_Der() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.GENERALIZED_TIME.ref().resolve( scope );
		Value value = new DateValueImpl( TimeUtils.parseGeneralizedTime( TIME_VALUE ) );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.DER );
			new GeneralizedTimeBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 15 );
			verify( writer ).write( new byte[]{0x32, 0x30, 0x31, 0x37, 0x30, 0x36, 0x30, 0x31, 0x31, 0x31, 0x35, 0x37, 0x30, 0x30, 0x5A} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_Ber() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.GENERALIZED_TIME.ref().resolve( scope );
		Value value = new DateValueImpl( TimeUtils.parseGeneralizedTime( TIME_VALUE ) );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.BER );
			new GeneralizedTimeBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 13 );
			verify( writer ).write( new byte[]{0x32, 0x30, 0x31, 0x37, 0x30, 0x36, 0x30, 0x31, 0x31, 0x31, 0x35, 0x37, 0x5A} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.INTEGER.ref().resolve( scope );
		Value value = new DateValueImpl( TimeUtils.parseGeneralizedTime( TIME_VALUE ) );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new GeneralizedTimeBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_value() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.GENERALIZED_TIME.ref().resolve( scope );
		Value value = BooleanValue.TRUE;
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new GeneralizedTimeBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}
}
