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

package org.asn1s.api.value.x681;

import org.asn1s.api.Ref;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class ObjectValue implements Value
{
	public ObjectValue( Map<String, Ref<?>> fields )
	{
		this.fields = new HashMap<>( fields );
	}

	private final Map<String, Ref<?>> fields;

	public Map<String, Ref<?>> getFields()
	{
		return Collections.unmodifiableMap( fields );
	}

	@SuppressWarnings( "unchecked" )
	@Nullable
	public <T> Ref<T> getField( @NotNull String name )
	{
		return (Ref<T>)fields.get( name );
	}

	@NotNull
	@Override
	public Kind getKind()
	{
		return Kind.OBJECT;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.OBJECT )
			return compareToObjectValue( o.toObjectValue() );

		return getKind().compareTo( o.getKind() );
	}

	private int compareToObjectValue( ObjectValue objectValue )
	{
		Map<String, Ref<?>> rhsFields = objectValue.getFields();
		int result = Integer.compare( fields.size(), rhsFields.size() );
		if( result != 0 )
			return result;

		List<String> lhs = new ArrayList<>( fields.keySet() );
		Collections.sort( lhs );
		List<String> rhs = new ArrayList<>( rhsFields.keySet() );
		Collections.sort( rhs );
		result = lhs.toString().compareTo( rhs.toString() );
		if( result != 0 )
			return result;

		for( Entry<String, Ref<?>> entry : fields.entrySet() )
		{
			Ref<?> ref = rhsFields.get( entry.getKey() );

			if( ref instanceof Value && entry.getValue() instanceof Value )
			{
				//noinspection OverlyStrongTypeCast
				result = ( (Value)entry.getValue() ).compareTo( (Value)ref );
			}
			else
				result = entry.getValue().toString().compareTo( ref.toString() );

			if( result != 0 )
				return result;
		}

		return 0;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "{ " );
		boolean first = true;
		for( Entry<String, Ref<?>> entry : fields.entrySet() )
		{
			if( first )
				first = false;
			else
				sb.append( ", " );

			sb.append( entry.getKey() ).append( ' ' ).append( entry.getValue() );
		}
		sb.append( " }" );
		return sb.toString();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof ObjectValue ) ) return false;

		ObjectValue objectValue = (ObjectValue)obj;

		return getFields().equals( objectValue.getFields() );
	}

	@Override
	public int hashCode()
	{
		return getFields().hashCode();
	}
}
