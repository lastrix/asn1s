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
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ComponentConstraintTest
{
	@Test
	public void test1() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();

		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		Ref<Type> intTypeRef = factory.builtin( "INTEGER" );
		sequenceType.addComponent( Kind.Primary, "a", intTypeRef, true, null );
		sequenceType.addComponent( Kind.Primary, "b", intTypeRef, false, null );

		ConstraintTemplate constraint = factory.component( "a", null, Presence.Absent );
		Type constrainedType = factory.constrained( constraint, sequenceType );
		factory.define( "My-Type", constrainedType, null );

		module.validate();

		ValueCollection value = factory.collection( true );
		value.addNamed( "a", factory.integer( 1 ) );
		value.addNamed( "b", factory.integer( 2 ) );


		Scope scope = module.createScope();
		assertFalse( "Constraint not failed", ConstraintTestUtils.checkConstraint( constraint, value, sequenceType, scope ) );

		ValueCollection value1 = factory.collection( true );
		value1.addNamed( "b", factory.integer( "1" ) );
		assertTrue( "Constraint failure", ConstraintTestUtils.checkConstraint( constraint, value1, sequenceType, scope ) );

		ConstraintTemplate constraint1 = factory.component( "a", null, Presence.Present );
		assertTrue( "Constraint failure", ConstraintTestUtils.checkConstraint( constraint1, value, sequenceType, scope ) );
		assertFalse( "Constraint not failed", ConstraintTestUtils.checkConstraint( constraint1, value1, sequenceType, scope ) );

		ConstraintTemplate constraint2 = factory.component( "a", new BooleanConstraintTemplate( false ), Presence.Optional );
		assertTrue( "Constraint failure", ConstraintTestUtils.checkConstraint( constraint2, value, sequenceType, scope ) );
		assertTrue( "Constraint failure", ConstraintTestUtils.checkConstraint( constraint2, value1, sequenceType, scope ) );

		ConstraintTemplate constraint3 = factory.component( "a", new BooleanConstraintTemplate( true ), Presence.Optional );
		assertFalse( "Constraint not failed", ConstraintTestUtils.checkConstraint( constraint3, value, sequenceType, scope ) );
		assertTrue( "Constraint failure", ConstraintTestUtils.checkConstraint( constraint3, value1, sequenceType, scope ) );
	}
}
