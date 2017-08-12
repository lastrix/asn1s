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

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JavaProperty implements Serializable
{
	public JavaProperty( String name )
	{
		this.name = name;
	}

	private final String name;
	private Field field;
	private Method setter;
	private Method getter;
	private JavaType propertyType;
	private JavaPropertyConfiguration configuration;

	public String getName()
	{
		return name;
	}

	public Field getField()
	{
		return field;
	}

	public void setField( @Nullable Field field )
	{
		this.field = field;
	}

	public Method getSetter()
	{
		return setter;
	}

	public void setSetter( Method setter )
	{
		this.setter = setter;
	}

	public Method getGetter()
	{
		return getter;
	}

	public void setGetter( Method getter )
	{
		this.getter = getter;
	}

	public JavaType getPropertyType()
	{
		return propertyType;
	}

	public void setPropertyType( JavaType propertyType )
	{
		this.propertyType = propertyType;
	}

	public JavaPropertyConfiguration getConfiguration()
	{
		if( configuration == null )
			configuration = buildConfiguration();
		return configuration;
	}

	public JavaProperty copy( JavaType propertyType )
	{
		JavaProperty property = new JavaProperty( name );
		property.setField( field );
		property.setSetter( setter );
		property.setGetter( getter );
		property.setPropertyType( propertyType );
		return property;
	}


	private JavaPropertyConfiguration buildConfiguration()
	{
		if( field == null && setter == null && getter == null )
			throw new IllegalStateException();


		return new JavaPropertyConfiguration( name, field, setter, getter );
	}
}
