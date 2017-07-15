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
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.value.x680.*;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Logical value representation.
 * Use {@link #getKind()} to find out whether or not you may use
 * methods like {@link #toIntegerValue()} and others by mask: to*Value()
 * </p>
 * <p>
 * If you call a method of any conversion ( to*Value() ) when Kind does not approve it,
 * then you always should get UnsupportedOperationException. Those methods may return other object, such as
 * obj == obj.toIntegerValue() is not always true or false.
 * </p>
 * <p>
 * By default {@link Object#equals(Object)} and {@link Object#hashCode()} must be used if you want to check exact instance.
 * When logical comparison is required, you must always refer to methods such as: {@link #isEqualTo(Value)}
 * and {@link Comparable#compareTo(Object)}
 * </p>
 */
@SuppressWarnings( "ClassReferencesSubclass" )
public interface Value extends Ref<Value>, Comparable<Value>
{
	/**
	 * Kind of value, used for logical separation between implementations
	 *
	 * @return Kind
	 */
	@NotNull
	Kind getKind();

	/**
	 * Returns true if value is logically equal to this.
	 *
	 * @param value the value to compare
	 * @return boolean
	 */
	default boolean isEqualTo( Value value )
	{
		// TODO: may be better impl? Raw comparison is expensive
		return compareTo( value ) == 0;
	}

	default BooleanValue toBooleanValue()
	{
		return (BooleanValue)this;
	}

	default IntegerValue toIntegerValue()
	{
		return (IntegerValue)this;
	}

	default RealValue toRealValue()
	{
		return (RealValue)this;
	}

	default NullValue toNullValue()
	{
		return (NullValue)this;
	}

	default NamedValue toNamedValue()
	{
		return (NamedValue)this;
	}

	default ValueCollection toValueCollection()
	{
		return (ValueCollection)this;
	}

	default StringValue toStringValue()
	{
		return (StringValue)this;
	}

	default DateValue toDateValue()
	{
		return (DateValue)this;
	}

	default ByteArrayValue toByteArrayValue()
	{
		return (ByteArrayValue)this;
	}

	default ObjectValue toObjectValue()
	{
		return (ObjectValue)this;
	}

	default ObjectIdentifierValue toObjectIdentifierValue()
	{
		return (ObjectIdentifierValue)this;
	}

	default OpenTypeValue toOpenTypeValue()
	{
		return (OpenTypeValue)this;
	}

	@Override
	default Value resolve( Scope scope ) throws ResolutionException
	{
		return this;
	}

	enum Kind
	{
		Boolean,
		ByteArray,
		CString,
		Collection,
		Empty,
		Integer,
		Iri,
		Name,
		NamedCollection,
		Null,
		Object,
		Oid,
		OpenType,
		Real,
		TemplateInstance,
		Time
	}
}
