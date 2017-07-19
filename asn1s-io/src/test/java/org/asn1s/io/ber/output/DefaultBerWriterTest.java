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
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeFactory;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.RealValueBig;
import org.asn1s.core.value.x680.RealValueFloat;
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.input.DefaultBerReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class DefaultBerWriterTest
{
	@Test
	public void writeHugeReal() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		ConstraintTemplate constraintTemplate = factory.valueRange( new RealValueFloat( 0.0f ), false, null, false );
		Type tagged = factory.constrained( constraintTemplate, UniversalType.Real.ref() );
		Type defined = factory.define( "MyReal", tagged, null );
		module.validate();
		Value value = new RealValueBig( new BigDecimal( BigInteger.valueOf( 34645344 ).pow( 15636 ) ) );
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.Der ) )
		{
			writer.write( scope, defined, value );
			Assert.assertEquals( "Arrays have different length", 117900, writer.toByteArray().length );
		}
	}

	@Test
	public void testHugeTagNumber() throws Exception
	{
		TypeFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		Type tagged = factory.tagged( TagEncoding.application( 2048 ), UniversalType.Integer.ref() );
		Type defined = factory.define( "MyTagged", tagged, null );
		module.validate();
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.Der ) )
		{
			writer.write( scope, defined, new IntegerValueInt( 0 ) );

			//7F900003020100
			Assert.assertArrayEquals( "", new byte[]{0x7F, (byte)0x90, 0x00, 0x03, 0x02, 0x01, 0x00}, writer.toByteArray() );
		}
	}

	@Test( expected = IOException.class )
	public void testNonInternalOsFail() throws Exception
	{
		TypeFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		try( ByteArrayOutputStream os = new ByteArrayOutputStream();
		     Asn1Writer writer = new DefaultBerWriter( BerRules.Der, os ) )
		{
			writer.write( scope, UniversalType.Integer.ref().resolve( scope ), new IntegerValueInt( 0 ) );
			writer.toByteArray();
			Assert.fail( "Must fail!" );
		}
	}

	@Test
	public void testChoiceReadWrite() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();
		Scope scope = module.createScope();

		CollectionType choiceType = factory.collection( CollectionType.Kind.Choice );

		CollectionType sequenceType = factory.collection( CollectionType.Kind.Sequence );
		sequenceType.addComponent( Kind.Primary, "a", UniversalType.Integer.ref(), false, null );
		sequenceType.addComponent( Kind.Primary, "b", UniversalType.Real.ref(), false, null );

		choiceType.addComponent( Kind.Primary, "seq", sequenceType, false, null );
		choiceType.addComponent( Kind.Primary, "b", UniversalType.Real.ref(), false, null );

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
