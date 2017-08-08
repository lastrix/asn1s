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

package org.asn1s.databind.builtin;

import org.asn1s.api.type.NamedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.IntegerValue;
import org.asn1s.databind.TypeMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Objects;

public final class IntegerTypeMapper implements TypeMapper
{
	public IntegerTypeMapper( Class<?> integerClass, NamedType asnType )
	{
		this.integerClass = integerClass;
		assert isByte() || isShort() || isInteger() || isLong() || isBigInteger();
		this.asnType = asnType;
	}

	private final Class<?> integerClass;
	private final NamedType asnType;

	@Override
	public Type getJavaType()
	{
		return integerClass;
	}

	@Override
	public NamedType getAsn1Type()
	{
		return asnType;
	}

	@NotNull
	@Override
	public Value toAsn1( @NotNull ValueFactory factory, @NotNull Object value )
	{
		if( isByte() && isAssignableToByte( value ) )
			return factory.integer( (Byte)value );

		if( isShort() && isAssignableToShort( value ) )
			return factory.integer( (Short)value );

		if( isInteger() && isAssignableToInt( value ) )
			return factory.integer( (Integer)value );

		if( isLong() && isAssignableToLong( value ) )
			return factory.integer( (Long)value );

		if( isBigInteger() && Objects.equals( value.getClass(), BigInteger.class ) )
			return factory.integer( (BigInteger)value );

		throw new IllegalArgumentException( "Unable to convert value: " + value );
	}

	@NotNull
	@Override
	public Object toJava( @NotNull Value value )
	{
		if( value.getKind() != Kind.INTEGER )
			throw new IllegalArgumentException( "Unable to convert to integer value of kind: " + value.getKind() );

		IntegerValue iv = value.toIntegerValue();
		if( isByte() )
			//noinspection NumericCastThatLosesPrecision
			return (byte)iv.asInt();

		if( isShort() )
			//noinspection NumericCastThatLosesPrecision
			return (short)iv.asInt();

		if( isInteger() )
			return iv.asInt();

		if( isLong() )
			return iv.asLong();

		if( isBigInteger() )
			return iv.asBigInteger();

		// not possible
		throw new UnsupportedOperationException();
	}

	private boolean isBigInteger()
	{
		return Objects.equals( integerClass, BigInteger.class );
	}

	private boolean isLong()
	{
		return Objects.equals( integerClass, Long.class ) || Objects.equals( integerClass, long.class );
	}

	private boolean isInteger()
	{
		return Objects.equals( integerClass, Integer.class ) || Objects.equals( integerClass, int.class );
	}

	private boolean isShort()
	{
		return Objects.equals( integerClass, Short.class ) || Objects.equals( integerClass, short.class );
	}

	private boolean isByte()
	{
		return Objects.equals( integerClass, Byte.class ) || Objects.equals( integerClass, byte.class );
	}

	private static boolean isAssignableToLong( Object value )
	{
		Class<?> aClass = value.getClass();
		return Objects.equals( aClass, long.class ) || Objects.equals( aClass, Long.class );
	}

	private static boolean isAssignableToInt( Object value )
	{
		Class<?> aClass = value.getClass();
		return Objects.equals( aClass, int.class ) || Objects.equals( aClass, Integer.class );
	}

	private static boolean isAssignableToShort( Object value )
	{
		Class<?> aClass = value.getClass();
		return Objects.equals( aClass, short.class ) || Objects.equals( aClass, Short.class );
	}

	private static boolean isAssignableToByte( Object value )
	{
		Class<?> aClass = value.getClass();
		return Objects.equals( aClass, byte.class ) || Objects.equals( aClass, Byte.class );
	}
}
