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

package org.asn1s.api.value.x680;

import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * X.680, p 12.8
 * Any integer literal
 */
public interface IntegerValue extends Value
{
	/**
	 * Returns true if value may be converted to int
	 *
	 * @return boolean
	 */
	boolean isInt();

	/**
	 * Returns true if value may be converted to long
	 *
	 * @return boolean
	 */
	boolean isLong();

	/**
	 * Returns true if value may be converted to BigInteger
	 *
	 * @return boolean
	 */
	boolean isBigInteger();

	default boolean isFloat()
	{
		return ordinal() < 3;
	}

	default boolean isDouble()
	{
		return ordinal() < 3;
	}

	boolean isZero();

	/**
	 * Type ordinal value.
	 * Int - 1
	 * Long - 2
	 * BigInteger - 10
	 *
	 * @return int
	 */
	int ordinal();

	/**
	 * Convert value to int
	 * Must throw exception if isInt() returns false
	 *
	 * @return int
	 */
	int asInt();

	/**
	 * Convert value to long
	 * Must throw exception if isLong() returns false
	 *
	 * @return long
	 */
	long asLong();

	/**
	 * Convert value to BigInteger
	 * Must throw exception if isBigInteger() returns false
	 *
	 * @return BigInteger
	 */
	BigInteger asBigInteger();

	IntegerValue negate();

	default float asFloat()
	{
		if( isInt() )
			return asInt();

		if( isLong() )
			return asLong();

		throw new UnsupportedOperationException();
	}

	default double asDouble()
	{
		if( isLong() )
			return asLong();

		throw new UnsupportedOperationException();
	}

	default BigDecimal asBigDecimal()
	{
		if( isLong() )
			return new BigDecimal( asLong() );
		return new BigDecimal( asBigInteger() );
	}

	@NotNull
	@Override
	default Kind getKind()
	{
		return Kind.INTEGER;
	}
}
