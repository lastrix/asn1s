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

package org.asn1s.databind.factory;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.Ref;
import org.asn1s.api.type.CollectionOfType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.asn1s.databind.instrospection.JavaType;
import org.asn1s.databind.instrospection.ParameterizedJavaType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class CollectionClassTypeMapperFactory implements TypeMapperFactory
{
	public CollectionClassTypeMapperFactory( TypeMapperContext context, Asn1Factory factory )
	{
		this.context = context;
		this.factory = factory;
	}

	private final TypeMapperContext context;
	private final Asn1Factory factory;

	@Override
	public int getPriority()
	{
		return 1;
	}

	@Override
	public boolean isSupportedFor( Type type )
	{
		return type instanceof ParameterizedType && isAllowedCollection( type );
	}

	private static boolean isAllowedCollection( Type type )
	{
		Class<?> rawType = (Class<?>)( (ParameterizedType)type ).getRawType();
		return Objects.equals( rawType, List.class );
	}

	@Override
	public TypeMapper mapType( Type type )
	{
		if( !isSupportedFor( type ) )
			throw new IllegalArgumentException( "Unable to handle type: " + type.getTypeName() );

		if( Objects.equals( ( (ParameterizedType)type ).getRawType(), List.class ) )
			return mapListClass( type );

		throw new UnsupportedOperationException();
	}

	private TypeMapper mapListClass( Type type )
	{
		JavaType javaType = context.getIntrospector().introspect( type );
		if( !( javaType instanceof ParameterizedJavaType ) )
			throw new IllegalArgumentException( "Illegal List class: " + type.getTypeName() );

		JavaType elementType = ( (ParameterizedJavaType)javaType ).getTypeArguments()[0];

		TypeMapper typeMapper = context.tryResolveOrMapType( elementType.getType() );

		CollectionOfType collectionOf = factory.types().collectionOf( Family.SEQUENCE_OF );
		collectionOf.setComponent( "item", typeMapper.getAsn1Type() );

		NamedType define = factory.types().define( "List-Of-" + typeMapper.getAsn1Type().getName(), collectionOf, null );
		TypeMapper collectionTypeMapper = new CollectionTypeMapper( List.class, define, typeMapper, ArrayList::new );
		context.registerJavaClassForNamedType( type, define );
		context.registerTypeMapper( collectionTypeMapper );
		return collectionTypeMapper;
	}

	private static final class CollectionTypeMapper implements TypeMapper
	{
		private CollectionTypeMapper( Class<?> type, NamedType define, TypeMapper elementMapper, Function<Integer, Collection<Object>> instantiator )
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
}
