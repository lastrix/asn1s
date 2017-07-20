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
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.RealValueFloat;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@SuppressWarnings( {"NumericCastThatLosesPrecision", "MagicNumber", "resource"} )
public class IntegerBerEncoderTest
{

	private static final Tag TAG = new Tag( TagClass.Universal, false, UniversalType.Integer.tagNumber() );

	@Test
	public void testWriteLong_0() throws Exception
	{
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			IntegerBerEncoder.writeLong( writer, 0L, null, false );
			verify( writer ).write( 0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteLong_minus_1() throws Exception
	{
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			IntegerBerEncoder.writeLong( writer, -1L, null, false );
			verify( writer ).write( -1 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteLong_256() throws Exception
	{
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			IntegerBerEncoder.writeLong( writer, 256L, null, false );
			verify( writer ).write( 1 );
			verify( writer ).write( 0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWriteLong_300000() throws Exception
	{
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			IntegerBerEncoder.writeLong( writer, 300000L, null, false );
			verify( writer ).write( (byte)0x04 );
			verify( writer ).write( (byte)0x93 );
			verify( writer ).write( (byte)0xE0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test( expected = IOException.class )
	public void testWriteLong_fail() throws Exception
	{
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			IntegerBerEncoder.writeLong( writer, 0L, null, true );
			fail( "This method must fail." );
		}
	}

	@Test
	public void testToByteArray_0() throws Exception
	{
		byte[] array = IntegerBerEncoder.toByteArray( 0L );
		Assert.assertEquals( "Illegal array size", 1, array.length );
		Assert.assertArrayEquals( "Arrays are different", new byte[]{0}, array );
	}

	@Test
	public void testToByteArray_minus_1() throws Exception
	{
		byte[] array = IntegerBerEncoder.toByteArray( -1L );
		Assert.assertEquals( "Illegal array size", 1, array.length );
		Assert.assertArrayEquals( "Arrays are different", new byte[]{-1}, array );
	}

	@Test
	public void testToByteArray_256() throws Exception
	{
		byte[] array = IntegerBerEncoder.toByteArray( 256L );
		Assert.assertEquals( "Illegal array size", 2, array.length );
		Assert.assertArrayEquals( "Arrays are different", new byte[]{1, 0}, array );
	}

	@Test
	public void testToByteArray_300000() throws Exception
	{
		byte[] array = IntegerBerEncoder.toByteArray( 300000L );
		Assert.assertEquals( "Illegal array size", 3, array.length );
		Assert.assertArrayEquals( "Arrays are different", new byte[]{0x04, (byte)0x93, (byte)0xE0}, array );
	}

	@Test
	public void testEncode_0() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		Value value = new IntegerValueInt( 0 );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new IntegerBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( 0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testEncode_minus_1() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		Value value = new IntegerValueInt( -1 );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new IntegerBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( -1 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testEncode_256() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		Value value = new IntegerValueInt( 256 );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new IntegerBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			verify( writer ).writeHeader( TAG, 2 );
			verify( writer ).write( 1 );
			verify( writer ).write( 0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Real.ref().resolve( scope );
		Value value = new IntegerValueInt( 256 );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new IntegerBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_value() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		Value value = new RealValueFloat( 0.0f );
		try( AbstractBerWriter writer = mock( AbstractBerWriter.class ) )
		{
			new IntegerBerEncoder().encode( new WriterContext( writer, scope, type, value, true ) );
			fail( "Must fail" );
		}
	}
}
