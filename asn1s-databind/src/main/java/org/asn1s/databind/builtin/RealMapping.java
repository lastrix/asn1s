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

import java.math.BigDecimal;

public enum RealMapping implements BuiltinMapping
{
	FLOAT( float.class, "Float" ),
	FLOAT_CLASS( Float.class, "Float" ),
	DOUBLE( double.class, "Double" ),
	DOUBLE_CLASS( Double.class, "Double" ),
	BIG_DECIMAL( BigDecimal.class, null );

	private final Class<?> javaType;
	private final String asnTypeName;

	RealMapping( Class<?> javaType, String asnTypeName )
	{
		this.javaType = javaType;
		this.asnTypeName = asnTypeName;
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
		return UniversalType.REAL;
	}

	@Override
	public boolean isRegisterAsDefault()
	{
		return this == BIG_DECIMAL;
	}

}
