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

package org.asn1s.databind;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.type.NamedType;
import org.asn1s.databind.factory.EnumTypeMapperFactory;
import org.asn1s.databind.factory.TypeMapperFactory;
import org.asn1s.databind.factory.collection.CollectionClassTypeMapperFactory;
import org.asn1s.databind.factory.userspace.UserClassTypeMapperFactory;
import org.asn1s.databind.instrospection.Introspector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

public final class TypeMapperContext
{
	/**
	 * Java class to NamedType map.
	 * Key format is:
	 * {@link Class#getTypeName()}
	 */
	private final Map<String, NamedType> java2asn1Map = new HashMap<>();

	/**
	 * Map for mapped types, which contains all required metadata for I/O
	 * Key format is:
	 * {@link Class#getTypeName()} + "=" + {@link NamedType#getFullyQualifiedName()}
	 */
	private final Map<String, TypeMapper> typeMappers = new HashMap<>();
	private List<TypeMapperFactory> factories;
	private final Introspector introspector = new Introspector();

	public void registerTypeMapper( @NotNull TypeMapper typeMapper )
	{
		String key = typeMapper.getKey();
		if( typeMappers.get( key ) != null )
			throw new IllegalArgumentException( "TypeMapper for key already exist: " + key );
		typeMappers.put( key, typeMapper );
	}

	/**
	 * @param key type mapper key, {@link TypeMapper#getKey()}
	 * @return the TypeMapper or null
	 */
	@Nullable
	public TypeMapper getTypeMapper( String key )
	{
		return typeMappers.get( key );
	}

	/**
	 * Register type to asn1 type mapping
	 *
	 * @param type      the java Type, must be {@link Class}
	 * @param namedType the asn1 type
	 */
	public void registerJavaClassForNamedType( @NotNull Type type, @NotNull NamedType namedType )
	{
		registerJavaClassForNamedType( type.getTypeName(), namedType );
	}

	/**
	 * Register java type to asn1 type mapping
	 *
	 * @param typeName  the java type name, must point to {@link Class}
	 * @param namedType the asn1 type
	 */
	public void registerJavaClassForNamedType( @NotNull String typeName, @NotNull NamedType namedType )
	{
		if( java2asn1Map.containsKey( typeName ) )
			throw new IllegalArgumentException( "Java2Asn1 mapping already exist for: " + typeName );
		java2asn1Map.put( typeName, namedType );
	}

	/**
	 * Returns direct mapping to asn1 type by type name, {@link Class#getTypeName()}
	 *
	 * @param className the type name
	 * @return asn1 type or null
	 */
	@Nullable
	public NamedType getNamedTypeForJavaName( String className )
	{
		return java2asn1Map.get( className );
	}

	public Introspector getIntrospector()
	{
		return introspector;
	}

	public void mapTypes( Asn1Factory factory, Type[] types )
	{
		if( factories == null )
			factories = getTypeMapperFactories( factory );
		for( Type type : types )
			tryResolveOrMapType( type, null );
	}

	@NotNull
	public TypeMapper mapType( Type type, @Nullable TypeMetadata metadata )
	{
		for( TypeMapperFactory mapperFactory : factories )
			if( mapperFactory.isSupportedFor( type ) )
				return mapperFactory.mapType( type, metadata );

		throw new UnsupportedOperationException( "Unable to map type: " + type.getTypeName() );
	}

	private List<TypeMapperFactory> getTypeMapperFactories( Asn1Factory factory )
	{
		List<TypeMapperFactory> list = new ArrayList<>();
		list.add( new UserClassTypeMapperFactory( this, factory ) );
		list.add( new EnumTypeMapperFactory( this, factory ) );
		list.add( new CollectionClassTypeMapperFactory( this, factory ) );
		Collections.sort( list );
		return list;
	}

	public TypeMapper tryResolveOrMapType( Type type, @Nullable TypeMetadata metadata )
	{
		NamedType namedType = getNamedTypeForJavaName( type.getTypeName() );
		if( namedType == null )
			return mapType( type, metadata );

		TypeMapper mapper = getTypeMapper( TypeMapperUtils.mkTypeMapperKey( type, namedType ) );
		if( mapper == null )
			return mapType( type, metadata );

		return mapper;
	}
}
