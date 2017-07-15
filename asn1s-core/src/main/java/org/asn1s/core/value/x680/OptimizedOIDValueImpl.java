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
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ObjectIdentifierValue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class OptimizedOIDValueImpl implements ObjectIdentifierValue
{
	public OptimizedOIDValueImpl( List<NamedValue> namedValues )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.namedValues = namedValues;
	}

	private final List<NamedValue> namedValues;

	@Override
	public Long[] asIDArray()
	{
		Long[] result = new Long[namedValues.size()];
		int i = 0;
		for( NamedValue value : namedValues )
		{
			result[i] = value.toIntegerValue().asLong();
			i++;
		}
		return result;
	}

	@Override
	public List<NamedValue> asNamedValueList()
	{
		return Collections.unmodifiableList( namedValues );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof OptimizedOIDValueImpl ) ) return false;

		ObjectIdentifierValue oidValue = (ObjectIdentifierValue)obj;
		return Arrays.equals( asIDArray(), oidValue.asIDArray() );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( asIDArray() );
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.Oid )
			return compareArrays( asIDArray(), o.toObjectIdentifierValue().asIDArray() );

		return getKind().compareTo( o.getKind() );
	}

	private static int compareArrays( Long[] lhs, Long[] rhs )
	{
		int result = Integer.compare( lhs.length, rhs.length );
		if( result != 0 )
			return result;

		for( int i = 0; i < lhs.length; i++ )
		{
			result = Long.compare( lhs[i], rhs[i] );
			if( result != 0 )
				return result;
		}
		return 0;
	}

	@Override
	public String toString()
	{
		return '{' + StringUtils.join( asIDArray(), " " ) + '}';
	}

}
