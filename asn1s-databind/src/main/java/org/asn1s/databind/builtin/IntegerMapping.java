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

import org.asn1s.api.UniversalType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;

import java.math.BigInteger;

public enum IntegerMapping implements BuiltinMapping
{
	BYTE( byte.class, "Byte", Byte.MIN_VALUE, Byte.MAX_VALUE ),
	BYTE_CLASS( Byte.class, "Byte", Byte.MIN_VALUE, Byte.MAX_VALUE ),
	SHORT( short.class, "Short", Short.MIN_VALUE, Short.MAX_VALUE ),
	SHORT_CLASS( Short.class, "Short", Short.MIN_VALUE, Short.MAX_VALUE ),
	INTEGER( int.class, "Int", Integer.MIN_VALUE, Integer.MAX_VALUE ),
	INTEGER_CLASS( Integer.class, "Int", Integer.MIN_VALUE, Integer.MAX_VALUE ),
	LONG( long.class, "Long", Long.MIN_VALUE, Long.MAX_VALUE ),
	LONG_CLASS( Long.class, "Long", Long.MIN_VALUE, Long.MAX_VALUE ),
	BIG_INTEGER( BigInteger.class, null, null, null );

	private final Class<?> javaType;
	private final String asnTypeName;
	private final String minValue;
	private final String maxValue;

	IntegerMapping( Class<?> javaType, String asnTypeName, Object minValue, Object maxValue )
	{
		this( javaType, asnTypeName, String.valueOf( minValue ), String.valueOf( maxValue ) );
	}

	IntegerMapping( Class<?> javaType, String asnTypeName, String minValue, String maxValue )
	{
		this.javaType = javaType;
		this.asnTypeName = asnTypeName;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public Class<?> getJavaType()
	{
		return javaType;
	}

	@Override
	public String getAsnTypeName()
	{
		return asnTypeName;
	}

	@Override
	public UniversalType getUniversalType()
	{
		return UniversalType.INTEGER;
	}

	@Override
	public Value getMinValue( ValueFactory factory )
	{
		return factory.integer( minValue );
	}

	@Override
	public Value getMaxValue( ValueFactory factory )
	{
		return factory.integer( maxValue );
	}

	@Override
	public boolean hasConstraint()
	{
		return minValue != null && maxValue != null;
	}

	@Override
	public boolean isRegisterAsDefault()
	{
		return this == BIG_INTEGER;
	}

}
