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

package org.asn1s.core.type;

import org.asn1s.api.*;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.*;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.DefinedValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.IntegerValue;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultAsn1Factory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class DefinedTypeTemplateTest
{
	@Test
	public void testValueResolve() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();

		Ref<Type> intType = factory.types().builtin( "INTEGER" );
		factory.types().define( "a", intType, factory.values().integer( 1 ), null );

		Ref<Value> nameRef = new ValueNameRef( "value-X-TYPE", null );
		Ref<Type> integerType =
				factory.types().builtin( "INTEGER",
				                         Collections.singletonList( factory.values().named( "a", nameRef ) ) );


		Template template = new Template();
		template.addParameter( new TemplateParameter( 0, new ValueNameRef( "value-X-TYPE" ), intType ) );
		DefinedType templateType =
				factory.types().define( "TemplateInteger", integerType, template );

		Ref<Type> typeInstance = factory.types().typeTemplateInstance( templateType.toRef(), Collections.singletonList( factory.values().integer( 2 ) ) );
		DefinedType singleInteger = factory.types().define( "SingleInteger", typeInstance, null );

		module.validate();

		Value aValue = singleInteger.getNamedValue( "a" );
		Assert.assertNotNull( "No value found", aValue );
		Assert.assertEquals( "Is not named value", Kind.NAME, aValue.getKind() );
		Assert.assertEquals( "Is not integer value", Kind.INTEGER, aValue.toNamedValue().getReferenceKind() );
		Assert.assertEquals( "Illegal value resolve", 2L, aValue.toIntegerValue().asLong() );
	}

	@Test
	public void testTypeResolve() throws Exception
	{
		TypeFactory factory = new CoreTypeFactory();
		Module module = factory.dummyModule();

		Ref<Type> intType = factory.builtin( "INTEGER" );
		DefinedType myType = factory.define( "MyType", intType, null );


		Template template = new Template();
		template.addParameter( new TemplateParameter( 0, new TypeNameRef( "X-Type" ), null ) );
		DefinedType templateType =
				factory.define( "MyTemplate", new TypeNameRef( "X-Type" ),
				                template );

		Type typeInstance = factory.typeTemplateInstance( templateType.toRef(), Collections.singletonList( myType.toRef() ) );
		DefinedType typeInstanceType = factory.define( "MyInstanceType", typeInstance, null );

		module.validate();

		Scope scope = typeInstance.getScope( typeInstanceType.createScope() );
		Assert.assertNotNull( "Sibling must not be null!", typeInstance.getSibling() );
		Type resolvedInstanceSubType = typeInstance.getSibling().resolve( scope );
		Assert.assertNotNull( "Type is not resolved", resolvedInstanceSubType );
		Assert.assertNotNull( "Sibling must not be null!", resolvedInstanceSubType.getSibling() );
		Assert.assertEquals( "Illegal type resolve", myType, resolvedInstanceSubType.getSibling().resolve( scope ) );
	}

	@Test
	public void testTemplateValueResolves() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();

		Ref<Type> intType = factory.types().builtin( "INTEGER" );
		CollectionType collectionType = factory.types().collection( Family.SEQUENCE );
		collectionType.addComponent( ComponentType.Kind.PRIMARY, "a", intType );
		collectionType.addComponent( ComponentType.Kind.PRIMARY, "b", intType );

		DefinedType type = factory.types().define( "CollectionType", collectionType, null );
		ValueCollection collection = factory.values().collection( true );
		Ref<Value> xValue = module.getValueResolver().getValueRef( "x-Value", null );
		collection.addNamed( "a", xValue );
		collection.addNamed( "b", factory.values().integer( 3 ) );
		Template template = new Template();
		template.addParameter( new TemplateParameter( 0, new ValueNameRef( "x-Value" ), intType ) );
		DefinedValue valueTemplate = factory.types().define( "value-Template", type, collection, template );

		IntegerValue integerValue = factory.values().integer( 10 );
		Value templateInstance = factory.types().valueTemplateInstance( valueTemplate.toRef(), Collections.singletonList( integerValue ) );
		DefinedValue value = factory.types().define( "value", type, templateInstance, null );

		module.validate();

		Assert.assertEquals( "Not collection value", Kind.NAMED_COLLECTION, value.getKind() );
		NamedValue a = value.toValueCollection().getNamedValue( "a" );
		Assert.assertNotNull( "No component 'a'", a );
		Assert.assertEquals( "Not integer", Kind.INTEGER, a.getReferenceKind() );
		Assert.assertEquals( "Illegal value", integerValue, a.toIntegerValue() );

	}
}
