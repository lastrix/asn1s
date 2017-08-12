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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;

public class ParameterizedJavaType extends JavaType
{
	public ParameterizedJavaType( Type type, JavaType rawJavaType, JavaType[] typeArguments )
	{
		super( type );
		this.rawJavaType = rawJavaType;
		this.typeArguments = typeArguments.clone();
		copyProperties();
	}

	private void copyProperties()
	{
		JavaProperty[] rawProperties = rawJavaType.getProperties();
		if( rawProperties == null )
			return;

		JavaProperty[] properties = new JavaProperty[rawProperties.length];
		int i = 0;
		for( JavaProperty property : rawProperties )
		{
			properties[i] = property.copy( determineActualType( property ) );
			i++;
		}
		setProperties( properties );
	}

	private JavaType determineActualType( JavaProperty property )
	{
		int i = 0;
		Type type = property.getPropertyType().getType();
		for( TypeVariable<Class<?>> variable : rawJavaType.getTypeVariables() )
		{
			if( Objects.equals( variable, type ) )
				return typeArguments[i];
			i++;
		}
		throw new IllegalArgumentException( "No type variable defined: " + type );
	}

	private final JavaType rawJavaType;
	private final JavaType[] typeArguments;

	public JavaType getRawJavaType()
	{
		return rawJavaType;
	}

	public JavaType[] getTypeArguments()
	{
		return typeArguments.clone();
	}
}
