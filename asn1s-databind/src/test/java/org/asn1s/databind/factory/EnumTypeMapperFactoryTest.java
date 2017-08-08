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

package org.asn1s.databind.factory;

import org.asn1s.annotation.Asn1Enumeration;
import org.asn1s.annotation.Asn1EnumerationItem;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.core.DefaultAsn1Factory;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnumTypeMapperFactoryTest
{
	private TypeMapper mapper;
	private DefaultAsn1Factory factory;
	private TypeMapperFactory mapperFactory;

	@Before
	public void before() throws Exception
	{
		factory = new DefaultAsn1Factory();
		factory.types().dummyModule();
		mapperFactory = new EnumTypeMapperFactory( new TypeMapperContext(), factory );
		assertTrue( "Priority must be less than 0", mapperFactory.getPriority() < 0 );
		mapper = mapperFactory.mapType( Values.class );
	}

	@Test
	public void testConvert() throws Exception
	{
		assertEquals( "Not equals", Values.VALUE_B, mapper.toJava( mapper.toAsn1( factory.values(), Values.VALUE_B ) ) );
		assertEquals( "Not equals", Values.VALUE_C, mapper.toJava( mapper.toAsn1( factory.values(), Values.VALUE_C ) ) );
		assertEquals( "Not equals", Values.VALUE_B, mapper.toJava( factory.values().integer( 2 ) ) );
		assertEquals( "Not equals", Values.VALUE_C, mapper.toJava( factory.values().integer( 3 ) ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testDuplicateFail() throws Exception
	{
		mapperFactory.mapType( Values.class );
		fail( "Must fail" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toJavaFails() throws Exception
	{
		mapper.toJava( factory.values().integer( 0 ) );
		fail( "Must fail" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toJavaFails2() throws Exception
	{
		mapper.toJava( factory.values().named( "aa", null ) );
		fail( "Must fail" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toJavaFails3() throws Exception
	{
		mapper.toJava( BooleanValue.TRUE );
		fail( "Must fail" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toAsn1Fails() throws Exception
	{
		mapper.toAsn1( factory.values(), true );
		fail( "Must fail" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testUnsupportedType() throws Exception
	{
		mapperFactory.mapType( Integer.class );
		fail( "Must fail!" );
	}

	@Test( expected = IllegalStateException.class )
	public void testNoEnumConstantsFail() throws Exception
	{
		mapperFactory.mapType( ValuesEmpty.class );
		fail( "Must fail" );
	}

	@Test( expected = IllegalStateException.class )
	public void testIllegalIndexes() throws Exception
	{
		mapperFactory.mapType( IllegalEnum.class );
		fail( "Must fail" );
	}

	@Test( expected = IllegalStateException.class )
	public void testIllegalIndexes2() throws Exception
	{
		mapperFactory.mapType( IllegalEnum2.class );
		fail( "Must fail" );
	}

	@Asn1Enumeration( name = "Values" )
	public enum Values
	{
		@Asn1EnumerationItem( name = "a", value = 1 )
		VALUE_A,
		@Asn1EnumerationItem( name = "b", value = 2 )
		VALUE_B,
		@Asn1EnumerationItem( name = "c", value = 3, extension = true )
		VALUE_C,
	}

	@Asn1Enumeration( name = "Values-Empty" )
	public enum ValuesEmpty
	{
		A, B, C
	}

	@Asn1Enumeration( name = "Values-Illegal" )
	public enum IllegalEnum
	{
		@Asn1EnumerationItem( name = "a", value = 1 )
		A,
		@Asn1EnumerationItem( name = "b" )
		B,
		@Asn1EnumerationItem( name = "c" )
		C
	}

	@Asn1Enumeration( name = "Values-Illegal2" )
	public enum IllegalEnum2
	{
		@Asn1EnumerationItem( name = "a" )
		A,
		@Asn1EnumerationItem( name = "b", value = 2 )
		B,
		@Asn1EnumerationItem( name = "c" )
		C
	}
}
