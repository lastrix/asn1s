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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class Template
{
	public Template()
	{
		this( false );
	}

	public Template( boolean instance )
	{
		this.instance = instance;
	}

	private final Map<String, TemplateParameter> parameterMap = new HashMap<>();
	private final boolean instance;

	public void addParameter( TemplateParameter parameter )
	{
		parameterMap.put( parameter.getName(), parameter );
	}

	@Nullable
	public TemplateParameter getParameter( @NotNull String name )
	{
		return parameterMap.get( name );
	}

	@NotNull
	public TemplateParameter getParameter( int index )
	{
		for( TemplateParameter parameter : parameterMap.values() )
			if( parameter.getIndex() == index )
				return parameter;

		throw new IllegalArgumentException( "No parameter with index: " + index );
	}

	public int getParameterCount()
	{
		return parameterMap.size();
	}

	public boolean isInstance()
	{
		return instance;
	}
}
