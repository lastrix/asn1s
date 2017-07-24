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

package org.asn1s.api.type;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.util.RefUtils;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractComponentType extends AbstractNestingType implements ComponentType
{
	protected AbstractComponentType( int index, String name, @NotNull Ref<Type> componentTypeRef )
	{
		super( componentTypeRef );
		RefUtils.assertValueRef( name );
		this.index = index;
		this.name = name;
	}

	private final int index;
	private int version;
	private final String name;
	private boolean optional;

	@Override
	public final int getIndex()
	{
		return index;
	}

	@Override
	public final int getVersion()
	{
		return version;
	}

	public final void setVersion( int version )
	{
		this.version = version;
	}

	@Override
	public final String getName()
	{
		return name;
	}

	@Override
	public final boolean isOptional()
	{
		return optional;
	}

	@Override
	public final void setOptional( boolean value )
	{
		if( value && getDefaultValueRef() != null )
			throw new IllegalArgumentException( "Either default value or optional must be present" );

		optional = value;
	}

	@NotNull
	@Override
	public final Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.typedScope( this );
	}

	@NotNull
	@Override
	public final Type getComponentType()
	{
		return getSibling();
	}

	@NotNull
	@Override
	public final Ref<Type> getComponentTypeRef()
	{
		return getSiblingRef();
	}

	@Override
	public final boolean equals( Object obj )
	{
		return obj == this || obj instanceof AbstractComponentType && toString().equals( obj.toString() );
	}

	@Override
	public final int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public final String toString()
	{
		if( isOptional() )
			return getComponentName() + ' ' + getComponentTypeRef() + " OPTIONAL";

		if( getDefaultValueRef() != null )
			return getComponentName() + ' ' + getComponentTypeRef() + " DEFAULT " + getDefaultValueRef();

		return getComponentName() + ' ' + getComponentTypeRef();
	}

	@Override
	protected String getSiblingNamespace()
	{
		return getFullyQualifiedName() + '.';
	}
}
