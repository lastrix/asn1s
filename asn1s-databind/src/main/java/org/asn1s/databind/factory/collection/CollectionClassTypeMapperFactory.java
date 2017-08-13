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

import org.asn1s.annotation.CollectionSettings;
import org.asn1s.api.Asn1Factory;
import org.asn1s.api.type.CollectionOfType;
import org.asn1s.api.type.CollectionType;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.asn1s.databind.TypeMetadata;
import org.asn1s.databind.factory.TypeMapperFactory;
import org.asn1s.databind.instrospection.JavaType;
import org.asn1s.databind.instrospection.ParameterizedJavaType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	public TypeMapper mapType( Type type, TypeMetadata metadata )
	{
		if( !isSupportedFor( type ) )
			throw new IllegalArgumentException( "Unable to handle type: " + type.getTypeName() );

		if( Objects.equals( ( (ParameterizedType)type ).getRawType(), List.class ) )
			return mapListClass( type, metadata );

		throw new UnsupportedOperationException();
	}

	private TypeMapper mapListClass( Type type, TypeMetadata metadata )
	{
		JavaType javaType = context.getIntrospector().introspect( type );
		if( !( javaType instanceof ParameterizedJavaType ) )
			throw new IllegalArgumentException( "Illegal List class: " + type.getTypeName() );

		if( metadata != null && metadata.getCollectionSettings() != null )
			return mapListClassWithMetadata( javaType, metadata );

		JavaType elementType = ( (ParameterizedJavaType)javaType ).getTypeArguments()[0];

		TypeMapper typeMapper = context.tryResolveOrMapType( elementType.getType(), null );

		CollectionOfType collectionOf = factory.types().collectionOf( Family.SEQUENCE_OF );
		collectionOf.setComponent( "item", typeMapper.getAsn1Type() );

		NamedType define = factory.types().define( "List-Of-" + typeMapper.getAsn1Type().getName(), collectionOf, null );
		TypeMapper collectionTypeMapper = new CollectionTypeMapper( List.class, define, typeMapper, ArrayList::new );
		context.registerJavaClassForNamedType( type, define );
		context.registerTypeMapper( collectionTypeMapper );
		return collectionTypeMapper;
	}

	private TypeMapper mapListClassWithMetadata( JavaType javaType, TypeMetadata metadata )
	{
		CollectionSettings settings = metadata.getCollectionSettings();

		Class<?>[] classes = settings.value();
		CollectionType choiceType = factory.types().collection( Family.CHOICE );
		ChoiceItem[] items = new ChoiceItem[classes.length];
		int i = 0;
		for( Class<?> aClass : classes )
		{
			TypeMapper typeMapper = context.tryResolveOrMapType( aClass, null );
			String name = toChoiceName( aClass );
			items[i] = new ChoiceItem( name, typeMapper );
			choiceType.addComponent( Kind.PRIMARY, name, typeMapper.getAsn1Type() );
			i++;
		}
		ChoiceTypeMapper choiceTypeMapper = new ChoiceTypeMapper( javaType.getType(), defineType( settings, choiceType ), items );

		CollectionOfType collectionOf = factory.types().collectionOf( Family.SEQUENCE_OF );
		collectionOf.setComponent( "item", choiceTypeMapper.getAsn1Type() );


		NamedType define = factory.types().define( "List-Of-" + choiceTypeMapper.getAsn1Type().getName(), collectionOf, null );

		return new CollectionTypeMapper( List.class, define, choiceTypeMapper, ArrayList::new );
	}

	private NamedType defineType( CollectionSettings settings, org.asn1s.api.type.Type choiceType )
	{
		return factory.types().define( createName( settings ), choiceType, null );
	}

	private int counter;

	private String createName( CollectionSettings settings )
	{
		counter++;
		return "T-Java-Bind-Unnamed-Choice-" + counter;
	}

	private static String toChoiceName( Class<?> aClass )
	{
		String name = aClass.getSimpleName().replace( '_', '-' );
		return Character.toLowerCase( name.charAt( 0 ) ) + name.substring( 1 );
	}

}
