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
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Date;

public enum DateMapping implements BuiltinMapping
{
	INSTANT_UTC( Instant.class, UniversalType.UTC_TIME ),
	INSTANT_G( Instant.class, UniversalType.GENERALIZED_TIME ),
	DATE_UTC( Date.class, UniversalType.UTC_TIME ),
	DATE_G( Date.class, UniversalType.GENERALIZED_TIME );

	private final Class<?> javaType;
	private final UniversalType universalType;

	DateMapping( Class<?> javaType, UniversalType universalType )
	{
		this.javaType = javaType;
		this.universalType = universalType;
	}

	@Override
	public Class<?> getJavaType()
	{
		return javaType;
	}

	@Nullable
	@Override
	public String getAsnTypeName()
	{
		return null;
	}

	@Override
	public UniversalType getUniversalType()
	{
		return universalType;
	}

	@Override
	public boolean isRegisterAsDefault()
	{
		return this == INSTANT_G || this == DATE_G;
	}
}
