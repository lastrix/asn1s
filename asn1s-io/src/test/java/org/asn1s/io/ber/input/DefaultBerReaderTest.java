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

import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeFactory;
import org.asn1s.api.value.Value;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.asn1s.core.value.x680.RealValueBig;
import org.asn1s.core.value.x680.RealValueFloat;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

public class DefaultBerReaderTest
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
		Value expected = new RealValueBig( new BigDecimal( BigInteger.valueOf( 34645 ).pow( 16663 ) ) );
		byte[] result = InputUtils.writeValue( scope, defined, expected );
		try( ByteArrayInputStream is = new ByteArrayInputStream( result );
		     AbstractBerReader reader = new DefaultBerReader( is, new DefaultObjectFactory() ) )
		{
			Value value = reader.read( scope, defined );
			Assert.assertEquals( "Values are not equal", expected, value );
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
		Value expected = new IntegerValueInt( 0 );
		byte[] result = InputUtils.writeValue( scope, defined, expected );
		try( ByteArrayInputStream is = new ByteArrayInputStream( result );
		     AbstractBerReader reader = new DefaultBerReader( is, new DefaultObjectFactory() ) )
		{
			Value value = reader.read( scope, defined );
			Assert.assertEquals( "Values are not equal", expected, value );
		}
	}
}
