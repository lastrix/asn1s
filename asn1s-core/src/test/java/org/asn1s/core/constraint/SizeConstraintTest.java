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
public class SizeConstraintTest
{

	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		Collection<Object[]> list = new ArrayList<>();
		list.add( new Object[]{"Success: size(A) == 1", true, "A", "1"} );
		list.add( new Object[]{"Failure: size(AA) == 1", false, "AA", "1"} );
		return list;
	}

	public SizeConstraintTest( String title, boolean expectedResult, String value, String constraintParam )
	{
		this.title = title;
		this.expectedResult = expectedResult;
		this.value = value;
		this.constraintParam = constraintParam;
	}

	private final String title;
	private final boolean expectedResult;
	private final String value;
	private final String constraintParam;

	@Test
	public void doTest() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();
		DefinedType type = factory.types().define( "MyStr", factory.types().builtin( "UTF8String" ), null );
		module.validate();

		ConstraintTemplate e = factory.constraints().size( factory.constraints().value( factory.values().integer( constraintParam ) ) );
		boolean actual = ConstraintTestUtils.checkConstraint( e, factory.values().cString( value ), type, module.createScope() );
		Assert.assertEquals( title + ": failed", expectedResult, actual );
	}
}
