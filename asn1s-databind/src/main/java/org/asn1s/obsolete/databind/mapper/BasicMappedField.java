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

package org.asn1s.obsolete.databind.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class BasicMappedField implements MappedField
{
	public static final long OPTION_READONLY = 0x0001;
	public static final long OPTION_OPTIONAL = 0x0002;

	BasicMappedField( int index, @NotNull String propertyName, @NotNull MappedType type, long options, @Nullable Field field, @Nullable Method setter, @Nullable Method getter )
	{
		if( setter == null && getter == null && field == null )
			throw new IllegalArgumentException( "Field, setter and getter must not be all nulls." );

		if( field == null && ( setter == null && ( options & OPTION_READONLY ) == 0 || getter == null ) )
			throw new IllegalArgumentException( "Both setter and getter must be non null values" );

		this.index = index;
		this.propertyName = propertyName;
		this.type = type;
		this.options = options;
		this.field = field;
		this.setter = setter;
		this.getter = getter;
	}

	private final int index;
	private final String propertyName;
	private final MappedType type;
	private final long options;
	private final Field field;
	private final Method setter;
	private final Method getter;

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	@NotNull
	public String getPropertyName()
	{
		return propertyName;
	}

	@Override
	@NotNull
	public MappedType getType()
	{
		return type;
	}

	@Override
	public boolean isReadonly()
	{
		return ( options & OPTION_READONLY ) != 0;
	}

	@Override
	public boolean isOptional()
	{
		return ( options & OPTION_OPTIONAL ) != 0;
	}

	@Nullable
	public Field getField()
	{
		return field;
	}

	@Nullable
	public Method getSetter()
	{
		return setter;
	}

	@Nullable
	public Method getGetter()
	{
		return getter;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof BasicMappedField ) ) return false;

		MappedField mappedField = (MappedField)obj;

		return getPropertyName().equals( mappedField.getPropertyName() );
	}

	@Override
	public int hashCode()
	{
		return getPropertyName().hashCode();
	}

	@Override
	public String toString()
	{
		return ( isOptional() ? "*" : "" ) + propertyName + ' ' + type.getTypeName() + "=>" + type.getAsnType().getName();
	}
}
