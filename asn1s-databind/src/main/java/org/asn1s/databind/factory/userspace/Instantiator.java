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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class Instantiator
{
	Instantiator( Constructor<?> constructor, @Nullable String[] parameters )
	{
		this.constructor = constructor;
		this.parameters = parameters == null ? null : parameters.clone();
	}

	private final Constructor<?> constructor;
	private final String[] parameters;

	boolean hasParameters()
	{
		return parameters != null && parameters.length > 0;
	}

	@Nullable
	String[] getParameters()
	{
		return parameters == null ? null : parameters.clone();
	}

	Object newInstance()
	{
		try
		{
			return constructor.newInstance();
		} catch( InstantiationException | IllegalAccessException | InvocationTargetException e )
		{
			throw new IllegalStateException( "Unable to create instance using: " + constructor.getDeclaringClass().getTypeName() + "::" + constructor.getName(), e );
		}
	}

	Object newInstance( @NotNull Object[] arguments )
	{
		if( !hasParameters() )
			throw new IllegalStateException( "No parameters expected" );

		assert parameters != null;
		if( arguments.length != parameters.length )
			throw new IllegalArgumentException( "Argument count does not match: " + parameters.length + ", got: " + arguments.length );
		try
		{
			return constructor.newInstance( arguments );
		} catch( InstantiationException | IllegalAccessException | InvocationTargetException e )
		{
			throw new IllegalStateException( "Unable to create instance using: "
					                                 + constructor.getDeclaringClass().getTypeName() + "::" + constructor.getName()
					                                 + '(' + StringUtils.join( parameters, ", " ) + ')',
			                                 e );
		}
	}
}
