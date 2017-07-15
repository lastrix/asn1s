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
public class RealTest
{
	@Test
	public void testRead() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();
		Type type = factory.builtin( "REAL" ).resolve( scope );
		Utils.performReadTest( scope, "Unable to read 0.15625", type, factory.real( 0.15625d ) );
		Utils.performReadTest( scope, "Unable to read 123.5", type, factory.real( 123.5d ) );
//		Utils.performReadTest( scope, "Unable to read -1E0", type, RealValue.create( -1E0d ) );
		Utils.performReadTest( scope, "Unable to read -5.5E33", type, factory.real( -5.5E33d ) );
		Utils.performReadTest( scope, "Unable to read 5555*2^-555", type, factory.real( 5555L, false, -555, false ) );
	}

	@Test
	public void testWrite() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();
		Type type = factory.builtin( "REAL" ).resolve( scope );
		//80 FB 05
		Utils.performWriteTest( scope, "Failed to encode 0.15625", type, factory.real( 0.15625d ), new byte[]{0x09, 0x03, (byte)0x80, (byte)0xFB, 0x05} );
	}
}
