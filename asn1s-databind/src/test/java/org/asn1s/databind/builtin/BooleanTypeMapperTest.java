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

package org.asn1s.databind.builtin;

import org.asn1s.api.type.DefinedType;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.CoreValueFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BooleanTypeMapperTest
{
	private static final CoreValueFactory FACTORY = new CoreValueFactory();
	@SuppressWarnings( "ConstantConditions" )
	@NotNull
	private static final DefinedType BOOLEAN = CoreModule.getInstance().getTypeResolver().getType( "BOOLEAN" );
//
//	@Test
//	public void testGetters()
//	{
//		BooleanTypeMapper mapper = new BooleanTypeMapper( boolean.class, BOOLEAN );
//		assertEquals( "Not equals", BOOLEAN, mapper.getAsn1Type() );
//		assertEquals( "Not equals", boolean.class, mapper.getJavaType() );
//	}

	@Test
	public void toAsn1() throws Exception
	{
		BooleanTypeMapper mapper = new BooleanTypeMapper( boolean.class, BOOLEAN );
		assertEquals( "Not equals", BooleanValue.TRUE, mapper.toAsn1( FACTORY, true ) );
		assertEquals( "Not equals", BooleanValue.FALSE, mapper.toAsn1( FACTORY, false ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toAsn1Fails() throws Exception
	{
		BooleanTypeMapper mapper = new BooleanTypeMapper( boolean.class, BOOLEAN );
		mapper.toAsn1( FACTORY, 1L );
		fail( "Must fail" );
	}

	@Test
	public void toJava() throws Exception
	{
		BooleanTypeMapper mapper = new BooleanTypeMapper( boolean.class, BOOLEAN );
		assertEquals( "Not equals", Boolean.TRUE, mapper.toJava( BooleanValue.TRUE ) );
		assertEquals( "Not equals", Boolean.FALSE, mapper.toJava( BooleanValue.FALSE ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toJavaFails() throws Exception
	{
		BooleanTypeMapper mapper = new BooleanTypeMapper( boolean.class, BOOLEAN );
		mapper.toJava( FACTORY.integer( 1 ) );
		fail( "Must fail" );
	}
}
