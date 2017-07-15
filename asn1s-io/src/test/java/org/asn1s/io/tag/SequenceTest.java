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

package org.asn1s.io.tag;

import org.asn1s.api.*;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Test;

@SuppressWarnings( "ALL" )
public class SequenceTest
{
	@Test
	public void testWrite() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Ref<Type> intRef = UniversalType.Integer.ref();
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( ComponentType.Kind.Primary, "id", intRef, false, factory.integer( -1 ) );
		sequenceType.addComponent( ComponentType.Kind.Primary, "data", UniversalType.OctetString.ref(), false, null );

		DefinedType type = factory.define( "My-Seq", sequenceType, null );
		module.validate();

		ValueCollection collection = factory.collection( true );
		collection.addNamed( "id", factory.integer( 1 ) );
		collection.addNamed( "data", factory.hString( "'12345'H" ) );

		// 30 80 04 03   12 34 50 00   00
		byte[] expected = {0x30, 0x08, (byte)0x80, 0x01, 0x01, (byte)0x81, 0x03, 0x12, 0x34, 0x50};
		Utils.performWriteTest( type.createScope(), "Unable to write value: " + collection, type, collection, expected );
	}

	@Test
	public void testRead() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Ref<Type> intRef = UniversalType.Integer.ref();
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( ComponentType.Kind.Primary, "id", intRef, false, factory.integer( -1 ) );
		sequenceType.addComponent( ComponentType.Kind.Primary, "data", UniversalType.OctetString.ref(), false, null );

		DefinedType type = factory.define( "My-Seq", sequenceType, null );
		module.validate();

		ValueCollection collection = factory.collection( true );
		collection.addNamed( "id", factory.integer( 1 ) );
		collection.addNamed( "data", factory.hString( "'12345'H" ) );

		Utils.performReadTest( type.createScope(), "Unable to read sequence value", type, collection );
	}

	@Test
	public void testRead2() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Ref<Type> intRef = UniversalType.Integer.ref();
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( ComponentType.Kind.Primary, "id", intRef, false, factory.integer( -1 ) );

		CollectionType subType = factory.collection( CollectionType.Kind.Sequence );
		subType.addComponent( ComponentType.Kind.Primary, "a", UniversalType.Real.ref(), false, null );
		subType.addComponent( ComponentType.Kind.Primary, "b", intRef, false, null );
		sequenceType.addComponent( ComponentType.Kind.Primary, "data", subType, false, null );

		DefinedType myType = factory.define( "My-Type", sequenceType, null );

		module.validate();

		ValueCollection collection = factory.collection( true );
		collection.addNamed( "id", factory.integer( 1 ) );

		ValueCollection data = factory.collection( true );
		data.addNamed( "a", factory.rZero() );
		data.addNamed( "b", factory.integer( 1 ) );
		collection.addNamed( "data", data );

		Utils.performReadTest( myType.createScope(), "Unable to read sequence value", myType, collection );
	}

	@Test
	public void testExtnRead() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Ref<Type> intRef = UniversalType.Integer.ref();
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( ComponentType.Kind.Primary, "a", intRef, false, null );
		sequenceType.addComponent( ComponentType.Kind.Extension, "b", UniversalType.Real.ref(), false, factory.integer( 2 ) );
		sequenceType.addComponent( ComponentType.Kind.Extension, "c", UniversalType.UTF8String.ref(), false, null );
		sequenceType.addComponent( ComponentType.Kind.Extension, "c1", UniversalType.UTF8String.ref(), false, null );
		sequenceType.addComponent( ComponentType.Kind.Secondary, "d", intRef, false, null );

		DefinedType myType = factory.define( "My-Type", sequenceType, null );
		module.validate();

		ValueCollection collection = factory.collection( true );
		collection.addNamed( "a", factory.integer( 1 ) );
		collection.addNamed( "c", factory.cString( "abc" ) );
		collection.addNamed( "d", factory.integer( 2 ) );

		Utils.performReadTest( myType.createScope(), "Unable to read sequence value", myType, collection );
	}

}
