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
import org.asn1s.api.value.x680.StringValue;
import org.jetbrains.annotations.NotNull;

public final class StringValueImpl implements StringValue
{
	public StringValueImpl( @NotNull String value )
	{
		this.value = value;
	}

	@NotNull
	private final String value;

	@Override
	public String asString()
	{
		return value;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.C_STRING )
			return asString().compareTo( o.toStringValue().asString() );

		if( o.getKind() == Kind.TIME )
			return -o.compareTo( this );

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public int length()
	{
		return value.length();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof StringValueImpl ) ) return false;

		StringValue stringValue = (StringValue)obj;
		return asString().equals( stringValue.asString() );
	}

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	@Override
	public String toString()
	{
		return '"' + value + '"';
	}
}
