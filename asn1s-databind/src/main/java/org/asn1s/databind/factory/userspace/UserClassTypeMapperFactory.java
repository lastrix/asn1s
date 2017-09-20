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

package org.asn1s.databind.factory.userspace;

import org.asn1s.annotation.AnnotationUtils;
import org.asn1s.annotation.Asn1Type;
import org.asn1s.annotation.Asn1Type.Kind;
import org.asn1s.annotation.ConstructorParam;
import org.asn1s.api.Asn1Factory;
import org.asn1s.api.type.AbstractNestingType;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.asn1s.databind.TypeMapperUtils;
import org.asn1s.databind.TypeMetadata;
import org.asn1s.databind.factory.TypeMapperFactory;
import org.asn1s.databind.instrospection.JavaProperty;
import org.asn1s.databind.instrospection.JavaPropertyConfiguration;
import org.asn1s.databind.instrospection.JavaType;
import org.asn1s.databind.instrospection.JavaTypeConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

public class UserClassTypeMapperFactory implements TypeMapperFactory
{
	public UserClassTypeMapperFactory( TypeMapperContext context, Asn1Factory factory )
	{
		this.context = context;
		this.factory = factory;
	}

	private final TypeMapperContext context;
	private final Asn1Factory factory;

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public boolean isSupportedFor( Type type )
	{
		if( !( type instanceof Class<?> ) || isSpecialClass( (Class<?>)type ) )
			return false;
		if( ( (Class<?>)type ).getEnclosingClass() != null && !Modifier.isStatic( ( (Class<?>)type ).getModifiers() ) )
			return false;

		AnnotatedElement element = (AnnotatedElement)type;
		return element.getAnnotation( Asn1Type.class ) != null;
	}

	private static boolean isSpecialClass( Class<?> type )
	{
		return type.isEnum() || type.isArray() || type.isAnnotation();
	}

	@Override
	public TypeMapper mapType( Type type, TypeMetadata metadata )
	{
		if( !isSupportedFor( type ) )
			throw new IllegalArgumentException( "Only classes may be mapped by this factory" );

		return mapClass( (Class<?>)type );
	}

	private TypeMapper mapClass( Class<?> aClass )
	{
		NamedType namedType = context.getNamedTypeForJavaName( aClass.getTypeName() );
		if( namedType != null )
			throw new IllegalArgumentException( "Unable to redefine type: " + aClass.getTypeName() );

		UserClassTypeMapper mapper = createBlankMapper( aClass );
		context.registerTypeMapper( mapper );
		context.registerJavaClassForNamedType( mapper.getJavaType(), mapper.getAsn1Type() );
		new TypeMapperBuilder( mapper ).build();
		return mapper;
	}

	private UserClassTypeMapper createBlankMapper( Class<?> aClass )
	{
		Asn1Type classAnnotation = aClass.getAnnotation( Asn1Type.class );
		String asnTypeName = AnnotationUtils.isDefault( classAnnotation )
				? TypeMapperUtils.getDefaultAsnTypeName( aClass )
				: classAnnotation.name();

		DefinedType asnType = factory.types().define( asnTypeName, null, null );
		return new UserClassTypeMapper( aClass, asnType );
	}

	private final class TypeMapperBuilder
	{
		private TypeMapperBuilder( UserClassTypeMapper mapper )
		{
			this.mapper = mapper;
		}

		private final UserClassTypeMapper mapper;
		private CollectionType collection;
		private final List<ClassFieldInfo> fieldMappers = new ArrayList<>();

		private void build()
		{
			JavaType type = context.getIntrospector().introspect( mapper.getJavaType() );
			JavaTypeConfiguration configuration = type.getConfiguration();
			if( configuration == null )
				throw new IllegalArgumentException( "No configuration for type: " + type.getType().getTypeName() );
			collection = factory.types().collection( configuration.getKind() == Kind.Sequence ? Family.SEQUENCE : Family.SET );
			( (AbstractNestingType)mapper.getAsn1Type() ).setSiblingRef( collection );
			collection.setExtensible( configuration.isExtensible() );

			if( type.getSuperClass() != null )
				collectProperties( type.getSuperClass() );

			JavaType[] interfaces = type.getInterfaces();
			for( JavaType javaType : interfaces )
				collectProperties( javaType );

			collectProperties( type );
			mapper.setFieldMappers( fieldMappers.toArray( new ClassFieldInfo[fieldMappers.size()] ) );

			InstantiatorFactory instantiatorFactory = new InstantiatorFactory( mapper );
			mapper.setInstantiator( instantiatorFactory.createInstantiator() );
		}

		private void collectProperties( JavaType type )
		{
			JavaProperty[] properties = type.getProperties();
			if( properties == null || properties.length == 0 )
				return;

			for( JavaProperty property : properties )
				collectProperty( property );
		}

		private void collectProperty( JavaProperty property )
		{
			JavaPropertyConfiguration conf = property.getConfiguration();
			TypeMapper propertyTypeMapper = getPropertyTypeMapper( property, conf );
			collection.addComponent( conf.getKind(), conf.getAsn1Name(), propertyTypeMapper.getAsn1Type() ).setOptional( conf.isOptional() );
			setAccessible( property );
			ClassFieldInfo info = new ClassFieldInfo( property, propertyTypeMapper, conf.isOptional() );
			fieldMappers.add( info );
		}

		@NotNull
		private TypeMapper getPropertyTypeMapper( JavaProperty property, JavaPropertyConfiguration conf )
		{
			Type type = property.getPropertyType().getType();
			String typeName = type.getTypeName();
			TypeMapper propertyTypeMapper =
					AnnotationUtils.isDefault( conf.getTypeName() )
							? tryFindTypeMapper( type )
							: context.getTypeMapper( TypeMapperUtils.mkTypeMapperKey( typeName, conf.getTypeName() ) );

			if( propertyTypeMapper == null )
				propertyTypeMapper = context.mapType( type, property.toTypeMetadata() );

			return propertyTypeMapper;
		}

		private void setAccessible( JavaProperty property )
		{
			if( property.getField() != null && !property.getField().isAccessible() )
				property.getField().setAccessible( true );
			if( property.getGetter() != null && !property.getGetter().isAccessible() )
				property.getGetter().setAccessible( true );
			if( property.getSetter() != null && !property.getSetter().isAccessible() )
				property.getSetter().setAccessible( true );
		}

		@Nullable
		private TypeMapper tryFindTypeMapper( Type type )
		{
			NamedType namedType = context.getNamedTypeForJavaName( type.getTypeName() );
			if( namedType == null )
				return null;
			return context.getTypeMapper( TypeMapperUtils.mkTypeMapperKey( type, namedType ) );
		}
	}

	private final class InstantiatorFactory
	{
		private InstantiatorFactory( UserClassTypeMapper typeMapper )
		{
			this.typeMapper = typeMapper;
		}

		private final UserClassTypeMapper typeMapper;

		Instantiator createInstantiator()
		{
			Constructor<?> constructor = findAppropriateConstructor();
			if( constructor.getParameterCount() == 0 )
				return new Instantiator( constructor, null );

			return buildInstantiatorWithParameters( constructor );
		}

		private Instantiator buildInstantiatorWithParameters( Constructor<?> constructor )
		{
			Parameter[] parameters = constructor.getParameters();
			String[] names = new String[parameters.length];
			int i = 0;
			for( Parameter parameter : parameters )
			{
				ConstructorParam annotation = parameter.getAnnotation( ConstructorParam.class );
				assertNameValidity( names, i, annotation.value() );
				names[i] = annotation.value();
				i++;
			}

			return new Instantiator( constructor, names );
		}

		private void assertNameValidity( String[] names, int maxIndex, String value )
		{
			for( int i = 0; i < maxIndex; i++ )
				if( value.equals( names[i] ) )
					throw new IllegalArgumentException( "Duplicate ConstructorParam name: " + value );

			for( ClassFieldInfo info : typeMapper.getFieldMappers() )
				if( info.getName().equals( value ) )
					return;

			throw new IllegalStateException( "No property for name: " + value );
		}

		private Constructor<?> findAppropriateConstructor()
		{
			Class<?> javaType = typeMapper.getJavaType();
			Constructor<?>[] constructors = javaType.getDeclaredConstructors();
			checkOnlySingleConstructorWithAnno( constructors );
			for( Constructor<?> constructor : constructors )
				if( isAppropriateConstructor( constructor ) )
					return constructor;

			return findNoArgConstructor( javaType );
		}

		private boolean isAppropriateConstructor( Constructor<?> constructor )
		{
			if( !Modifier.isPublic( constructor.getModifiers() )
					|| constructor.getAnnotation( org.asn1s.annotation.Constructor.class ) == null )
				return false;

			assertConstructorParamsHasAnnotations( constructor );
			return true;
		}

		@NotNull
		private Constructor<?> findNoArgConstructor( Class<?> javaType )
		{
			try
			{
				Constructor<?> constructor = javaType.getDeclaredConstructor();
				if( !Modifier.isPublic( constructor.getModifiers() ) )
					throw new IllegalStateException( "No arg constructor is not public" );

				return constructor;
			} catch( NoSuchMethodException e )
			{
				throw new IllegalStateException( "Unable to find no arg constructor for: " + javaType.getTypeName(), e );
			}
		}

		private void assertConstructorParamsHasAnnotations( Constructor<?> constructor )
		{
			for( Parameter parameter : constructor.getParameters() )
				if( parameter.getAnnotation( ConstructorParam.class ) == null )
					throw new IllegalStateException( "All parameters of Constructor must have ConstructorParam annotation" );
		}

		private void checkOnlySingleConstructorWithAnno( Constructor<?>[] constructors )
		{
			boolean found = false;
			for( Constructor<?> constructor : constructors )
			{
				if( constructor.getAnnotation( org.asn1s.annotation.Constructor.class ) == null )
					continue;

				if( found )
					throw new IllegalStateException( "Only single constructor may have Constructor annotation" );

				found = true;
			}
		}
	}
}
