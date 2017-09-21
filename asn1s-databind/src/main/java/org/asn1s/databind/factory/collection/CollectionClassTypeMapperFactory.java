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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.annotation.AnnotationUtils;
import org.asn1s.annotation.CollectionSettings;
import org.asn1s.api.Asn1Factory;
import org.asn1s.api.Ref;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.api.type.*;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.asn1s.databind.TypeMetadata;
import org.asn1s.databind.factory.TypeMapperFactory;
import org.asn1s.databind.instrospection.JavaType;
import org.asn1s.databind.instrospection.ParameterizedJavaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CollectionClassTypeMapperFactory implements TypeMapperFactory
{
	private static final Log log = LogFactory.getLog( CollectionClassTypeMapperFactory.class );

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
	public TypeMapper mapType( Type type, TypeMetadata metadata )
	{
		if( !isSupportedFor( type ) )
			throw new IllegalArgumentException( "Unable to handle type: " + type.getTypeName() );

		if( Objects.equals( ( (ParameterizedType)type ).getRawType(), List.class ) )
			return new ListMapperFactory( type, metadata ).build();

		throw new UnsupportedOperationException();
	}

	private static String resolveListName( @Nullable TypeMetadata metadata, String defaultName )
	{
		if( metadata != null && !AnnotationUtils.isDefault( metadata.getTypeName() ) )
		{
			String typeName = metadata.getTypeName();
			RefUtils.assertTypeRef( typeName );
			return typeName;
		}
		return defaultName;
	}

	@Nullable
	private static ModuleReference resolveTypeModuleReference( @Nullable TypeMetadata metadata, @NotNull TypeMapper typeMapper )
	{
		if( metadata != null && !AnnotationUtils.isDefault( metadata.getModuleName() ) )
			return new ModuleReference( metadata.getModuleName() );

		NamedType asn1Type = typeMapper.getAsn1Type();
		return asn1Type instanceof DefinedType ? ( (DefinedType)asn1Type ).getModule().getModuleReference() : null;
	}

	@Nullable
	private ModuleReference getModuleReference( @Nullable TypeMetadata metadata, Type type )
	{
		if( metadata != null && !AnnotationUtils.isDefault( metadata.getModuleName() ) )
			return new ModuleReference( metadata.getModuleName() );

		TypeMapper typeMapper = context.tryResolveOrMapType( type, null );
		NamedType asn1Type = typeMapper.getAsn1Type();
		return asn1Type instanceof DefinedType ? ( (DefinedType)asn1Type ).getModule().getModuleReference() : null;
	}

	private NamedType defineType( @NotNull TypeFactory typeFactory, CollectionSettings settings, Ref<org.asn1s.api.type.Type> choiceType )
	{
		if( !AnnotationUtils.isDefault( settings.moduleName() ) )
			typeFactory = factory.types( new ModuleReference( settings.moduleName() ) );

		return typeFactory.define( createName( settings ), choiceType, null );
	}

	private int counter;

	private String createName( CollectionSettings settings )
	{
		if( !AnnotationUtils.isDefault( settings.typeName() ) )
		{
			RefUtils.assertTypeRef( settings.typeName() );
			return settings.typeName();
		}

		counter++;
		return "T-Java-Bind-Unnamed-Choice-" + counter;
	}

	private static String toChoiceName( Class<?> aClass )
	{
		String name = aClass.getSimpleName().replace( '_', '-' );
		return Character.toLowerCase( name.charAt( 0 ) ) + name.substring( 1 );
	}

	private final class ListMapperFactory
	{
		private ListMapperFactory( Type listType, @Nullable TypeMetadata metadata )
		{
			this.listType = listType;
			this.metadata = metadata;
		}

		private final Type listType;
		private final TypeMetadata metadata;

		private TypeMapper build()
		{
			JavaType javaType = context.getIntrospector().introspect( listType );
			if( !( javaType instanceof ParameterizedJavaType ) )
				throw new IllegalArgumentException( "Illegal List class: " + listType.getTypeName() );

			TypeMapper typeMapper = mapElementType( javaType, metadata );

			NamedType asn1Type = typeMapper.getAsn1Type();
			ModuleReference moduleReference = resolveTypeModuleReference( metadata, typeMapper );
			TypeFactory typeFactory = factory.types( moduleReference );

			CollectionOfType collectionOf = typeFactory.collectionOf( Family.SEQUENCE_OF );
			collectionOf.setComponent( "item", asn1Type );

			NamedType define = typeFactory.define( resolveListName( metadata, "List-Of-" + asn1Type.getName() ), collectionOf, null );
			log.debug( "Created new list-of type: " + define.getFullyQualifiedName() );
			TypeMapper collectionTypeMapper = new CollectionTypeMapper( List.class, define, typeMapper, ArrayList::new );
			context.registerJavaClassForNamedType( listType, define );
			context.registerTypeMapper( collectionTypeMapper );
			return collectionTypeMapper;
		}

		private TypeMapper mapElementType( JavaType javaType, @Nullable TypeMetadata metadata )
		{
			if( metadata != null && metadata.getCollectionSettings() != null )
				return mapElementAsChoiceType( javaType, metadata );

			JavaType elementType = ( (ParameterizedJavaType)javaType ).getTypeArguments()[0];
			return context.tryResolveOrMapType( elementType.getType(), null );
		}

		@NotNull
		private TypeMapper mapElementAsChoiceType( JavaType javaType, @NotNull TypeMetadata metadata )
		{
			CollectionSettings settings = metadata.getCollectionSettings();
			Class<?>[] classes = settings.value();
			ModuleReference moduleReference = getModuleReference( metadata, classes[0] );
			TypeFactory typeFactory = factory.types( moduleReference );
			CollectionType choiceType = typeFactory.collection( Family.CHOICE );
			ChoiceItem[] items = buildChoiceItems( classes, choiceType );
			return new ChoiceTypeMapper( javaType.getType(), defineType( typeFactory, settings, choiceType ), items );
		}

		@NotNull
		private ChoiceItem[] buildChoiceItems( Class<?>[] classes, ComponentTypeConsumer typeConsumer )
		{
			ChoiceItem[] items = new ChoiceItem[classes.length];
			int i = 0;
			for( Class<?> aClass : classes )
			{
				TypeMapper typeMapper = context.tryResolveOrMapType( aClass, null );
				String name = toChoiceName( aClass );
				items[i] = new ChoiceItem( name, typeMapper );
				typeConsumer.addComponent( Kind.PRIMARY, name, typeMapper.getAsn1Type() );
				i++;
			}
			return items;
		}
	}
}
