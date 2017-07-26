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
import org.asn1s.api.value.x680.ValueCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ValueCollectionImpl implements ValueCollection
{
	public ValueCollectionImpl( boolean named )
	{
		this.named = named;
		valueRefs = new ArrayList<>();
	}

	private final boolean named;
	private final List<Ref<Value>> valueRefs;

	@Override
	public void add( @NotNull Ref<Value> valueRef )
	{
		if( named && !( valueRef instanceof NamedValue ) )
			throw new IllegalArgumentException( "Must be NamedValue" );

		valueRefs.add( valueRef );
	}

	@Override
	public void addNamed( @NotNull String name, @NotNull Ref<Value> valueRef )
	{
		if( !named )
			throw new IllegalStateException( "Use #add when collection is not named" );

		valueRefs.add( new NamedValueImpl( name, valueRef ) );
	}

	@NotNull
	@Override
	public Kind getKind()
	{
		return named || valueRefs.isEmpty() ? Kind.NAMED_COLLECTION : Kind.COLLECTION;
	}

	@Override
	public List<Ref<Value>> asValueList()
	{
		return Collections.unmodifiableList( valueRefs );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List<NamedValue> asNamedValueList()
	{
		return (List<NamedValue>)(Object)asValueList();
	}

	@Override
	public Map<String, NamedValue> asNamedValueMap()
	{
		assert named;
		Map<String, NamedValue> map = new HashMap<>();
		for( NamedValue value : asNamedValueList() )
			map.put( value.getName(), value );
		return map;
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( String name )
	{
		if( !named )
			throw new UnsupportedOperationException();

		for( Ref<Value> ref : valueRefs )
		{
			NamedValue value = ( (Value)ref ).toNamedValue();
			if( value.getName().equals( name ) )
				return value;
		}
		return null;
	}

	@Override
	public int size()
	{
		return valueRefs.size();
	}

	@Override
	public boolean isEmpty()
	{
		return valueRefs.isEmpty();
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == getKind() )
		{
			ValueCollection collection = o.toValueCollection();
			if( isEmpty() )
			{
				if( collection.isEmpty() )
					return 0;
				return -1;
			}

			if( collection.isEmpty() )
				return 0;

			if( getKind() == Kind.NAMED_COLLECTION )
				return compareNamedCollection( collection );

			return compareToCollection( collection );
		}

		return getKind().compareTo( o.getKind() );
	}

	private int compareToCollection( @NotNull ValueCollection collection )
	{
		Iterator<Ref<Value>> myIterator = asValueList().iterator();
		Iterator<Ref<Value>> otherIterator = collection.asValueList().iterator();
		while( myIterator.hasNext() && otherIterator.hasNext() )
		{
			//noinspection TypeMayBeWeakened
			Value my = (Value)myIterator.next();
			Value other = (Value)otherIterator.next();
			int result = my.compareTo( other );
			if( result != 0 )
				return result;
		}

		if( myIterator.hasNext() )
			return 1;

		if( otherIterator.hasNext() )
			return -1;

		return 0;
	}

	private int compareNamedCollection( @NotNull ValueCollection value )
	{
		Map<String, NamedValue> our = asNamedValueMap();
		Map<String, NamedValue> their = value.asNamedValueMap();
		Collection<String> unsortedKeys = new HashSet<>();
		unsortedKeys.addAll( our.keySet() );
		unsortedKeys.addAll( their.keySet() );
		List<String> keys = new ArrayList<>( unsortedKeys );
		Collections.sort( keys );

		for( String key : keys )
		{
			NamedValue lhs = our.get( key );
			NamedValue rhs = their.get( key );
			if( lhs == null )
			{
				assert rhs != null;
				return -1;
			}
			else if( rhs == null )
				return 1;
			else
			{
				int result = lhs.compareTo( rhs );
				if( result != 0 )
					return result;
			}
		}
		return 0;
	}

	@Override
	public boolean equals( Object obj )
	{
		return this == obj || obj instanceof ValueCollectionImpl && toString().equals( obj.toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public String toString()
	{
		return "{ " + StringUtils.join( valueRefs, ", " ) + " }";
	}
}
