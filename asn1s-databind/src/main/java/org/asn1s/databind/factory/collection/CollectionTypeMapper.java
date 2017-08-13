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

package org.asn1s.databind.factory.collection;

import org.asn1s.api.Ref;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.databind.TypeMapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Function;

final class CollectionTypeMapper implements TypeMapper
{
	CollectionTypeMapper( Class<?> type, NamedType define, TypeMapper elementMapper, Function<Integer, Collection<Object>> instantiator )
	{
		this.type = type;
		this.define = define;
		this.elementMapper = elementMapper;
		this.instantiator = instantiator;
	}

	private final Class<?> type;
	private final NamedType define;
	private final TypeMapper elementMapper;
	private final Function<Integer, Collection<Object>> instantiator;

	@Override
	public Class<?> getJavaType()
	{
		return type;
	}

	@Override
	public NamedType getAsn1Type()
	{
		return define;
	}

	@NotNull
	@Override
	public Value toAsn1( @NotNull ValueFactory factory, @NotNull Object value )
	{
		if( !type.isAssignableFrom( value.getClass() ) )
			throw new IllegalArgumentException( "Unable to handle value of type: " + value.getClass().getTypeName() );

		ValueCollection collection = factory.collection( false );
		for( Object o : (Iterable<?>)value )
		{
			Value asn1 = elementMapper.toAsn1( factory, o );
			collection.add( asn1 );
		}
		return collection;
	}

	@NotNull
	@Override
	public Object toJava( @NotNull Value value )
	{
		Kind kind = value.getKind();
		if( kind != Kind.COLLECTION && kind != Kind.NAMED_COLLECTION )
			throw new IllegalArgumentException( "Unable to handle value of kind: " + kind );

		ValueCollection collection = value.toValueCollection();
		Collection<Object> objects = instantiator.apply( collection.size() );
		for( Ref<Value> ref : collection.asValueList() )
			objects.add( elementMapper.toJava( (Value)ref ) );

		return objects;
	}
}
