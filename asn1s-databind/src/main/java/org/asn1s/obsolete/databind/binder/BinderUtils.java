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

package org.asn1s.obsolete.databind.binder;

import org.asn1s.obsolete.databind.mapper.MappedType;
import org.asn1s.obsolete.databind.mapper.SequenceMappedType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class BinderUtils
{
	private BinderUtils()
	{
	}

	static <T> T newInstance( @NotNull MappedType type, @Nullable Object[] parameters )
	{
		if( parameters != null && parameters.length > 0 )
			return newInstanceWithParameters( type, parameters );

		return newInstanceWithoutParameters( type );
	}

	@SuppressWarnings( "unchecked" )
	@NotNull
	private static <T> T newInstanceWithoutParameters( @NotNull MappedType type )
	{
		if( !( type instanceof SequenceMappedType ) )
			throw new UnsupportedOperationException();

		try
		{
			Constructor<T> constructor = (Constructor<T>)( (SequenceMappedType)type ).getConstructor();
			return constructor.newInstance();
		} catch( IllegalAccessException | InvocationTargetException | InstantiationException e )
		{
			throw new IllegalStateException( "Unable to create new instance of type: " + type.getTypeName(), e );
		}
	}

	@SuppressWarnings( "unchecked" )
	private static <T> T newInstanceWithParameters( MappedType type, Object[] parameters )
	{
		if( !( type instanceof SequenceMappedType ) )
			throw new UnsupportedOperationException();

		String[] constructorParameters = ( (SequenceMappedType)type ).getConstructorParameters();
		if( constructorParameters == null || constructorParameters.length != parameters.length )
			throw new IllegalArgumentException( "Amount of parameters does not match constructor parameters." );

		try
		{
			Constructor<T> constructor = (Constructor<T>)( (SequenceMappedType)type ).getConstructor();
			return constructor.newInstance( parameters );
		} catch( IllegalAccessException | InvocationTargetException | InstantiationException e )
		{
			throw new IllegalStateException( "Unable to create new instance of type: " + type.getTypeName(), e );
		}
	}

}
