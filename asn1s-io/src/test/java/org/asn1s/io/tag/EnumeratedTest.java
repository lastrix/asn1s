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
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Enumerated;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Test;

@SuppressWarnings( "ALL" )
public class EnumeratedTest
{
	@Test
	public void testIO() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Enumerated enumerated = factory.enumerated();
		enumerated.addItem( Enumerated.ItemKind.Primary, "a", factory.integer( 1 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "b", factory.integer( 10 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "c", factory.integer( 12 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "d", factory.integer( 41 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "e", factory.integer( 91 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "f", factory.integer( 111 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "g", factory.integer( 121 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "k", factory.integer( 191 ) );
		enumerated.addItem( Enumerated.ItemKind.Primary, "l", factory.integer( 201 ) );

		DefinedType type = factory.define( "My-Enum", enumerated, null );
		module.validate();

		scope = type.createScope();
		Utils.performWriteTest( scope, "Failed to encode a enumeration item", type, factory.integer( 1 ), new byte[]{0xA, 0x1, 0x1} );
		Utils.performWriteTest( scope, "Failed to encode e enumeration item", type, factory.integer( 91 ), new byte[]{0xA, 0x1, 0x5B} );
		Utils.performWriteTest( scope, "Failed to encode l enumeration item", type, factory.integer( 201 ), new byte[]{0xA, 0x2, 0x0, (byte)0xC9} );


		Utils.performReadTest( scope, "Failed to encode a enumeration item", type, factory.integer( 1 ) );
		Utils.performReadTest( scope, "Failed to encode e enumeration item", type, factory.integer( 91 ) );
		Utils.performReadTest( scope, "Failed to encode l enumeration item", type, factory.integer( 201 ) );
	}
}
