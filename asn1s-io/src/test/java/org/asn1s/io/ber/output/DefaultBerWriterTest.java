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

package org.asn1s.io.ber.output;

import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.input.DefaultBerReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class DefaultBerWriterTest
{
	@Test
	public void testChoiceReadWrite() throws Exception
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

		performReadTest( type.createScope(), "Unable to read choice value", type, value );
	}


	@Test
	public void testTaggedReadWrite() throws Exception
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

		performReadTest( type2.createScope(), "Encoding of [APPLICATION 3] IMPLICIT INTEGER failed", type2, value );
		performReadTest( type3.createScope(), "Encoding of [2] type2 failed", type3, value );
		performReadTest( type4.createScope(), "Encoding of [APPLICATION 7] IMPLICIT type3 failed", type4, value );
		performReadTest( type5.createScope(), "Encoding of [2] IMPLICIT type2 failed", type5, value );
	}

	private static void performReadTest( Scope scope, String message, Ref<Type> type, Value value ) throws Exception
	{
		byte[] expected = writeValue( scope, type, value );
		Value iValue;
		try( ByteArrayInputStream bis = new ByteArrayInputStream( expected );
		     Asn1Reader is = new DefaultBerReader( bis, new CoreValueFactory() ) )
		{
			iValue = is.read( scope, type );
		}
		byte[] actual = writeValue( scope, type, iValue );
		Assert.assertArrayEquals( message, expected, actual );
	}

	private static byte[] writeValue( Scope scope, Ref<Type> type, Value value ) throws Exception
	{
		byte[] result;
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.Der ) )
		{
			writer.write( scope, type, value );
			result = writer.toByteArray();
		}
		return result;
	}

}
