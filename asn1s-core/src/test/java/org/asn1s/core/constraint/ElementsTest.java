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
public class ElementsTest
{
	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		Collection<Object[]> list = new ArrayList<>();
		list.add( new Object[]{"Success: A", true, new BooleanConstraintTemplate( false ), null} );
		list.add( new Object[]{"Failure: A", false, new BooleanConstraintTemplate( true ), null} );
		list.add( new Object[]{"Success: A EXCEPT B", true, new BooleanConstraintTemplate( false ), new BooleanConstraintTemplate( true )} );
		list.add( new Object[]{"Failure: A EXCEPT B", false, new BooleanConstraintTemplate( true ), new BooleanConstraintTemplate( false )} );

		return list;
	}

	public ElementsTest( String title, boolean expectedResult, ConstraintTemplate constraint, ConstraintTemplate except )
	{
		this.title = title;
		this.expectedResult = expectedResult;
		this.constraint = constraint;
		this.except = except;
	}

	private final String title;
	private final boolean expectedResult;
	private final ConstraintTemplate constraint;
	private final ConstraintTemplate except;

	@Test
	public void doTest() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();
		DefinedType type = factory.types().define( "MyInt", factory.types().builtin( "INTEGER" ), null );
		module.validate();

		ConstraintTemplate e = factory.constraints().elements( constraint, except );
		boolean actual = ConstraintTestUtils.checkConstraint( e, factory.values().integer( 1 ), type, module.createScope() );
		Assert.assertEquals( title + ": failed", expectedResult, actual );
	}
}
