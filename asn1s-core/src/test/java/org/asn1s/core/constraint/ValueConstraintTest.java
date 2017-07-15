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

import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.DefinedType;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

@RunWith( Parameterized.class )
public class ValueConstraintTest
{
	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		Collection<Object[]> list = new ArrayList<>();
		list.add( new Object[]{"Success: 10 = 10", true, "10", "10"} );
		list.add( new Object[]{"Failure: 10 = 9", false, "10", "9"} );
		list.add( new Object[]{"Failure: 10 = 11", false, "10", "11"} );
		return list;
	}

	public ValueConstraintTest( String title, boolean expectedResult, String constraintParam, String value )
	{
		this.title = title;
		this.expectedResult = expectedResult;
		this.constraintParam = constraintParam;
		this.value = value;
	}

	private final String title;
	private final boolean expectedResult;
	private final String constraintParam;
	private final String value;

	@Test
	public void doTest() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		DefinedType type = factory.define( "MyInt", factory.builtin( "INTEGER" ), null );
		module.validate();

		ConstraintTemplate e = factory.value( factory.integer( constraintParam ) );
		Scope scope = module.createScope();
		boolean actual = ConstraintTestUtils.checkConstraint( e, factory.integer( value ), type, scope );
		Assert.assertEquals( title + ": failed", expectedResult, actual );
	}
}
