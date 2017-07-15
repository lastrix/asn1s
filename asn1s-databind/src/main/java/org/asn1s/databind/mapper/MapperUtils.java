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

package org.asn1s.databind.mapper;

import org.asn1s.annotation.Constructor;
import org.asn1s.annotation.ConstructorParam;
import org.asn1s.annotation.Sequence;
import org.asn1s.api.util.RefUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public final class MapperUtils
{
	public static final String LABEL_DEFAULT_VALUE = "#default";
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private MapperUtils()
	{
	}

	static java.lang.reflect.Constructor<?> chooseConstructor( Class<?> type )
	{
		java.lang.reflect.Constructor<?>[] constructors = type.getDeclaredConstructors();

		int constructorsWithAnnotation = countConstructorsWithAnnotation( constructors );

		if( constructorsWithAnnotation > 1 )
			throw new IllegalStateException( "Only single constructor may be marker with @Constructor annotation for class: " + type.getCanonicalName() );

		java.lang.reflect.Constructor<?> defaultConstructor = null;
		if( constructorsWithAnnotation == 1 )
		{
			for( java.lang.reflect.Constructor<?> constructor : constructors )
			{
				if( constructor.getParameters().length == 0 )
					defaultConstructor = constructor;
				else if( isValidConstructor( constructor, true ) )
					return constructor;
			}
		}
		else
		{
			try
			{
				defaultConstructor = type.getDeclaredConstructor();
			} catch( NoSuchMethodException e )
			{
				throw new IllegalStateException( "Type has no default constructor: " + type.getCanonicalName(), e );
			}
		}

		if( defaultConstructor != null && isValidConstructor( defaultConstructor, false ) )
			return defaultConstructor;

		throw new IllegalStateException( "Unable to detect any valid constructor for type: " + type.getCanonicalName() );
	}

	private static int countConstructorsWithAnnotation( java.lang.reflect.Constructor<?>[] constructors )
	{
		int constructorsWithAnnotation = 0;
		for( java.lang.reflect.Constructor<?> constructor : constructors )
		{
			if( constructor.getAnnotation( Constructor.class ) != null )
				constructorsWithAnnotation++;
		}
		return constructorsWithAnnotation;
	}

	@NotNull
	public static String getAsn1TypeNameForClass( Class<?> type )
	{
		Sequence annotation = type.getAnnotation( Sequence.class );
		if( annotation != null && !LABEL_DEFAULT_VALUE.equals( annotation.name() ) )
			return annotation.name();

		return "T-Java-Bind-" + type.getCanonicalName().replace( '.', '-' );
	}

	private static boolean isValidConstructor( java.lang.reflect.Constructor<?> constructor, boolean requireAnnotation )
	{
		Constructor annotation = constructor.getAnnotation( Constructor.class );
		//noinspection SimplifiableIfStatement
		if( annotation == null && requireAnnotation )
			return false;

		return Modifier.isPublic( constructor.getModifiers() ) && ( annotation == null || isValidParameters( constructor ) );
	}

	private static boolean isValidParameters( java.lang.reflect.Constructor<?> constructor )
	{
		for( Parameter parameter : constructor.getParameters() )
			if( parameter.getAnnotation( ConstructorParam.class ) == null )
				return false;

		return true;
	}

	static String[] fetchConstructorParameterNames( java.lang.reflect.Constructor<?> constructor )
	{
		if( constructor.getParameterCount() == 0 )
			return EMPTY_STRING_ARRAY;

		List<String> list = new ArrayList<>( constructor.getParameterCount() );
		for( Parameter parameter : constructor.getParameters() )
		{
			ConstructorParam annotation = parameter.getAnnotation( ConstructorParam.class );
			assert annotation != null;
			RefUtils.assertValueRef( annotation.value() );
			list.add( ( annotation.global() ? TypeMapper.MARKER_GLOBAL_VARIABLE : TypeMapper.MARKER_LOCAL_VARIABLE ) + annotation.value() );
		}
		return list.toArray( new String[list.size()] );
	}
}
