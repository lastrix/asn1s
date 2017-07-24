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
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.CollectionOfType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultAsn1Factory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InnerTypeConstraintTest
{
	@Test
	public void validate() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();

		CollectionOfType sequenceOfType = factory.types().collectionOf( Family.SequenceOf );
		sequenceOfType.setComponent( ComponentType.DUMMY, factory.types().builtin( "INTEGER" ) );

		factory.types().define( "My-Type", sequenceOfType, null );
		module.validate();

		ConstraintTemplate constraint = factory.constraints().innerType( factory.constraints().value( factory.values().integer( 1 ) ) );

		ValueCollection value = factory.values().collection( false );
		value.add( factory.values().integer( 1 ) );
		value.add( factory.values().integer( 1 ) );
		value.add( factory.values().integer( 1 ) );
		Scope scope = module.createScope();
		assertTrue( "Constraint failure", ConstraintTestUtils.checkConstraint( constraint, value, sequenceOfType, scope ) );

		ValueCollection value2 = factory.values().collection( false );
		value2.add( factory.values().integer( 1 ) );
		value2.add( factory.values().integer( 2 ) );
		assertFalse( "Constraint not failed", ConstraintTestUtils.checkConstraint( constraint, value2, sequenceOfType, scope ) );
	}

}
