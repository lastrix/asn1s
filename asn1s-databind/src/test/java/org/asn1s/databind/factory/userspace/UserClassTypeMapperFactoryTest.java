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

package org.asn1s.databind.factory.userspace;

import org.asn1s.api.value.Value;
import org.asn1s.core.DefaultAsn1Factory;
import org.asn1s.databind.Asn1Mapper;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.factory.Attribute;
import org.asn1s.databind.factory.Element;
import org.asn1s.databind.factory.TextElement;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class UserClassTypeMapperFactoryTest
{
	private static final DefaultAsn1Factory FACTORY = new DefaultAsn1Factory();

	@Test
	public void testMapping() throws Exception
	{
		Asn1Mapper mapper = new Asn1Mapper( FACTORY, new Class<?>[]{Element.class, TextElement.class, Attribute.class} );
		TypeMapper typeMapper = mapper.getContext().getTypeMapper( Element.class.getTypeName() + "=Java-Bind-Module:Element" );

		Element siblingSibling = new Element( "sub-item" );
		siblingSibling.setAttributes( Arrays.asList( new Attribute( "id", "22" ), new Attribute( "type", "subtype" ) ) );
		siblingSibling.setSiblings( Collections.singletonList( new TextElement( "text", "content" ) ) );

		Element sibling = new Element( "sibling" );
		sibling.setAttributes( Arrays.asList( new Attribute( "id", "2" ), new Attribute( "type", "plain" ) ) );
		sibling.setSiblings( Arrays.asList( new TextElement( "text", "wow!" ), siblingSibling ) );

		Element element = new Element( "root" );
		element.setAttributes( Arrays.asList( new Attribute( "flag", "true" ), new Attribute( "options", "221" ) ) );
		element.setSiblings( Arrays.asList( sibling, new TextElement( "value1", "Hello, World" ), new TextElement( "value2", "Good job!" ) ) );
		Value value = typeMapper.toAsn1( FACTORY.values(), element );
		Element o = (Element)typeMapper.toJava( value );
		Assert.assertEquals( "Not equal", element, o );
		int k = 0;
	}

}
