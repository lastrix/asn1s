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

package org.asn1s.databind.factory;

import org.asn1s.annotation.Asn1Type;
import org.asn1s.api.Asn1Factory;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

public class UserClassTypeMapperFactory implements TypeMapperFactory
{
	public UserClassTypeMapperFactory( TypeMapperContext context, Asn1Factory factory )
	{

		this.context = context;
		this.factory = factory;
	}

	private final TypeMapperContext context;
	private final Asn1Factory factory;

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public boolean isSupportedFor( Type type )
	{
		if( !( type instanceof Class<?> ) || ( (Class<?>)type ).isEnum() || ( (Class<?>)type ).isArray() || ( (Class<?>)type ).isAnnotation() )
			return false;

		AnnotatedElement element = (AnnotatedElement)type;
		return element.getAnnotation( Asn1Type.class ) != null;
	}

	@Override
	public TypeMapper mapType( Type type )
	{
		if( !( type instanceof Class<?> ) )
			throw new IllegalArgumentException( "Only classes may be mapped by this factory" );

		return mapClass( (Class<?>)type );
	}

	private TypeMapper mapClass( Class<?> aClass )
	{
		throw new UnsupportedOperationException();
	}
}
