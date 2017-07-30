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

package org.asn1s.api.value;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Validation;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ValueNameRef implements Ref<Value>
{
	public ValueNameRef( @NotNull String name )
	{
		this( name, null );
	}

	public ValueNameRef( @NotNull String name, @Nullable String moduleName )
	{
		this( new ValueName( name, moduleName ) );
	}

	public ValueNameRef( ValueName valueName )
	{
		this.valueName = valueName;
	}

	private final ValueName valueName;

	public String getName()
	{
		return valueName.getName();
	}

	public String getModuleName()
	{
		return valueName.getModuleName();
	}

	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		Value value = scope.resolveValue( valueName );
		if( value instanceof DefinedValue && !( (DefinedValue)value ).isAbstract() )
			try
			{
				( (Validation)value ).validate( scope );
			} catch( ValidationException e )
			{
				throw new ResolutionException( "Unable to validate value: " + value, e );
			}
		return value;
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj == this || obj instanceof ValueNameRef && getValueName().equals( ( (ValueNameRef)obj ).getValueName() );
	}

	private ValueName getValueName()
	{
		return valueName;
	}

	@Override
	public int hashCode()
	{
		return valueName.hashCode();
	}

	@Override
	public String toString()
	{
		return valueName.toString();
	}
}
