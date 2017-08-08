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

public enum StringMapping implements BuiltinMapping
{
	UTF8_STRING( UniversalType.UTF8_STRING ),
	NUMERIC_STRING( UniversalType.NUMERIC_STRING ),
	PRINTABLE_STRING( UniversalType.PRINTABLE_STRING ),
	T61_STRING( UniversalType.T61_STRING ),
	TELETEX( UniversalType.TELETEX ),
	VIDEOTEX_STRING( UniversalType.VIDEOTEX_STRING ),
	IA5_STRING( UniversalType.IA5_STRING ),
	GRAPHIC_STRING( UniversalType.GRAPHIC_STRING ),
	VISIBLE_STRING( UniversalType.VISIBLE_STRING ),
	ISO_646_STRING( UniversalType.ISO_646_STRING ),
	GENERAL_STRING( UniversalType.GENERAL_STRING ),
	UNIVERSAL_STRING( UniversalType.UNIVERSAL_STRING ),
	BMP_STRING( UniversalType.BMP_STRING );

	private final UniversalType universalType;

	StringMapping( UniversalType universalType )
	{
		this.universalType = universalType;
	}

	@Override
	public Class<?> getJavaType()
	{
		return String.class;
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
		return this == UTF8_STRING;
	}
}
