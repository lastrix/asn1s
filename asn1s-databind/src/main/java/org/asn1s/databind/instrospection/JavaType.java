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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class JavaType
{
	JavaType( Type type )
	{
		this.type = type;
		if( type instanceof Class<?> && ( (AnnotatedElement)type ).getAnnotation( Asn1Type.class ) != null )
			configuration = new JavaTypeConfiguration( (Class<?>)type );
	}

	private final Type type;
	private TypeVariable<Class<?>>[] typeVariables;
	private JavaType superClass;
	private JavaType[] interfaces;
	private JavaProperty[] properties;
	private JavaTypeConfiguration configuration;

	public TypeVariable<Class<?>>[] getTypeVariables()
	{
		return typeVariables.clone();
	}

	public void setTypeVariables( TypeVariable<Class<?>>[] typeVariables )
	{
		this.typeVariables = typeVariables.clone();
	}

	public Type getType()
	{
		return type;
	}

	public JavaType getSuperClass()
	{
		return superClass;
	}

	public void setSuperClass( JavaType superClass )
	{
		this.superClass = superClass;
	}

	public JavaType[] getInterfaces()
	{
		return interfaces.clone();
	}

	public void setInterfaces( JavaType[] interfaces )
	{
		this.interfaces = interfaces.clone();
	}

	@Nullable
	public JavaProperty[] getProperties()
	{
		return properties == null ? null : properties.clone();
	}

	public void setProperties( JavaProperty[] properties )
	{
		this.properties = properties.clone();
	}

	public JavaTypeConfiguration getConfiguration()
	{
		return configuration;
	}
}
