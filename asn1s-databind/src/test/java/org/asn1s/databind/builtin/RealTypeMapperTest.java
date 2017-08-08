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

import org.asn1s.api.type.NamedType;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.databind.TypeMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RealTypeMapperTest
{
	private static final ValueFactory FACTORY = new CoreValueFactory();
	@SuppressWarnings( "ConstantConditions" )
	@NotNull
	private static final NamedType REAL = CoreModule.getInstance().getTypeResolver().getType( "REAL" );

	@Test
	public void toAsn1AndBack() throws Exception
	{
		doTest( new RealTypeMapper( float.class, REAL ), 1f );
		doTest( new RealTypeMapper( double.class, REAL ), 1d );
		doTest( new RealTypeMapper( BigDecimal.class, REAL ), BigDecimal.ONE );
	}

	private static void doTest( TypeMapper mapper, Object value )
	{
		assertEquals( "Not equals", value, mapper.toJava( mapper.toAsn1( FACTORY, value ) ) );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toAsn1Fails() throws Exception
	{
		TypeMapper mapper = new RealTypeMapper( double.class, REAL );
		mapper.toAsn1( FACTORY, true );
		fail( "Must fail" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void toJavaFails() throws Exception
	{
		TypeMapper mapper = new RealTypeMapper( double.class, REAL );
		mapper.toJava( BooleanValue.TRUE );
		fail( "Must fail" );
	}
}
