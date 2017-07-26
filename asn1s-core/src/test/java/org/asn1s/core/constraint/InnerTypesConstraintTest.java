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
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultAsn1Factory;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InnerTypesConstraintTest
{
	@Test
	public void validate() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();

		CollectionType sequenceType = factory.types().collection( Family.SEQUENCE );
		sequenceType.setExtensible( true );
		sequenceType.addComponent( Kind.PRIMARY, "a", factory.types().builtin( "INTEGER" ) ).setOptional( true );
		sequenceType.addComponent( Kind.PRIMARY, "b", factory.types().builtin( "REAL" ) );

		DefinedType type = factory.types().define( "My-Type", sequenceType, null );
		module.validate();

		Ref<Value> realValueRef = factory.values().real( 0.0f );
		ConstraintTemplate constraint = factory.constraints().innerTypes(
				Collections.singletonList( factory.constraints().component( "b", factory.constraints().value( realValueRef ), Presence.PRESENT ) ),
				false );

		ValueCollection value = factory.values().collection( true );
		value.addNamed( "b", realValueRef );

		Scope scope = type.createScope();
		assertTrue( "Constraint failure", ConstraintTestUtils.checkConstraint( constraint, value, sequenceType, scope ) );

		ValueCollection value1 = factory.values().collection( true );
		value1.addNamed( "a", factory.values().integer( 1 ) );
		value1.addNamed( "b", realValueRef );
		assertFalse( "Constraint not failed", ConstraintTestUtils.checkConstraint( constraint, value1, sequenceType, scope ) );
	}
}
