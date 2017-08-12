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

package org.asn1s.databind.instrospection;

import org.asn1s.annotation.Asn1Type;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Introspector
{
	private final Map<String, JavaType> typeMap = new HashMap<>();

	@NotNull
	public JavaType introspect( Type type )
	{
		if( typeMap.containsKey( type.getTypeName() ) )
			return typeMap.get( type.getTypeName() );

		if( type instanceof Class<?> )
			return forClass( (Class<?>)type );

		if( type instanceof ParameterizedType )
			return forParameterized( (ParameterizedType)type );

		if( type instanceof TypeVariable<?> )
		{
			JavaType javaType = new JavaType( type );
			typeMap.put( type.getTypeName(), javaType );
			return javaType;
		}
		throw new UnsupportedOperationException();
	}

	private JavaType forParameterized( ParameterizedType type )
	{
		Type rawType = type.getRawType();
		JavaType rawJavaType = introspect( rawType );
		ParameterizedJavaType javaType = new ParameterizedJavaType( type, rawJavaType, collectTypeArguments( type ) );
		typeMap.put( type.getTypeName(), javaType );
		return javaType;
	}

	@NotNull
	private JavaType[] collectTypeArguments( ParameterizedType type )
	{
		Type[] actualTypeArguments = type.getActualTypeArguments();
		JavaType[] arguments = new JavaType[actualTypeArguments.length];
		int i = 0;
		for( Type argument : actualTypeArguments )
		{
			arguments[i] = introspect( argument );
			i++;
		}
		return arguments;
	}

	private JavaType forClass( Class<?> type )
	{
		if( type.isAnnotation() || type.isEnum() || type.isArray() || type.isAnonymousClass() || type.isMemberClass() || type.isSynthetic() )
			throw new UnsupportedOperationException();

		Asn1Type annotation = type.getAnnotation( Asn1Type.class );

		JavaType javaType = new JavaType( type );

		TypeVariable<? extends Class<?>>[] variables = type.getTypeParameters();
		if( variables != null && variables.length > 0 )
			//noinspection unchecked,ConstantConditions
			javaType.setTypeVariables( (TypeVariable<Class<?>>[])variables );

		typeMap.put( type.getTypeName(), javaType );
		if( annotation == null )
			return javaType;

		if( !Objects.equals( type.getSuperclass(), Object.class ) )
			javaType.setSuperClass( introspect( type.getGenericSuperclass() == null ? type.getSuperclass() : type.getGenericSuperclass() ) );

		collectInterfaces( type, javaType );
		new PropertyCollector( javaType, this ).collectProperties( type );
		return javaType;
	}

	private void collectInterfaces( Class<?> type, JavaType javaType )
	{
		Type[] interfaces = type.getGenericInterfaces();
		if( interfaces == null || interfaces.length == 0 )
			interfaces = type.getInterfaces();
		JavaType[] array = new JavaType[interfaces.length];
		int i = 0;
		for( Type interfaceType : interfaces )
		{
			array[i] = introspect( interfaceType );
			i++;
		}
		javaType.setInterfaces( array );
	}
}
