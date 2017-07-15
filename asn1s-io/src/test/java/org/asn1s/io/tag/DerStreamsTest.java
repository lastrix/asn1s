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
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.input.DefaultBerReader;
import org.asn1s.io.ber.output.DefaultBerWriter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;

@SuppressWarnings( "ALL" )
public class DerStreamsTest
{
	@Test
	public void testEncode() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		DefinedType integerType = factory.define( "MyInt", factory.builtin( "INTEGER" ), null );
		DefinedType octetType = factory.define( "MyOct", factory.builtin( "OCTET STRING" ), null );

		Ref<Type> reference = new TypeNameRef( "S-Type", null );
		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( Kind.Primary, "a", reference, true, null );
		sequenceType.addComponent( Kind.Primary, "b", reference, true, null );
		sequenceType.addComponent( Kind.Primary, "c", reference, true, null );
		sequenceType.addComponent( Kind.Primary, "d", reference, false, null );

		IEncoding encoding = factory.tagEncoding( TagMethod.Implicit, TagClass.Application, factory.integer( 1 ) );
		DefinedType templateType = factory.define( "MyType", factory.tagged( encoding, sequenceType ),
		                                           Collections.singletonList( factory.templateParameter( 0, reference, null ) ) );


		Type typeInstance = factory.typeTemplateInstance( templateType.toRef(),
		                                                  Collections.singletonList( integerType )
		);
		DefinedType type = factory.define( "My-Type-Instance", typeInstance, null );
		module.validate();

		ValueCollection collection = factory.collection( true );
		collection.addNamed( "a", factory.integer( 1 ) );
		collection.addNamed( "b", factory.integer( 2 ) );
		collection.addNamed( "c", factory.integer( 3 ) );
		collection.addNamed( "d", factory.integer( 4 ) );


		byte[] bytes;
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.Der ) )
		{
			writer.write( scope, integerType, factory.integer( 2730 ) );
			writer.write( scope, octetType, factory.hString( "'AFC'H" ) );
			writer.write( scope, type, collection );
			bytes = writer.toByteArray();
		}
		String content = TestUtils.toHexString( bytes );
		try( ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
		     Asn1Reader is = new DefaultBerReader( bais, new CoreValueFactory() ) )
		{
			System.out.println( is.read( scope, integerType ) );
			System.out.println( is.read( scope, octetType ) );
			System.out.println( is.read( scope, type ) );
		}
		int k = 0;
	}
}
