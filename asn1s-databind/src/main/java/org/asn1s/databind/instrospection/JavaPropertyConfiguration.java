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

import org.asn1s.annotation.AnnotationUtils;
import org.asn1s.annotation.Asn1Property;
import org.asn1s.api.type.ComponentType.Kind;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JavaPropertyConfiguration
{

	public JavaPropertyConfiguration( String name, Field field, Method setter, Method getter )
	{
		Asn1Property property = findAnnotationProperty( field, setter, getter );
		asn1Name = AnnotationUtils.isDefaultName( property ) ? name.replace( '_', '-' ) : property.name();
		kind = property.componentKind();
		index = property.index();
		optional = property.optional();
		typeName = property.typeName();
	}

	private final String asn1Name;
	private final Kind kind;
	private final int index;
	private final boolean optional;
	private final String typeName;

	private static Asn1Property findAnnotationProperty( Field field, Method setter, Method getter )
	{
		Asn1Property annotation = field.getAnnotation( Asn1Property.class );
		if( annotation != null )
			return annotation;

		annotation = setter.getAnnotation( Asn1Property.class );
		if( annotation != null )
			return annotation;

		return getter.getAnnotation( Asn1Property.class );
	}

	public String getAsn1Name()
	{
		return asn1Name;
	}

	public Kind getKind()
	{
		return kind;
	}

	public int getIndex()
	{
		return index;
	}

	public boolean isOptional()
	{
		return optional;
	}

	public String getTypeName()
	{
		return typeName;
	}
}
