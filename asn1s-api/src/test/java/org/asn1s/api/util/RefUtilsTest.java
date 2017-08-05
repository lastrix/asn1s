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

package org.asn1s.api.util;

import org.asn1s.api.Ref;
import org.asn1s.api.type.AbstractComponentType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RefUtilsTest
{
	@Test( expected = IllegalArgumentException.class )
	public void testNotTypeRef()
	{
		RefUtils.assertTypeRef( "a" );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testNotValueRef()
	{
		RefUtils.assertValueRef( "A" );
	}

	@Test
	public void testSameAsDefault() throws Exception
	{
		MyIntegerValue value = new MyIntegerValue();
		assertTrue( "Must be true", RefUtils.isSameAsDefaultValue( new MyScope(), new MyAbstractComponentType( value ), value ) );
	}

	private static class MyAbstractComponentType extends AbstractComponentType
	{
		private final MyIntegerValue value;

		MyAbstractComponentType( MyIntegerValue value )
		{
			super( 0, "a", new TypeNameRef( "A" ) );
			this.value = value;
		}

		@Nullable
		@Override
		public Value getDefaultValue()
		{
			return value;
		}

		@Nullable
		@Override
		public Ref<Value> getDefaultValueRef()
		{
			return value;
		}

		@Override
		public void setDefaultValueRef( Ref<Value> ref )
		{

		}

		@NotNull
		@Override
		public ComponentType copy()
		{
			throw new UnsupportedOperationException();
		}
	}
}
