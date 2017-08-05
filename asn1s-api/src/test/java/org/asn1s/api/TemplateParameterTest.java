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

package org.asn1s.api;

import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.value.ValueNameRef;
import org.junit.Test;

import static org.junit.Assert.*;

public class TemplateParameterTest
{
	@Test
	public void testTypeParameter() throws Exception
	{
		TemplateParameter parameter = new TemplateParameter( 0, new TypeNameRef( "A" ), null );
		assertTrue( "Is not type ref", parameter.isTypeRef() );
		assertFalse( "Is value ref", parameter.isValueRef() );
		assertEquals( "Illegal index", 0, parameter.getIndex() );
		assertEquals( "Name is not A", "A", parameter.getName() );
		assertNotNull( "Reference can not be null", parameter.getReference() );
		assertEquals( "Not A", "A", parameter.toString() );
	}

	@Test
	public void testValueParameter() throws Exception
	{
		TemplateParameter parameter = new TemplateParameter( 1, new ValueNameRef( "a" ), new TypeNameRef( "A" ) );
		assertTrue( "Is not value ref", parameter.isValueRef() );
		assertFalse( "Is type ref", parameter.isTypeRef() );
		assertEquals( "Illegal index", 1, parameter.getIndex() );
		assertEquals( "Name is not a", "a", parameter.getName() );
		assertNotNull( "Governor should not be null", parameter.getGovernor() );
		assertEquals( "Not A: a", "A: a", parameter.toString() );
	}

	@Test( expected = IllegalStateException.class )
	public void testIllegalRef() throws Exception
	{
		TemplateParameter parameter = new TemplateParameter(
				0,
				scope -> {
					throw new UnsupportedOperationException();
				}, null );
		parameter.getName();
		fail( "Must fail" );
	}

	@Test
	public void equalityTest()
	{
		TemplateParameter aType = new TemplateParameter( 0, new TypeNameRef( "A" ), null );
		TemplateParameter aValue = new TemplateParameter( 0, new ValueNameRef( "a" ), new TypeNameRef( "A" ) );
		TemplateParameter a2Type = new TemplateParameter( 2, new TypeNameRef( "A" ), null );
		assertEquals( "Must be equal", aType, aValue );
		assertNotEquals( "Must not be equal", a2Type, aValue );
		assertNotEquals( "Must not be equal", aType, a2Type );
		assertEquals( "Must be 0", 0, aType.compareTo( aValue ) );
		assertEquals( "Must be -1", -1, aType.compareTo( a2Type ) );
		assertEquals( "Hashes must be same", aType.hashCode(), aValue.hashCode() );
		assertNotEquals( "Hashes must not be the same", a2Type.hashCode(), aValue.hashCode() );
	}
}
