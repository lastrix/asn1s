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

package org.asn1s.api.value;

import org.asn1s.api.Ref;
import org.asn1s.api.value.x680.*;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNestingValue implements Value
{
	protected AbstractNestingValue( @Nullable Ref<Value> valueRef )
	{
		this.valueRef = valueRef;
	}

	private final Ref<Value> valueRef;

	@Nullable
	public Ref<Value> getValueRef()
	{
		return valueRef;
	}

	@Override
	public BooleanValue toBooleanValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toBooleanValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public IntegerValue toIntegerValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toIntegerValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public RealValue toRealValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toRealValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public NullValue toNullValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toNullValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public NamedValue toNamedValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toNamedValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public ValueCollection toValueCollection()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toValueCollection();

		throw new UnsupportedOperationException();
	}

	@Override
	public ByteArrayValue toByteArrayValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toByteArrayValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public StringValue toStringValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toStringValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public DateValue toDateValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toDateValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectValue toObjectValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toObjectValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectIdentifierValue toObjectIdentifierValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toObjectIdentifierValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public OpenTypeValue toOpenTypeValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toOpenTypeValue();

		throw new UnsupportedOperationException();
	}
}
