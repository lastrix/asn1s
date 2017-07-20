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
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.core.CoreUtils;
import org.asn1s.core.module.CoreModule;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class OctetStringBerEncoderTest
{
	private static final Tag TAG = new Tag( TagClass.Universal, false, UniversalType.OctetString.tagNumber() );

	@Test
	public void testEncode_Empty() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.OctetString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromHexString( "''H" );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new OctetStringBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testEncode_AF() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.OctetString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromHexString( "'AF'H" );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new OctetStringBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( new byte[]{(byte)0xAF} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromHexString( "'AF'H" );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new OctetStringBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_value() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.OctetString.ref().resolve( scope );
		Value value = BooleanValue.TRUE;
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new OctetStringBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}

}
