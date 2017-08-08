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

package org.asn1s.databind;

import org.asn1s.annotation.Asn1Enumeration;
import org.asn1s.annotation.Asn1EnumerationItem;
import org.asn1s.core.DefaultAsn1Factory;
import org.junit.Test;

public class Asn1MapperTest
{
	@Test
	public void testCreate0() throws Exception
	{
		DefaultAsn1Factory factory = new DefaultAsn1Factory();
		new Asn1Mapper( factory );
	}

	@Test
	public void testCreate1() throws Exception
	{
		DefaultAsn1Factory factory = new DefaultAsn1Factory();
		new Asn1Mapper( factory, new Class<?>[]{Values.class} );
	}

	@Test
	public void testCreate2() throws Exception
	{
		DefaultAsn1Factory factory = new DefaultAsn1Factory();
		new Asn1Mapper( factory, "My-Module", new Class<?>[]{Values.class, ValuesWithDefaultNaming.class} );
	}

	@Asn1Enumeration( name = "Values" )
	public enum Values
	{
		@Asn1EnumerationItem( name = "a", value = 2 )
		VALUE1,
		@Asn1EnumerationItem( name = "b", value = 1 )
		VALUE2,
		@Asn1EnumerationItem( name = "c", value = 3, extension = true )
		VALUE3
	}

	@Asn1Enumeration
	public enum ValuesWithDefaultNaming
	{
		@Asn1EnumerationItem
		VALUE1,
		@Asn1EnumerationItem
		VALUE2,
		@Asn1EnumerationItem
		VALUE3
	}
}
