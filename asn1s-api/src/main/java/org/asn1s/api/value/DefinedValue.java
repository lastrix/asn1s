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

import org.asn1s.api.Disposable;
import org.asn1s.api.Ref;
import org.asn1s.api.Scoped;
import org.asn1s.api.Validation;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.x680.*;
import org.asn1s.api.value.x681.ObjectValue;

public interface DefinedValue extends Value, Validation, Disposable, Scoped
{
	String getName();

	Type getType();

	Value getValue();

	Ref<Value> toRef();

	/**
	 * Returns true if this value is template
	 *
	 * @return boolean
	 */
	default boolean isTemplate()
	{
		return false;
	}

	@Override
	default BooleanValue toBooleanValue()
	{
		return getValue().toBooleanValue();
	}

	@Override
	default IntegerValue toIntegerValue()
	{
		return getValue().toIntegerValue();
	}

	@Override
	default RealValue toRealValue()
	{
		return getValue().toRealValue();
	}

	@Override
	default NullValue toNullValue()
	{
		return getValue().toNullValue();
	}

	@Override
	default NamedValue toNamedValue()
	{
		return getValue().toNamedValue();
	}

	@Override
	default ValueCollection toValueCollection()
	{
		return getValue().toValueCollection();
	}

	@Override
	default StringValue toStringValue()
	{
		return getValue().toStringValue();
	}

	@Override
	default ByteArrayValue toByteArrayValue()
	{
		return getValue().toByteArrayValue();
	}

	@Override
	default DateValue toDateValue()
	{
		return getValue().toDateValue();
	}

	@Override
	default ObjectValue toObjectValue()
	{
		return getValue().toObjectValue();
	}

	@Override
	default ObjectIdentifierValue toObjectIdentifierValue()
	{
		return getValue().toObjectIdentifierValue();
	}

	@Override
	default OpenTypeValue toOpenTypeValue()
	{
		return getValue().toOpenTypeValue();
	}

}
