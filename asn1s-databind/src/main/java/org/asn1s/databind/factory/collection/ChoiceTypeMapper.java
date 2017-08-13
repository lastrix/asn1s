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

package org.asn1s.databind.factory.collection;

import org.asn1s.api.type.NamedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.databind.TypeMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

final class ChoiceTypeMapper implements TypeMapper
{
	ChoiceTypeMapper( Type javaType, NamedType asn1Type, ChoiceItem[] items )
	{
		this.javaType = javaType;
		this.asn1Type = asn1Type;
		this.items = items.clone();
	}

	private final Type javaType;
	private final NamedType asn1Type;
	private final ChoiceItem[] items;

	@Override
	public Type getJavaType()
	{
		return javaType;
	}

	@Override
	public NamedType getAsn1Type()
	{
		return asn1Type;
	}

	@NotNull
	@Override
	public Value toAsn1( @NotNull ValueFactory factory, @NotNull Object value )
	{
		Class<?> aClass = value.getClass();
		ChoiceItem item = selectForClass( aClass );
		if( item == null )
			throw new IllegalArgumentException( "Unable to handle value of type: " + aClass.getTypeName() );

		return factory.named( item.getName(), item.getMapper().toAsn1( factory, value ) );
	}

	@Nullable
	private ChoiceItem selectForClass( Class<?> aClass )
	{
		for( ChoiceItem item : items )
			if( Objects.equals( item.getMapper().getJavaType(), aClass ) )
				return item;

		return null;
	}

	@NotNull
	@Override
	public Object toJava( @NotNull Value value )
	{
		if( value.getKind() != Kind.NAME )
			throw new IllegalArgumentException( "Unable to handle value of kind: " + value.getKind() );

		NamedValue namedValue = value.toNamedValue();
		ChoiceItem item = selectByNameOrDie( namedValue.getName() );

		assert namedValue.getValueRef() != null;
		return item.getMapper().toJava( (Value)namedValue.getValueRef() );
	}

	private ChoiceItem selectByNameOrDie( String name )
	{
		for( ChoiceItem item : items )
		{
			if( name.equals( item.getName() ) )
				return item;
		}
		throw new IllegalStateException( "Unable to find component: " + name );
	}
}
