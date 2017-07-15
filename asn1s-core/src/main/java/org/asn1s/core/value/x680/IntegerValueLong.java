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

public final class IntegerValueLong implements IntegerValue
{

	private static final long MAX_VALUE_FOR_INT = Integer.MAX_VALUE;
	private static final long MIN_VALUE_FOR_INT = Integer.MIN_VALUE;

	public IntegerValueLong( long value )
	{
		this.value = value;
	}

	private final long value;

	@Override
	public boolean isInt()
	{
		return value <= MAX_VALUE_FOR_INT && value >= MIN_VALUE_FOR_INT;
	}

	@Override
	public boolean isLong()
	{
		return true;
	}

	@Override
	public boolean isBigInteger()
	{
		return true;
	}

	@Override
	public boolean isZero()
	{
		return value == 0L;
	}

	@Override
	public int ordinal()
	{
		return 2;
	}

	@Override
	public int asInt()
	{
		if( !isInt() )
			throw new ArithmeticException( "Out of int range" );
		//noinspection NumericCastThatLosesPrecision
		return (int)value;
	}

	@Override
	public long asLong()
	{
		return value;
	}

	@Override
	public BigInteger asBigInteger()
	{
		return BigInteger.valueOf( value );
	}

	@Override
	public IntegerValue negate()
	{
		return new IntegerValueLong( -value );
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.Real )
			return -o.compareTo( this );

		if( o.getKind() == Kind.Integer )
		{
			IntegerValue integerValue = o.toIntegerValue();
			if( integerValue.ordinal() > ordinal() )
				return -integerValue.compareTo( this );

			return Long.compare( asLong(), integerValue.asLong() );
		}

		if( o.getKind() == Kind.Name )
			return CoreUtils.compareNumberToNamed( this, o.toNamedValue() );

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof IntegerValueLong ) ) return false;

		IntegerValue valueLong = (IntegerValue)obj;

		return asLong() == valueLong.asLong();
	}

	@Override
	public int hashCode()
	{
		return (int)( value ^ ( value >>> 32 ) );
	}

	@Override
	public String toString()
	{
		return Long.toString( value ) + 'L';
	}
}
