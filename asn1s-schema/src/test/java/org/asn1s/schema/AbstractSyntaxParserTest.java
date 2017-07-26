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

package org.asn1s.schema;

import org.asn1s.api.Ref;
import org.asn1s.api.UniversalType;
import org.asn1s.core.DefaultAsn1Factory;
import org.asn1s.core.module.CoreModule;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.core.type.x681.ClassTypeImpl;
import org.asn1s.core.type.x681.FixedValueFieldType;
import org.asn1s.core.type.x681.TypeFieldType;
import org.asn1s.schema.x681.AbstractSyntaxParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class AbstractSyntaxParserTest
{
	@Test
	public void testParser() throws Exception
	{
		ClassTypeImpl classType = new ClassTypeImpl();
		classType.setSyntaxList( Arrays.asList( "&Type", "IDENTIFIED", "BY", "&id", "[", "CONSTRAINED", "BY", "&TypeConstraint", "]" ) );
		classType.add( new FixedValueFieldType( "&id", UniversalType.OBJECT_IDENTIFIER.ref(), true, false, null ) );
		classType.add( new TypeFieldType( "&Type", false, null ) );
		classType.add( new TypeFieldType( "&TypeConstraint", false, null ) );
		classType.validate( CoreModule.getInstance().createScope() );
		ModuleSet moduleSet = new ModuleSet();
		DefaultAsn1Factory factory = new DefaultAsn1Factory( moduleSet );
		AbstractSyntaxParser parser = new AbstractSyntaxParser( moduleSet, factory, CoreModule.getInstance(), classType );
		Map<String, Ref<?>> result = parser.parse( "Super-Type IDENTIFIED BY { rootOid 3 } CONSTRAINED BY TYPE-Constraint" );
		Assert.assertNotNull( "No result", result );
	}
}
