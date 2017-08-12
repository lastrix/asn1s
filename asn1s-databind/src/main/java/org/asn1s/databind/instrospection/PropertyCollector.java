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

import org.asn1s.annotation.Asn1Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

class PropertyCollector
{
	PropertyCollector( JavaType javaType, Introspector introspector )
	{
		this.javaType = javaType;
		this.introspector = introspector;
	}

	private final JavaType javaType;
	private final Introspector introspector;
	private final List<JavaProperty> properties = new ArrayList<>();
	private final Collection<Method> visitedMethods = new HashSet<>();
	private Method[] methods;

	void collectProperties( Class<?> type )
	{
		methods = type.getDeclaredMethods();
		collectFields( type );
		collectMethods( type );
		properties.forEach( this :: updateType );
		javaType.setProperties( properties.toArray( new JavaProperty[properties.size()] ) );
	}

	private void updateType( JavaProperty property )
	{
		JavaType propertyType = introspector.introspect( resolveType( property ) );
		property.setPropertyType( propertyType );
	}

	private static Type resolveType( JavaProperty property )
	{
		Field field = property.getField();
		if( field != null )
			return field.getGenericType() == null ? field.getType() : field.getGenericType();

		Method getter = property.getGetter();
		if( getter != null )
			return getter.getGenericReturnType() == null ? getter.getReturnType() : getter.getGenericReturnType();

		Method setter = property.getSetter();
		if( setter != null )
		{
			Type type = setter.getGenericParameterTypes()[0];
			return type == null ? setter.getParameterTypes()[0] : type;
		}
		throw new IllegalStateException( "Unable to determine property type" );
	}

	private void collectMethods( Class<?> type )
	{
		for( Method method : type.getDeclaredMethods() )
			if( !visitedMethods.contains( method ) && method.getAnnotation( Asn1Property.class ) != null )
				collectMethod( type, method );
	}

	private void collectMethod( Class<?> type, Method method )
	{
		if( !isGetterSignature( method ) && !isSetterSignature( method ) )
			throw new IllegalStateException( "Is not getter or setter: " + method );

		String name = getPropertyName( method );
		JavaProperty javaProperty = getOrCreateJavaProperty( name );
		collectPropertyMethods( getBaseName( name ), javaProperty );
		javaProperty.setField( getFieldOrNull( type, name ) );
	}

	@Nullable
	private static Field getFieldOrNull( Class<?> type, String name )
	{
		try
		{
			return type.getDeclaredField( name );
		} catch( NoSuchFieldException ignored )
		{
			return null;
		}
	}

	private JavaProperty getOrCreateJavaProperty( String name )
	{
		for( JavaProperty property : properties )
			if( property.getName().equals( name ) )
				return property;

		JavaProperty property = new JavaProperty( name );
		properties.add( property );
		return property;
	}

	private void collectFields( Class<?> type )
	{
		Field[] fields = type.getDeclaredFields();
		for( Field field : fields )
			if( field.getAnnotation( Asn1Property.class ) != null )
				collectField( field );
	}

	private void collectField( Field field )
	{
		JavaProperty property = new JavaProperty( field.getName() );
		property.setField( field );
		properties.add( property );

		String baseName = getBaseName( field.getName() );

		collectPropertyMethods( baseName, property );
	}

	private void collectPropertyMethods( String baseName, JavaProperty property )
	{
		for( Method method : methods )
			collectPropertyMethod( baseName, property, method );
	}

	private void collectPropertyMethod( String baseName, JavaProperty property, Method method )
	{
		if( !setupMethod( baseName, property, method ) )
			return;

		if( method.getAnnotation( Asn1Property.class ) != null )
			throw new IllegalStateException( "Property annotation declared on Field and Method, only one must present" );

		if( !method.isAccessible() )
			method.setAccessible( true );
		visitedMethods.add( method );
	}

	private static boolean setupMethod( String baseName, JavaProperty property, Method method )
	{
		if( isSetter( method, baseName ) )
			property.setSetter( method );
		else if( isGetter( method, baseName ) )
			property.setGetter( method );
		else
			return false;
		return true;
	}

	private static boolean isGetter( Method method, String baseName )
	{
		return ( method.getName().equals( "get" + baseName ) || method.getName().equals( "is" + baseName ) )
				&& isGetterSignature( method );

	}

	private static boolean isGetterSignature( Method method )
	{
		return !Objects.equals( method.getReturnType(), void.class )
				&& method.getParameterCount() == 0;
	}

	private static boolean isSetter( Method method, String baseName )
	{
		return method.getName().equals( "set" + baseName )
				&& isSetterSignature( method );
	}

	private static boolean isSetterSignature( Method method )
	{
		return Objects.equals( method.getReturnType(), void.class )
				&& method.getParameterCount() == 1;
	}

	private static String getPropertyName( Method method )
	{
		String name = method.getName();
		if( name.startsWith( "set" ) || name.startsWith( "get" ) )
			name = name.substring( 3 );
		else if( name.startsWith( "is" ) )
			name = name.substring( 2 );
		else
			throw new IllegalArgumentException( "Is not getter or setter: " + method.getName() );

		return Character.toLowerCase( name.charAt( 0 ) ) + name.substring( 1 );
	}

	@NotNull
	private static String getBaseName( String name )
	{
		String baseName = name;
		baseName = Character.toUpperCase( baseName.charAt( 0 ) ) + baseName.substring( 1 );
		return baseName;
	}
}
