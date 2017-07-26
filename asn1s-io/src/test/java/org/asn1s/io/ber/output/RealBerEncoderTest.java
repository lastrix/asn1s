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
import org.asn1s.api.util.NRxUtils;
import org.asn1s.api.value.Value;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.x680.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class RealBerEncoderTest
{

	private static final Tag TAG = new Tag( TagClass.UNIVERSAL, false, UniversalType.REAL.tagNumber() );

	@Test( expected = AssertionError.class )
	public void testWrite_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.INTEGER.ref().resolve( scope );
		Value value = new IntegerValueInt( 0 );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testWrite_fail_value() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.INTEGER.ref().resolve( scope );
		Value value = new StringValueImpl( "Value" );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			fail( "Must fail" );
		}
	}


	@Test
	public void testWriteInteger_0() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		Value value = new IntegerValueInt( 0 );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteReal_neg_0() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		Value value = new RealValueFloat( -0.0f );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( 67 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteReal_neg_inf() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		Value value = new RealValueFloat( Float.NEGATIVE_INFINITY );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( 65 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteReal_pos_inf() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		Value value = new RealValueFloat( Float.POSITIVE_INFINITY );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( 64 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteReal_nan() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		Value value = new RealValueFloat( Float.NaN );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( 66 );
			verifyNoMoreInteractions( writer );
		}
	}


	@Test
	public void testWriteReal_big() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		BigDecimal decimal = new BigDecimal( BigInteger.valueOf( 1023234L ).pow( 5223 ) );
		byte[] bytes = NRxUtils.toCanonicalNR3( decimal.toString() ).getBytes( "UTF-8" );
		Value value = new RealValueBig( decimal );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 31396 );
			verify( writer ).write( 3 );
			verify( writer ).write( bytes );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteInteger_big() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		BigInteger bigInteger = BigInteger.valueOf( 1023234L ).pow( 5223 );
		byte[] bytes = NRxUtils.toCanonicalNR3( new BigDecimal( bigInteger ).toString() ).getBytes( "UTF-8" );
		Value value = new IntegerValueBig( bigInteger );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 31396 );
			verify( writer ).write( 3 );
			verify( writer ).write( bytes );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteReal_1_0() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		Value value = new RealValueFloat( 1.0f );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 3 );
			verify( writer ).write( -128 );
			verify( writer ).write( new byte[]{-51} );
			verify( writer ).write( new byte[]{0} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteReal_0_15625d() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.REAL.ref().resolve( scope );
		Value value = new RealValueDouble( 0.15625d );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new RealBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 3 );
			verify( writer ).write( -128 );
			verify( writer ).write( new byte[]{(byte)0xFB} );
			verify( writer ).write( new byte[]{0x05} );
			verifyNoMoreInteractions( writer );
		}
	}
}
