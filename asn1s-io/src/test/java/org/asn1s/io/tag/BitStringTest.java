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

import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Scope;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.Type;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Test;

@SuppressWarnings( "ALL" )
public class BitStringTest
{
	@Test
	public void testRead() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();
		Type type = factory.builtin( "BIT STRING" ).resolve( scope );
		Utils.performReadTest( scope, "Test of '0A3B5F291CD'H failed", type, factory.hString( "'0A3B5F291CD'H" ) );
		Utils.performReadTest( scope, "Test of '0110110101'B failed", type, factory.bString( "'0110110101'B" ) );
		Utils.performReadTest( scope, "Test of ''H failed", type, factory.hString( "''H" ) );
		Utils.performReadTest( scope, "Test of ''B failed", type, factory.bString( "''B" ) );
	}

	@Test
	public void testWrite() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();
		Type type = factory.builtin( "BIT STRING" ).resolve( scope );
		Utils.performWriteTest( scope, "Test of '0A3B 5F29 1CD 'H failed", type, factory.hString( "'0A3B 5F29 1CD 'H" ), new byte[]{0x3, 0x7, 0x4, 0xA, 0x3b, 0x5f, 0x29, 0x1C, (byte)0xD0} );
		Utils.performWriteTest( scope, "Test of '0110 1101 01'B failed", type, factory.bString( "'0110 1101 01'B" ), new byte[]{0x3, 0x3, 0x6, 0x6D, 0x40} );
		Utils.performWriteTest( scope, "Test of ''B failed", type, factory.hString( "''H" ), new byte[]{0x3, 0x1, 0x0} );
		Utils.performWriteTest( scope, "Test of ''H failed", type, factory.bString( "''B" ), new byte[]{0x3, 0x1, 0x0} );
	}
}
