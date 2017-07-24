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

package org.asn1s.api;

import org.asn1s.api.util.RefUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractName<T extends AbstractName<T>> implements Comparable<T>
{
	protected AbstractName( @NotNull String name, @Nullable String moduleName )
	{
		if( moduleName != null )
			RefUtils.assertTypeRef( moduleName );

		this.name = name;
		this.moduleName = moduleName;
	}

	private final String name;
	private final String moduleName;

	public String getName()
	{
		return name;
	}

	public String getModuleName()
	{
		return moduleName;
	}

	@Override
	public int compareTo( @NotNull T o )
	{
		return toString().compareTo( o.toString() );
	}

	@Override
	public final boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof AbstractName ) ) return false;

		AbstractName typeName = (AbstractName)obj;

		//noinspection SimplifiableIfStatement
		if( !getName().equals( typeName.getName() ) ) return false;
		return getModuleName() != null ? getModuleName().equals( typeName.getModuleName() ) : typeName.getModuleName() == null;
	}

	@Override
	public final int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + ( getModuleName() != null ? getModuleName().hashCode() : 0 );
		return result;
	}

	@Override
	public final String toString()
	{
		if( moduleName == null )
			return name;

		return moduleName + '.' + name;
	}
}
