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

import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.x681.ClassFieldRef;
import org.asn1s.api.type.x681.ClassType;
import org.asn1s.api.type.x681.ScopeClassFieldRef;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.CoreUtils;
import org.asn1s.core.module.ModuleImpl;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.core.type.x680.collection.SequenceType;
import org.asn1s.core.type.x681.ClassTypeImpl;
import org.asn1s.core.type.x681.TypeFieldType;
import org.asn1s.core.type.x681.ValueFieldType;
import org.asn1s.core.type.x681.ValueSetFieldType;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.OpenTypeValueImpl;
import org.asn1s.core.value.x680.StringValueImpl;
import org.asn1s.core.value.x680.ValueCollectionImpl;
import org.junit.Assert;
import org.junit.Test;

public class ClassTypeImplTest
{
	@Test
	public void testVariableTypeFields() throws Exception
	{
		Module module = ModuleImpl.newDummy( new ModuleSet() );
		ClassType classType = new ClassTypeImpl();
		classType.add( new TypeFieldType( "&TypeField", true, null ) );
		classType.add( new ValueFieldType( "&fixedTypeValueField", UniversalType.INTEGER.ref(), false, true ) );
		ScopeClassFieldRef fieldTypeRef = new ScopeClassFieldRef( "&TypeField" );
		classType.add( new ValueFieldType( "&variableTypeValueField", fieldTypeRef, false, true ) );
		classType.add( new ValueSetFieldType( "&FixedTypeValueSetField", UniversalType.INTEGER.ref(), true, null ) );
		classType.add( new ValueSetFieldType( "&VariableTypeValueSetField", fieldTypeRef, true, null ) );

		ClassType simpleClassType = new ClassTypeImpl();
		simpleClassType.add( new ValueFieldType( "&value", UniversalType.INTEGER.ref(), false, false ) );

		CollectionType sequence = new SequenceType( true );
		sequence.addComponent( Kind.PRIMARY, "openTypeComponent1", new ClassFieldRef( classType, "&TypeField" ) );
		sequence.addComponent( Kind.PRIMARY, "integerComponent1", new ClassFieldRef( classType, "&fixedTypeValueField" ) );
		sequence.addComponent( Kind.PRIMARY, "openTypeComponent2", new ClassFieldRef( classType, "&variableTypeValueField" ) );
		sequence.addComponent( Kind.PRIMARY, "integerComponent2", new ClassFieldRef( classType, "&FixedTypeValueSetField" ) );
		sequence.addComponent( Kind.PRIMARY, "openTypeComponent3", new ClassFieldRef( classType, "&VariableTypeValueSetField" ) );

		DefinedTypeImpl type = new DefinedTypeImpl( module, "ExampleType", sequence );
		module.getTypeResolver().add( type );

		Scope scope = module.createScope();
		ValueCollection collection = new ValueCollectionImpl( true );
		Value openTypeComponent1Value = new OpenTypeValueImpl( UniversalType.BOOLEAN.ref(), BooleanValue.TRUE ).resolve( scope );
		collection.addNamed( "openTypeComponent1", openTypeComponent1Value );
		collection.addNamed( "integerComponent1", new IntegerValueInt( 123 ) );
		collection.addNamed( "openTypeComponent2", new OpenTypeValueImpl( UniversalType.IA5_STRING.ref(), new StringValueImpl( "abcdef" ) ).resolve( scope ) );
		collection.addNamed( "integerComponent2", new IntegerValueInt( 456 ) );
		collection.addNamed( "openTypeComponent3", new OpenTypeValueImpl( UniversalType.BIT_STRING.ref(), CoreUtils.byteArrayFromBitString( "'0101010101'B" ) ).resolve( scope ) );

		module.validate();

		Value value = type.optimize( scope, collection );
		Assert.assertNotNull( "No result", value );
	}
}
