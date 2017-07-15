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
import org.asn1s.api.value.x680.DateValue;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class DateValueImpl implements DateValue
{
	public DateValueImpl( Instant value )
	{
		this.value = value;
	}

	private final Instant value;

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.Time
				|| o.getKind() == Kind.Name && o.toNamedValue().getReferenceKind() == Kind.Time )
			return value.compareTo( o.toDateValue().asInstant() );

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public Instant asInstant()
	{
		return value;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof DateValue ) ) return false;

		DateValue dateValue = (DateValue)obj;

		return asInstant().equals( dateValue.asInstant() );
	}

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}

	@Override
	public String toString()
	{
		return value.toString();
	}
}
