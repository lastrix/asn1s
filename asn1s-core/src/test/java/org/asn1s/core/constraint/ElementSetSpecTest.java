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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith( Parameterized.class )
public class ElementSetSpecTest
{
	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		Collection<Object[]> list = new ArrayList<>();

		list.add( new Object[]{"Success: A A", true, Arrays.asList( new BooleanConstraintTemplate( false ), new BooleanConstraintTemplate( false ) ), null} );
		list.add( new Object[]{"Failure: F A", true, Arrays.asList( new BooleanConstraintTemplate( true ), new BooleanConstraintTemplate( false ) ), null} );
		list.add( new Object[]{"Failure: F F", false, Arrays.asList( new BooleanConstraintTemplate( true ), new BooleanConstraintTemplate( true ) ), null} );
		list.add( new Object[]{"Success: ALL EXCEPT A", true, null, new BooleanConstraintTemplate( true )} );
		list.add( new Object[]{"Failure: ALL EXCEPT A", false, null, new BooleanConstraintTemplate( false )} );
		return list;
	}

	public ElementSetSpecTest( String title, boolean expectedResult, List<ConstraintTemplate> unions, ConstraintTemplate exclusion )
	{
		this.title = title;
		this.expectedResult = expectedResult;
		this.unions = unions == null ? null : new ArrayList<>( unions );
		this.exclusion = exclusion;
	}

	private final String title;
	private final boolean expectedResult;
	private final List<ConstraintTemplate> unions;
	private final ConstraintTemplate exclusion;

	@Test
	public void doTest()
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();
		DefinedType type = factory.types().define( "MyInt", factory.types().builtin( "INTEGER" ), null );

		ConstraintTemplate specs = unions == null ? factory.constraints().elementSetSpec( exclusion ) : factory.constraints().elementSetSpec( unions );
		boolean actual = ConstraintTestUtils.checkConstraint( specs, factory.values().integer( 1 ), type, module.createScope() );
		Assert.assertEquals( title + ": failed", expectedResult, actual );
	}
}
