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

package org.asn1s.io.ber;

import org.asn1s.api.Module;
import org.asn1s.api.ObjectFactory;
import org.asn1s.api.UniversalType;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.CollectionType.Kind;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.util.HexUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.input.DefaultBerReader;
import org.asn1s.io.ber.output.DefaultBerWriter;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class SimpleTest
{
	@Test
	public void test1() throws Exception
	{
		ObjectFactory factory = new DefaultObjectFactory();
		Module module = factory.dummyModule();

		CollectionType sequenceType = factory.collection( Kind.Sequence );
		sequenceType.addComponent( ComponentType.Kind.Primary, "id", UniversalType.Integer.ref(), false, factory.integer( -1 ) );

		CollectionType subType = factory.collection( Kind.Sequence );
		subType.addComponent( ComponentType.Kind.Primary, "a", UniversalType.Real.ref(), false, null );
		subType.addComponent( ComponentType.Kind.Primary, "b", UniversalType.Integer.ref(), false, null );
		sequenceType.addComponent( ComponentType.Kind.Primary, "data", subType, false, null );

		DefinedType myType = factory.define( "My-Type", sequenceType, null );

		module.validate();

		ValueCollection collection = factory.collection( true );
		collection.addNamed( "id", factory.integer( 1 ) );

		ValueCollection data = factory.collection( true );
		data.addNamed( "a", factory.rZero() );
		data.addNamed( "b", factory.integer( 1 ) );
		collection.addNamed( "data", data );

		byte[] content;
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.Ber ) )
		{
			writer.write( myType.createScope(), myType, collection );
			content = writer.toByteArray();
		}

		try( Asn1Reader is = new DefaultBerReader( new ByteArrayInputStream( content ), new CoreValueFactory() ) )
		{
			Value value = is.read( myType.createScope(), myType );
			Assert.assertEquals( "Values are not equal", collection, value );
		}
		String contentStr = HexUtils.toHexString( content );
		Assert.assertNotNull( "No content", contentStr );
	}
}
