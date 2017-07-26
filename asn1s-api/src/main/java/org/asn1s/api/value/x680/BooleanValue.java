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

/**
 * X.680, p
 * Boolean value
 */
public final class BooleanValue implements Value
{
	public static final Value TRUE = new BooleanValue( Boolean.TRUE );
	public static final Value FALSE = new BooleanValue( Boolean.FALSE );

	private BooleanValue( @NotNull Boolean value )
	{
		this.value = value;
	}

	private final Boolean value;

	public boolean asBoolean()
	{
		return value;
	}

	@NotNull
	@Override
	public Kind getKind()
	{
		return Kind.BOOLEAN;
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj == this || obj instanceof BooleanValue && value.equals( ( (BooleanValue)obj ).asBoolean() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.BOOLEAN )
			return Boolean.compare( value, ( (BooleanValue)o ).asBoolean() );

		if( o.getKind() == Kind.NAME && o.toNamedValue().getReferenceKind() == Kind.BOOLEAN )
			return compareTo( o.toNamedValue().toBooleanValue() );

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public String toString()
	{
		return value ? "TRUE" : "FALSE";
	}
}
