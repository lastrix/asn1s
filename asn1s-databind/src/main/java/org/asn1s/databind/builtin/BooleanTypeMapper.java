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

package org.asn1s.databind.builtin;

import org.asn1s.api.type.NamedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.databind.TypeMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Objects;

public final class BooleanTypeMapper implements TypeMapper
{
	public BooleanTypeMapper( Class<?> booleanClass, NamedType asnType )
	{
		assert Objects.equals( booleanClass, boolean.class ) || Objects.equals( booleanClass, Boolean.class );
		this.booleanClass = booleanClass;
		this.asnType = asnType;
	}

	private final Class<?> booleanClass;
	private final NamedType asnType;

	@Override
	public Type getJavaType()
	{
		return booleanClass;
	}

	@Override
	public NamedType getAsn1Type()
	{
		return asnType;
	}

	@NotNull
	@Override
	public Value toAsn1( @NotNull ValueFactory factory, @NotNull Object value )
	{
		if( !isBooleanClass( value ) )
			throw new IllegalArgumentException( "Unable to convert value: " + value );

		if( Boolean.TRUE.equals( value ) )
			return BooleanValue.TRUE;

		if( Boolean.FALSE.equals( value ) )
			return BooleanValue.FALSE;

		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Object toJava( @NotNull Value value )
	{
		if( value.getKind() != Kind.BOOLEAN )
			throw new IllegalArgumentException( "Unable to handle value of kind: " + value.getKind() );

		if( Objects.equals( value, BooleanValue.TRUE ) )
			return Boolean.TRUE;

		if( Objects.equals( value, BooleanValue.FALSE ) )
			return Boolean.FALSE;

		// this can not happen, since boolean value may be either TRUE or false
		throw new UnsupportedOperationException();
	}

	private static boolean isBooleanClass( @NotNull Object value )
	{
		return Objects.equals( value.getClass(), boolean.class )
				|| Objects.equals( value.getClass(), Boolean.class );
	}
}
