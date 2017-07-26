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
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.asn1s.core.value.x680.OptimizedOIDValueImpl;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ObjectIDBerEncoderTest
{
	private static final Tag TAG = new Tag( TagClass.UNIVERSAL, false, UniversalType.OBJECT_IDENTIFIER.tagNumber() );
	private static final Value OPTIMIZED_OID_VALUE = new OptimizedOIDValueImpl(
			Arrays.asList( new NamedValueImpl( "itu", new IntegerValueInt( 0 ) ),
			               new NamedValueImpl( "recommendation", new IntegerValueInt( 0 ) ),
			               new NamedValueImpl( "absd", new IntegerValueInt( 1 ) ),
			               new NamedValueImpl( "hello", new IntegerValueInt( 256 ) )
			) );

	@Test
	public void testEncode_NoHeader() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.OBJECT_IDENTIFIER.ref().resolve( scope );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new ObjectIDBerEncoder().encode( new WriterContext( writer, scope, type, OPTIMIZED_OID_VALUE, false ) );
			verify( writer, times( 2 ) ).write( 0 );
			verify( writer ).write( 1 );
			verify( writer ).write( (byte)0x82 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testEncode_Buffered() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.OBJECT_IDENTIFIER.ref().resolve( scope );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			when( writer.isBufferingAvailable() ).thenReturn( true );
			new ObjectIDBerEncoder().encode( new WriterContext( writer, scope, type, OPTIMIZED_OID_VALUE, true ) );
			verify( writer ).isBufferingAvailable();
			verify( writer ).startBuffer( -1 );
			verify( writer, times( 2 ) ).write( 0 );
			verify( writer ).write( 1 );
			verify( writer ).write( (byte)0x82 );
			verify( writer ).stopBuffer( TAG );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.INTEGER.ref().resolve( scope );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new ObjectIDBerEncoder().encode( new WriterContext( writer, scope, type, OPTIMIZED_OID_VALUE, false ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_value() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.OBJECT_IDENTIFIER.ref().resolve( scope );
		Value value = BooleanValue.TRUE;
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new ObjectIDBerEncoder().encode( new WriterContext( writer, scope, type, value, false ) );
			fail( "Must fail" );
		}
	}

}
