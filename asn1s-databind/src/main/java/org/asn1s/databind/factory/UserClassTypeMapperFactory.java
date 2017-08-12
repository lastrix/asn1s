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

import org.asn1s.annotation.AnnotationUtils;
import org.asn1s.annotation.Asn1Type;
import org.asn1s.annotation.Asn1Type.Kind;
import org.asn1s.api.Asn1Factory;
import org.asn1s.api.type.AbstractNestingType;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.asn1s.databind.TypeMapperUtils;
import org.asn1s.databind.instrospection.JavaProperty;
import org.asn1s.databind.instrospection.JavaPropertyConfiguration;
import org.asn1s.databind.instrospection.JavaType;
import org.asn1s.databind.instrospection.JavaTypeConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	public TypeMapper mapType( Type type )
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

	private void buildSiblingType( UserClassTypeMapper mapper )
	{
		Asn1Type classAnnotation = mapper.getJavaType().getAnnotation( Asn1Type.class );

		CollectionType collection = factory.types().collection( classAnnotation.kind() == Kind.Sequence ? Family.SEQUENCE : Family.SET );

		( (AbstractNestingType)mapper.getAsn1Type() ).setSiblingRef( collection );
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
			Type type = property.getPropertyType().getType();
			String typeName = type.getTypeName();
			TypeMapper propertyTypeMapper =
					AnnotationUtils.isDefault( conf.getTypeName() )
							? tryFindTypeMapper( type )
							: context.getTypeMapper( TypeMapperUtils.mkTypeMapperKey( typeName, conf.getTypeName() ) );
			if( propertyTypeMapper == null )
				propertyTypeMapper = context.mapType( type );

			collection.addComponent( conf.getKind(), conf.getAsn1Name(), propertyTypeMapper.getAsn1Type() ).setOptional( conf.isOptional() );

			if( property.getField() != null && !property.getField().isAccessible() )
				property.getField().setAccessible( true );
			if( property.getGetter() != null && !property.getGetter().isAccessible() )
				property.getGetter().setAccessible( true );
			if( property.getSetter() != null && !property.getSetter().isAccessible() )
				property.getSetter().setAccessible( true );
			ClassFieldInfo info = new ClassFieldInfo( property.getField(), property.getSetter(), property.getGetter(), propertyTypeMapper, conf.isOptional() );
			fieldMappers.add( info );
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

	private static final class UserClassTypeMapper implements TypeMapper
	{
		private UserClassTypeMapper( Class<?> javaType, NamedType asnType )
		{
			this.javaType = javaType;
			this.asnType = asnType;
		}

		private final Class<?> javaType;
		private final NamedType asnType;
		private ClassFieldInfo[] fieldMappers;

		@Override
		public Class<?> getJavaType()
		{
			return javaType;
		}

		@Override
		public NamedType getAsn1Type()
		{
			return asnType;
		}

		ClassFieldInfo[] getFieldMappers()
		{
			return fieldMappers.clone();
		}

		void setFieldMappers( ClassFieldInfo[] fieldMappers )
		{
			this.fieldMappers = fieldMappers.clone();
		}

		@NotNull
		@Override
		public Value toAsn1( @NotNull ValueFactory factory, @NotNull Object value )
		{
			if( !Objects.equals( javaType, value.getClass() ) )
				throw new IllegalArgumentException( "Unable to handle type: " + value.getClass() );
			ValueCollection collection = factory.collection( true );
			try
			{
				for( ClassFieldInfo fieldMapper : fieldMappers )
				{
					Object propertyValue = fieldMapper.getValue( value );
					if( propertyValue == null )
					{
						if( !fieldMapper.isOptional() )
							throw new IllegalStateException( "Unable to handle null value for property: " + fieldMapper.getName() );
						continue;
					}
					Value asn1 = fieldMapper.getMapper().toAsn1( factory, propertyValue );
					collection.addNamed( fieldMapper.getAsnName(), asn1 );
				}
			} catch( Exception e )
			{
				throw new IllegalStateException( e );
			}
			return collection;
		}

		@NotNull
		@Override
		public Object toJava( @NotNull Value value )
		{
			if( value.getKind() != Value.Kind.NAMED_COLLECTION )
				throw new IllegalArgumentException( "Unable to handle value of kind: " + value.getKind() );

			try
			{
				Object o = newInstance();
				for( NamedValue namedValue : value.toValueCollection().asNamedValueList() )
				{
					ClassFieldInfo fieldMapper = getFieldMapper( namedValue.getName() );
					Object java = fieldMapper.getMapper().toJava( (Value)namedValue.getValueRef() );
					fieldMapper.setValue( o, java );
				}
				return o;
			} catch( Exception e )
			{
				throw new IllegalStateException( e );
			}
		}

		private ClassFieldInfo getFieldMapper( String name )
		{
			for( ClassFieldInfo fieldMapper : fieldMappers )
			{
				if( fieldMapper.getAsnName().equals( name ) )
					return fieldMapper;
			}
			throw new IllegalArgumentException( "No fields for name: " + name );
		}

		private Object newInstance() throws Exception
		{
			Constructor<?> constructor = javaType.getDeclaredConstructor();
			if( !constructor.isAccessible() )
				constructor.setAccessible( true );
			return constructor.newInstance();
		}
	}
}
