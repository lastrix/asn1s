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

package org.asn1s.io.ber.input;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.type.x680.collection.SequenceOfType;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.ValueCollectionImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class SequenceOfBerDecoderTest
{
	@Test
	public void testWriteSequenceOf_Buffered() throws Exception
	{
		Scope scope = CoreModule.getInstance().createScope();
		SequenceOfType type = new SequenceOfType();
		type.addComponent( Kind.Primary, "a", UniversalType.Integer.ref() );
		type.validate( scope );
		ComponentType component = type.getComponentType();
		Assert.assertNotNull( "No component a", component );
		ValueCollection expected = new ValueCollectionImpl( true );
		Ref<Value> valueInt = new IntegerValueInt( 0 );
		expected.addNamed( "a", valueInt );
		byte[] result = InputUtils.writeValue( scope, type, expected );
		try( ByteArrayInputStream is = new ByteArrayInputStream( result );
		     BerReader reader = new DefaultBerReader( is, new DefaultObjectFactory() ) )
		{
			Value value = reader.read( scope, type );
			Assert.assertEquals( "Values are not equal", expected, value );
		}
	}
}
