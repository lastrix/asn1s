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

import org.asn1s.api.Module;
import org.asn1s.api.ObjectFactory;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.type.DefinedType;
import org.asn1s.core.DefaultObjectFactory;
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
public class UnionTest
{
	@Parameters( name = "{0}" )
	public static Collection<Object[]> data()
	{
		Collection<Object[]> list = new ArrayList<>();
		list.add( new Object[]{"Success: F | F | A", true, Arrays.asList( new BooleanConstraintTemplate( true ), new BooleanConstraintTemplate( true ), new BooleanConstraintTemplate( false ) )} );
		list.add( new Object[]{"Failure: F | F", false, Arrays.asList( new BooleanConstraintTemplate( true ), new BooleanConstraintTemplate( true ) )} );
		return list;
	}

	public UnionTest( String title, boolean expectedResult, List<ConstraintTemplate> items )
	{
		this.title = title;
		this.expectedResult = expectedResult;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.items = items;
	}

	private final String title;
	private final boolean expectedResult;
	private final List<ConstraintTemplate> items;


	@Test
	public void doTest()
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		DefinedType type = factory.define( "MyInt", factory.builtin( "INTEGER" ), null );

		ConstraintTemplate union = factory.elementSetSpec( items );

		boolean actual = ConstraintTestUtils.checkConstraint( union, factory.integer( 1 ), type, module.createScope() );
		Assert.assertEquals( title + ": failed", expectedResult, actual );
	}
}
