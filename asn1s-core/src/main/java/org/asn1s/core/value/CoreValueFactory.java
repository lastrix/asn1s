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

package org.asn1s.core.value;

import org.asn1s.api.Ref;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.NRxUtils;
import org.asn1s.api.value.ByteArrayValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.*;
import org.asn1s.core.CoreUtils;
import org.asn1s.core.value.x680.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

public class CoreValueFactory implements ValueFactory
{
	private static final long MASK_DOUBLE_MANTISSA = 0X000FFFFFFFFFFFFFL;
	private static final long MASK_DOUBLE_ONE_BIT = 0x0010000000000000L;
	private static final int MASK_DOUBLE_EXPONENT = 0x7FF;
	private static final int DOUBLE_EXP_SHIFT = 1023 + 52;
	private static final MathContext MATH_CONTEXT = new MathContext( 10000, RoundingMode.HALF_DOWN );
	private static final String MINUS_INFINITY = "MINUS-INFINITY";
	private static final String PLUS_INFINITY = "PLUS-INFINITY";
	private static final String NOT_A_NUMBER = "NOT-A-NUMBER";
	private static final ByteArrayValue EMPTY_ARRAY = new ByteArrayValueImpl( 0, new byte[0] );
	private static final IntegerValue INT_ZERO = new IntegerValueInt( 0 );
	private static final int BYTE_MASK = 0xFF;
	private static final long BYTE_MASK_LONG = 0xFFL;
	private static final int MANTISSA_BIT_COUNT = 52;

	@NotNull
	@Override
	public RealValue rZero()
	{
		return RealValueDouble.ZERO;
	}

	@NotNull
	@Override
	public RealValue rNegativeZero()
	{
		return RealValueDouble.MINUS_ZERO;
	}

	@NotNull
	@Override
	public RealValue rPositiveInfinity()
	{
		return RealValueDouble.INFINITY_POSITIVE;
	}

	@NotNull
	@Override
	public RealValue rNegativeInfinity()
	{
		return RealValueDouble.INFINITY_NEGATIVE;
	}

	@NotNull
	@Override
	public RealValue rNan()
	{
		return RealValueDouble.NAN;
	}

	@NotNull
	@Override
	public RealValue real( @NotNull String value )
	{
		switch( value )
		{
			case MINUS_INFINITY:
				return rNegativeInfinity();

			case PLUS_INFINITY:
				return rPositiveInfinity();

			case NOT_A_NUMBER:
				return rNan();

			default:
				// do nothing
		}

		try
		{
			float v = Float.parseFloat( value );
			if( NRxUtils.toCanonicalNR3( Float.toString( v ) ).equals( NRxUtils.toCanonicalNR3( value ) ) )
				return real( v );
		} catch( NumberFormatException ignored )
		{

		}

		try
		{
			double v = Double.parseDouble( value );
			if( NRxUtils.toCanonicalNR3( Double.toString( v ) ).equals( NRxUtils.toCanonicalNR3( value ) ) )
				return real( v );
		} catch( NumberFormatException ignored )
		{

		}

		try
		{
			return real( new BigDecimal( value ) );
		} catch( NumberFormatException ignored )
		{

		}

		throw new IllegalArgumentException( "Unable to parse real value: " + value );
	}

	@NotNull
	@Override
	public RealValue real( float value )
	{
		return new RealValueFloat( value );
	}

	@NotNull
	@Override
	public RealValue real( double value )
	{
		return new RealValueDouble( value );
	}

	@NotNull
	@Override
	public RealValue real( @NotNull BigDecimal value )
	{
		return new RealValueBig( value );
	}

	@NotNull
	@Override
	public RealValue real( long mantissa, boolean decimal, int exponent, boolean negative )
	{
		return createReal( mantissa, decimal, exponent, negative );
	}

	@NotNull
	@Override
	public RealValue real( BigInteger mantissa, boolean decimal, int exponent, boolean negative )
	{
		return createReal( mantissa, decimal, exponent, negative );
	}

	@NotNull
	@Override
	public RealValue real( @NotNull IntegerValue mantissa, boolean decimal, @NotNull IntegerValue exponent, boolean negative )
	{
		if( !exponent.isLong() )
			throw new IllegalArgumentException( "Exponent is too big to handle" );

		int e;
		if( exponent.isInt() )
			e = exponent.asInt();
		else if( exponent.isLong() )
		{
			long le = exponent.asLong();
			if( le > Integer.MAX_VALUE || le < Integer.MIN_VALUE )
				throw new IllegalArgumentException( "Exponent overflow" );
			//noinspection NumericCastThatLosesPrecision
			e = (int)le;
		}
		else
		{
			BigInteger bi = exponent.asBigInteger();
			e = bi.intValue();
		}

		if( mantissa.isLong() )
			return real( mantissa.asLong(), decimal, e, negative );

		return real( mantissa.asBigInteger(), decimal, e, negative );
	}

	@NotNull
	@Override
	public IntegerValue integer( @NotNull byte[] bytes )
	{
		if( bytes.length == 0 )
			return INT_ZERO;

		if( bytes.length <= 4 )
			return integer( bytesToIntLe( bytes ) );

		if( bytes.length <= 8 )
			return integer( bytesToLongLe( bytes ) );

		return integer( new BigInteger( bytes ) );
	}

	private static int bytesToIntLe( byte[] bytes )
	{
		int result = bytes[0] < 0 ? -1 : 0;
		for( byte part : bytes )
			result = ( ( result << 8 ) & ~BYTE_MASK ) | part & BYTE_MASK;

		return result;
	}

	private static long bytesToLongLe( byte[] bytes )
	{
		long result = bytes[0] < 0 ? -1 : 0L;
		for( byte part : bytes )
			//noinspection MagicNumber
			result = ( ( result << 8L ) & ~BYTE_MASK_LONG ) | part & BYTE_MASK_LONG;

		return result;
	}

	@NotNull
	@Override
	public IntegerValue integer( @NotNull String value )
	{
		try
		{
			return integer( Integer.parseInt( value ) );
		} catch( NumberFormatException ignored )
		{

		}

		try
		{
			return integer( Long.parseLong( value ) );
		} catch( NumberFormatException ignored )
		{

		}

		try
		{
			return integer( new BigInteger( value ) );
		} catch( NumberFormatException ignored )
		{

		}
		throw new IllegalArgumentException( "Unable to parse integer value: " + value );
	}

	@NotNull
	@Override
	public IntegerValue integer( int value )
	{
		return new IntegerValueInt( value );
	}

	@NotNull
	@Override
	public IntegerValue integer( long value )
	{
		return new IntegerValueLong( value );
	}

	@NotNull
	@Override
	public IntegerValue integer( @NotNull BigInteger value )
	{
		return new IntegerValueBig( value );
	}

	@NotNull
	@Override
	public StringValue cString( @NotNull String value )
	{
		if( value.startsWith( "\"" ) && value.endsWith( "\"" ) || value.startsWith( "'" ) && value.endsWith( "'" ) )
			value = value.substring( 1, value.length() - 1 );
		return new StringValueImpl( value );
	}

	@NotNull
	@Override
	public NamedValue named( @NotNull String name, @Nullable Ref<Value> valueRef )
	{
		return new NamedValueImpl( name, valueRef );
	}

	@NotNull
	@Override
	public ValueCollection collection( boolean named )
	{
		return new ValueCollectionImpl( named );
	}

	@NotNull
	@Override
	public ByteArrayValue hString( @NotNull String content )
	{
		return CoreUtils.byteArrayFromHexString( content );
	}

	@NotNull
	@Override
	public ByteArrayValue bString( @NotNull String content )
	{
		return CoreUtils.byteArrayFromBitString( content );
	}

	@NotNull
	@Override
	public ByteArrayValue byteArrayValue( int bits, @Nullable byte[] bytes )
	{
		return new ByteArrayValueImpl( bits, bytes );
	}

	@NotNull
	@Override
	public ByteArrayValue emptyByteArray()
	{
		return EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public OpenTypeValue openTypeValue( @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef )
	{
		return new OpenTypeValueImpl( typeRef, valueRef );
	}

	@NotNull
	@Override
	public DateValue timeValue( @NotNull Instant instant )
	{
		return new DateValueImpl( instant );
	}

	@NotNull
	@Override
	public ObjectIdentifierValue objectIdentifier( @NotNull List<Ref<Value>> oidRefs )
	{
		return new NonOptimizedOIDValueImpl( oidRefs );
	}

	/**
	 * Create real value from components
	 *
	 * @param mantissa the mantissa value
	 * @param decimal  if true - decimal notation, binary otherwise
	 * @param exponent an integer exponent
	 * @return RealValue
	 */
	@NotNull
	public static RealValue createReal( long mantissa, boolean decimal, int exponent, boolean negative )
	{
		if( decimal )
			return new RealValueBig( new BigDecimal( mantissa ).multiply( new BigDecimal( 10 ).pow( exponent ) ) );
		else
		{
			int e = exponent;
			long scaledMantissa = mantissa;

			if( ( ( e < 0 ? -e : e ) & ~MASK_DOUBLE_EXPONENT ) != 0 || ( scaledMantissa & ~( MASK_DOUBLE_MANTISSA & ~MASK_DOUBLE_ONE_BIT ) ) != 0 )
				return createReal( BigInteger.valueOf( mantissa ), false, exponent, negative );

			if( scaledMantissa == 0 )
				e += DOUBLE_EXP_SHIFT - 1;
			else
			{
				while( ( scaledMantissa & MASK_DOUBLE_MANTISSA ) == 0L )
				{
					scaledMantissa <<= 8;
					e -= 8;
				}

				while( ( scaledMantissa & MASK_DOUBLE_ONE_BIT ) == 0L )
				{
					scaledMantissa <<= 1;
					e--;
				}

				scaledMantissa &= ~MASK_DOUBLE_ONE_BIT;
				e += DOUBLE_EXP_SHIFT;
			}
			if( ( ( e < 0 ? -e : e ) & ~MASK_DOUBLE_EXPONENT ) != 0 || ( scaledMantissa & ~MASK_DOUBLE_MANTISSA ) != 0 )
				return createReal( BigInteger.valueOf( mantissa ), false, exponent, negative );
			else
			{
				double value = Double.longBitsToDouble( (long)e << MANTISSA_BIT_COUNT | scaledMantissa );
				return new RealValueDouble( negative ? -value : value );
			}
		}
	}

	/**
	 * Constructs BigDecimal from parts by arithmetic operations. Precision is 10k digits.
	 * It's highly NOT recommended to use this method unless your values are huge and can not be handled by long
	 *
	 * @param mantissa an integer mantissa
	 * @param decimal  pow base is 10 or 2
	 * @param exponent the exponent in range of [-999999999, 999999999]
	 * @return RealValue that uses BigDecimal
	 */
	@NotNull
	private static RealValue createReal( BigInteger mantissa, boolean decimal, int exponent, boolean negative )
	{
		BigDecimal result = new BigDecimal( mantissa ).multiply( new BigDecimal( decimal ? 10 : 2 ).pow( exponent, MATH_CONTEXT ) );
		if( negative )
			result = result.negate();
		return new RealValueBig( result );
	}
}
