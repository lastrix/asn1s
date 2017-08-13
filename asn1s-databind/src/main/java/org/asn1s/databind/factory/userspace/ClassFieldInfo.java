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

import org.asn1s.annotation.AnnotationUtils;
import org.asn1s.annotation.Asn1Property;
import org.asn1s.databind.TypeMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ClassFieldInfo
{
	ClassFieldInfo( Field field, Method setter, Method getter, TypeMapper mapper, boolean optional )
	{
		this.field = field;
		this.setter = setter;
		this.getter = getter;
		this.mapper = mapper;
		this.optional = optional;
	}

	private final Field field;
	private final Method setter;
	private final Method getter;
	private final TypeMapper mapper;
	private final boolean optional;

	public String getName()
	{
		if( field == null )
		{
			String base = setter == null ? getter.getName() : setter.getName();
			base = base.substring( 3 );
			return Character.toLowerCase( base.charAt( 0 ) ) + base.substring( 1 );
		}
		return field.getName();
	}

	public String getAsnName()
	{
		Asn1Property property = getPropertyAnnotation();
		return AnnotationUtils.isDefaultName( property )
				? getName()
				: property.name();
	}

	@NotNull
	public Asn1Property getPropertyAnnotation()
	{
		if( field != null )
			return field.getAnnotation( Asn1Property.class );

		return setter == null
				? getter.getAnnotation( Asn1Property.class )
				: setter.getAnnotation( Asn1Property.class );
	}

	public Field getField()
	{
		return field;
	}

	public Method getSetter()
	{
		return setter;
	}

	public Method getGetter()
	{
		return getter;
	}

	public TypeMapper getMapper()
	{
		return mapper;
	}

	public boolean isOptional()
	{
		return optional;
	}

	public void setValue( Object thisObject, Object value ) throws InvocationTargetException, IllegalAccessException
	{
		if( field == null )
		{
			assert setter != null;
			setter.invoke( thisObject, value );
		}
		else
			field.set( thisObject, value );
	}

	public Object getValue( Object thisObject ) throws InvocationTargetException, IllegalAccessException
	{
		if( field == null )
		{
			assert getter != null;
			return getter.invoke( thisObject );
		}
		return field.get( thisObject );
	}
}
