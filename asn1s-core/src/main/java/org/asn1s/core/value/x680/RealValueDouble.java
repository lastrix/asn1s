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

import org.asn1s.api.util.NRxUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.IntegerValue;
import org.asn1s.api.value.x680.RealValue;
import org.asn1s.core.CoreUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public final class RealValueDouble implements RealValue
{
	public static final RealValue INFINITY_POSITIVE = new RealValueDouble( Double.POSITIVE_INFINITY );
	public static final RealValue INFINITY_NEGATIVE = new RealValueDouble( Double.NEGATIVE_INFINITY );
	public static final RealValue NAN = new RealValueDouble( Double.NaN );
	public static final RealValue ZERO = new RealValueDouble( 0.0d );
	public static final RealValue MINUS_ZERO = new RealValueDouble( -0.0d );

	public RealValueDouble( double value )
	{
		this.value = value;
	}

	private final double value;

	@Override
	public boolean isFloat()
	{
		return false;
	}

	@Override
	public boolean isDouble()
	{
		return true;
	}

	@Override
	public boolean isBigDecimal()
	{
		return true;
	}

	@Override
	public int ordinal()
	{
		return 4;
	}

	@Override
	public float asFloat()
	{
		throw new ArithmeticException( "Conversion to float prohibited" );
	}

	@Override
	public double asDouble()
	{
		return value;
	}

	@Override
	public BigDecimal asBigDecimal()
	{
		return BigDecimal.valueOf( value );
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.REAL )
		{
			RealValue realValue = o.toRealValue();
			if( realValue.ordinal() > ordinal() )
				return -realValue.compareTo( this );
			return Double.compare( asDouble(), realValue.asDouble() );
		}

		if( o.getKind() == Kind.INTEGER )
		{
			IntegerValue integerValue = o.toIntegerValue();
			if( integerValue.ordinal() > ordinal() )
				return asBigDecimal().compareTo( integerValue.asBigDecimal() );

			return Double.compare( asDouble(), integerValue.asDouble() );
		}

		if( o.getKind() == Kind.NAME )
			return CoreUtils.compareNumberToNamed( this, o.toNamedValue() );

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof RealValueDouble ) ) return false;

		RealValue valueDouble = (RealValue)obj;

		return Double.compare( valueDouble.asDouble(), asDouble() ) == 0;
	}

	@Override
	public int hashCode()
	{
		long temp = Double.doubleToLongBits( value );
		return (int)( temp ^ ( temp >>> 32 ) );
	}

	@Override
	public String toString()
	{
		return NRxUtils.toCanonicalNR3( Double.toString( value ) ) + 'd';
	}
}
