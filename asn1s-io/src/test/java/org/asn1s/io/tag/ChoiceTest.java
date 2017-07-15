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

import org.asn1s.api.Module;
import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Test;

@SuppressWarnings( "ALL" )
public class ChoiceTest
{
	@Test
	public void testAll() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		CollectionType choiceType = factory.collection( CollectionType.Kind.Choice );

		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( ComponentType.Kind.Primary, "a", UniversalType.Integer.ref(), false, null );
		sequenceType.addComponent( ComponentType.Kind.Primary, "b", UniversalType.Real.ref(), false, null );

		choiceType.addComponent( ComponentType.Kind.Primary, "seq", sequenceType, false, null );
		choiceType.addComponent( ComponentType.Kind.Primary, "b", UniversalType.Real.ref(), false, null );

		DefinedType type = factory.define( "My-Choice", choiceType, null );
		module.validate();

		ValueCollection collection = factory.collection( true );
		collection.addNamed( "a", factory.integer( 1 ) );
		collection.addNamed( "b", factory.rZero() );
		Value value = factory.named( "seq", collection );

		//A0 05 80 01 01 81 00
		Utils.performWriteTest( type.createScope(), "Failed to write choice value: ", type, value, new byte[]{(byte)0xA0, (byte)0x05, (byte)0x80, 0x01, 0x01, (byte)0x81, 0x00} );
		Utils.performReadTest( type.createScope(), "Unable to read choice value", type, value );
	}
}
