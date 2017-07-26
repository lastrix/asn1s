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

package org.asn1s.core.type;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.StringValue;
import org.asn1s.core.DefaultAsn1Factory;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;

public class TimeTest
{
	@Test
	public void testGeneralized() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();

		DefinedType type = factory.types().define( "GenInst", UniversalType.GENERALIZED_TIME.ref(), null );

		Ref<Value> value = factory.values().timeValue( Instant.now() );
		StringValue cString = factory.values().cString( "20170701080136.345Z" );
		module.validate();
		Scope scope = module.createScope();
		type.accept( scope, value );
		type.optimize( scope, cString );

		Assert.assertEquals( "Optimized value is not Time.", Kind.TIME, type.optimize( scope, cString ).getKind() );
	}

	@Test
	public void testUTC() throws Exception
	{
		Asn1Factory factory = new DefaultAsn1Factory();
		Module module = factory.types().dummyModule();

		DefinedType type = factory.types().define( "GenInst", UniversalType.UTC_TIME.ref(), null );

		Ref<Value> value = factory.values().timeValue( Instant.now() );
		StringValue cString = factory.values().cString( "170701081514Z" );
		module.validate();
		Scope scope = module.createScope();
		type.accept( scope, value );
		type.optimize( scope, cString );

		Assert.assertEquals( "Optimized value is not Time.", Kind.TIME, type.optimize( scope, cString ).getKind() );
	}
}
