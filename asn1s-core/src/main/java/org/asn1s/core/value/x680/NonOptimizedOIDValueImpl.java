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

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ObjectIdentifierValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class NonOptimizedOIDValueImpl implements ObjectIdentifierValue
{
	public NonOptimizedOIDValueImpl( @NotNull List<Ref<Value>> oidRefs )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.oidRefs = oidRefs;
	}

	private final List<Ref<Value>> oidRefs;

	@Override
	public Long[] asIDArray()
	{
		throw new UnsupportedOperationException( "Not optimized" );
	}

	@Override
	public List<NamedValue> asNamedValueList()
	{
		throw new UnsupportedOperationException( "Not optimized" );
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		throw new UnsupportedOperationException( "Not optimized" );
	}

	public List<Ref<Value>> getOidRefs()
	{
		return Collections.unmodifiableList( oidRefs );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof NonOptimizedOIDValueImpl ) ) return false;

		NonOptimizedOIDValueImpl value = (NonOptimizedOIDValueImpl)obj;

		return getOidRefs().equals( value.getOidRefs() );
	}

	@Override
	public int hashCode()
	{
		return getOidRefs().hashCode();
	}

	@Override
	public String toString()
	{
		return '{' + StringUtils.join( oidRefs, " " ) + '}';
	}
}
