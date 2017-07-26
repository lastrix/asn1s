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

package org.asn1s.core.value.x680;

import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.IntegerValue;
import org.asn1s.core.CoreUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public final class IntegerValueBig implements IntegerValue
{

	private static final int BITS_FOR_INTEGER = 32;
	private static final int BITS_FOR_LONG = 64;

	public IntegerValueBig( @NotNull BigInteger value )
	{
		this.value = value;
	}

	@NotNull
	private final BigInteger value;

	@Override
	public boolean isInt()
	{
		return value.bitCount() < BITS_FOR_INTEGER;
	}

	@Override
	public boolean isLong()
	{
		return value.bitCount() < BITS_FOR_LONG;
	}

	@Override
	public boolean isBigInteger()
	{
		return true;
	}

	@Override
	public boolean isZero()
	{
		return value.equals( BigInteger.ZERO );
	}

	@Override
	public int ordinal()
	{
		return 10;
	}

	@Override
	public int asInt()
	{
		return value.intValueExact();
	}

	@Override
	public long asLong()
	{
		return value.longValueExact();
	}

	@Override
	public BigInteger asBigInteger()
	{
		return value;
	}

	@Override
	public IntegerValue negate()
	{
		return new IntegerValueBig( value.negate() );
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.REAL )
			return -o.compareTo( this );

		if( o.getKind() == Kind.INTEGER )
			return asBigInteger().compareTo( o.toIntegerValue().asBigInteger() );

		if( o.getKind() == Kind.NAME )
			return CoreUtils.compareNumberToNamed( this, o.toNamedValue() );

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof IntegerValueBig ) ) return false;

		IntegerValue integerBig = (IntegerValue)obj;
		return asBigInteger().equals( integerBig.asBigInteger() );
	}

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	@Override
	public String toString()
	{
		return value.toString();
	}
}
