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

package org.asn1s.core.type;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractBuiltinTypeWithNamedValues extends BuiltinType
{
	protected AbstractBuiltinTypeWithNamedValues( @Nullable Collection<NamedValue> values )
	{
		this.values = values == null ? null : new ArrayList<>( values );
	}

	private final List<NamedValue> values;
	private List<NamedValue> actualValues;

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		if( actualValues == null )
			return null;

		for( NamedValue value : actualValues )
			if( name.equals( value.getName() ) )
				return value;

		return null;
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return actualValues == null ? Collections.emptyList() : Collections.unmodifiableCollection( actualValues );
	}

	@Override
	public String toString()
	{
		if( values == null )
			return getFamily().name();

		return getFamily().name() + " { " + StringUtils.join( values, ", " ) + " }";
	}

	@Nullable
	public List<NamedValue> getValues()
	{
		return values == null ? null : Collections.unmodifiableList( values );
	}

	protected void setActualValues( @NotNull List<NamedValue> actualValues )
	{
		this.actualValues = new ArrayList<>( actualValues );
	}

	@Override
	protected void onDispose()
	{
		if( values != null )
			values.clear();

		if( actualValues != null )
		{
			actualValues.clear();
			actualValues = null;
		}
	}
}
