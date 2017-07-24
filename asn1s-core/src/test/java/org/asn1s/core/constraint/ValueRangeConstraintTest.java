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

package org.asn1s.core.constraint;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.DefinedType;
import org.asn1s.core.DefaultAsn1Factory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith( Parameterized.class )
public class ValueRangeConstraintTest
{
	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		Collection<Object[]> list = new ArrayList<>();
		list.add( new Object[]{"Success: 0 <  1 < 10", true, "1", "0", true, "10", true} );
		list.add( new Object[]{"Failure: 0 <  0 < 10", false, "0", "0", true, "10", true} );
		list.add( new Object[]{"Failure: 0 <  10 < 10", false, "10", "0", true, "10", true} );

		list.add( new Object[]{"Success: 0 <=  1 <= 10", true, "1", "0", false, "10", false} );
		list.add( new Object[]{"Success: 0 <=  0 <= 10", true, "0", "0", false, "10", false} );
		list.add( new Object[]{"Success: 0 <=  10 <= 10", true, "10", "0", false, "10", false} );
		return list;
	}

	public ValueRangeConstraintTest( String title, boolean expectedResult, String value, String min, boolean minLt, String max, boolean maxGt )
	{
		this.title = title;
		this.expectedResult = expectedResult;
		this.value = value;
		this.min = min;
		this.minLt = minLt;
		this.max = max;
		this.maxGt = maxGt;
	}

	private final String title;
	private final boolean expectedResult;
	private final String value;
	private final String min;
	private final boolean minLt;
	private final String max;
	private final boolean maxGt;

	@Test
	public void doTest() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();
		DefinedType type = factory.types().define( "MyInt", factory.types().builtin( "INTEGER" ), null );
		module.validate();

		ConstraintTemplate e = factory.constraints().valueRange( min == null ? null : factory.values().integer( min ), minLt,
		                                                         max == null ? null : factory.values().integer( max ), maxGt );
		boolean actual = ConstraintTestUtils.checkConstraint( e, factory.values().integer( value ), type, module.createScope() );
		Assert.assertEquals( title + ": failed", expectedResult, actual );
	}
}
