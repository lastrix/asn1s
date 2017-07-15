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
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.DefaultObjectFactory;
import org.junit.Test;

@SuppressWarnings( "ALL" )
public class TaggedTest
{
	@Test
	public void testWrite() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Ref<Type> type1 = factory.builtin( "INTEGER" );

		TagEncoding type2Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Implicit, TagClass.Application, 3 );
		DefinedType type2 = factory.define( "Type2",
		                                    factory.tagged( type2Encoding, type1 ),
		                                    null );

		TagEncoding type3Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Explicit, TagClass.ContextSpecific, 2 );

		DefinedType type3 = factory.define( "Type3",
		                                    factory.tagged( type3Encoding, type2 ),
		                                    null );

		TagEncoding type4Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Implicit, TagClass.Application, 7 );

		DefinedType type4 = factory.define( "Type4",
		                                    factory.tagged( type4Encoding, type3 ),
		                                    null );

		TagEncoding type5Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Implicit, TagClass.ContextSpecific, 2 );

		DefinedType type5 = factory.define( "Type5",
		                                    factory.tagged( type5Encoding, type2 ),
		                                    null );

		Value value = factory.integer( 100 );

		module.validate();

		Utils.performWriteTest( type2.createScope(), "Encoding of [APPLICATION 3] IMPLICIT INTEGER failed", type2, value, new byte[]{0x43, 0x01, 0x64} );
		Utils.performWriteTest( type3.createScope(), "Encoding of [2] type2 failed", type3, value, new byte[]{(byte)0xA2, 0x03, 0x43, 0x01, 0x64} );
		Utils.performWriteTest( type4.createScope(), "Encoding of [APPLICATION 7] IMPLICIT type3 failed", type4, value, new byte[]{(byte)0x67, 0x03, 0x43, 0x01, 0x64} );
		Utils.performWriteTest( type5.createScope(), "Encoding of [2] IMPLICIT type2 failed", type5, value, new byte[]{(byte)0x82, 0x01, 0x64} );
	}

	@Test
	public void testRead() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Ref<Type> type1 = factory.builtin( "INTEGER" );

		TagEncoding type2Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Implicit, TagClass.Application, 3 );
		DefinedType type2 = factory.define( "Type2",
		                                    factory.tagged( type2Encoding, type1 ),
		                                    null );

		TagEncoding type3Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Explicit, TagClass.ContextSpecific, 2 );

		DefinedType type3 = factory.define( "Type3",
		                                    factory.tagged( type3Encoding, type2 ),
		                                    null );

		TagEncoding type4Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Implicit, TagClass.Application, 7 );

		DefinedType type4 = factory.define( "Type4",
		                                    factory.tagged( type4Encoding, type3 ),
		                                    null );

		TagEncoding type5Encoding = TagEncoding.create( module.getTagMethod(), TagMethod.Implicit, TagClass.ContextSpecific, 2 );

		DefinedType type5 = factory.define( "Type5",
		                                    factory.tagged( type5Encoding, type2 ),
		                                    null );

		Value value = factory.integer( 100 );

		module.validate();

		Utils.performReadTest( type2.createScope(), "Encoding of [APPLICATION 3] IMPLICIT INTEGER failed", type2, value );
		Utils.performReadTest( type3.createScope(), "Encoding of [2] type2 failed", type3, value );
		Utils.performReadTest( type4.createScope(), "Encoding of [APPLICATION 7] IMPLICIT type3 failed", type4, value );
		Utils.performReadTest( type5.createScope(), "Encoding of [2] IMPLICIT type2 failed", type5, value );
	}
}
