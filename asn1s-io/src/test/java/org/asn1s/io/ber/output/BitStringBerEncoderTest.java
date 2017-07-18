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
import org.asn1s.core.type.x680.string.BitStringType;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.asn1s.io.ber.BerRules;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class BitStringBerEncoderTest
{
	private static final Tag TAG = new Tag( TagClass.Universal, false, UniversalType.BitString.tagNumber() );
	private static final String VALUE_0101B = "'0101'B";
	private static final String VALUE_0000B = "'0000'B";
	private static final String VALUE_01010000000000B = "'01010000000000'B";

	@Test
	public void testWrite_0101_Der() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.BitString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_0101B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Der );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 2 );
			verify( writer ).write( 4 );
			verify( writer ).write( new byte[]{0x50} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_0101_Ber() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.BitString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_0101B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Ber );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 2 );
			verify( writer ).write( 4 );
			verify( writer ).write( new byte[]{0x50} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_01010000000000_Der_noNamed() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.BitString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_01010000000000B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Der );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 3 );
			verify( writer ).write( 2 );
			verify( writer ).write( new byte[]{0x50, 0x0} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_01010000000000_Der_hasNamed() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = new BitStringType( Arrays.asList(
				new NamedValueImpl( "a", new IntegerValueInt( 1 ) ),
				new NamedValueImpl( "b", new IntegerValueInt( 2 ) ),
				new NamedValueImpl( "c", new IntegerValueInt( 3 ) ),
				new NamedValueImpl( "d", new IntegerValueInt( 4 ) ) ) );
		type.validate( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_01010000000000B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Der );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 2 );
			verify( writer ).write( 4 );
			verify( writer ).write( new byte[]{0x50} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_0000_Der_hasNamed() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = new BitStringType( Arrays.asList(
				new NamedValueImpl( "a", new IntegerValueInt( 1 ) ),
				new NamedValueImpl( "b", new IntegerValueInt( 2 ) ),
				new NamedValueImpl( "c", new IntegerValueInt( 3 ) ),
				new NamedValueImpl( "d", new IntegerValueInt( 4 ) ) ) );
		type.validate( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_0000B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Der );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 2 );
			verify( writer ).write( 1 );
			verify( writer ).write( new byte[]{0x0} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_0000_Ber() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.BitString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_0000B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Ber );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 2 );
			verify( writer ).write( 4 );
			verify( writer ).write( new byte[]{0x0} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_Empty_Ber() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.BitString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromBitString( "''B" );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Ber );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 1 );
			verify( writer ).write( 0 );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test
	public void testWrite_01010000000000_Ber() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.BitString.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_01010000000000B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			when( writer.getRules() ).thenReturn( BerRules.Ber );
			new BitStringBerEncoder().encode( writer, scope, type, value, true );
			verify( writer ).getRules();
			verify( writer ).writeHeader( TAG, 3 );
			verify( writer ).write( 2 );
			verify( writer ).write( new byte[]{0x50, 0x0} );
			verifyNoMoreInteractions( writer );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_type() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.Integer.ref().resolve( scope );
		Value value = CoreUtils.byteArrayFromBitString( VALUE_0101B );
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			new BitStringBerEncoder().encode( writer, scope, type, value, false );
			fail( "Must fail" );
		}
	}

	@Test( expected = AssertionError.class )
	public void testEncode_fail_value() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		Type type = UniversalType.BitString.ref().resolve( scope );
		Value value = BooleanValue.TRUE;
		try( BerWriter writer = mock( BerWriter.class ) )
		{
			new BitStringBerEncoder().encode( writer, scope, type, value, false );
			fail( "Must fail" );
		}
	}
}
