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

package org.asn1s.core.scope;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeName;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class TemplateScope extends AbstractScope
{
	TemplateScope( Scope scope, Template<?> type )
	{
		super( scope );
		this.type = type;
	}

	private final Template<?> type;

	@SuppressWarnings( "unchecked" )
	@NotNull
	@Override
	public Ref<Type> getTypeRef( @NotNull String ref, @Nullable String module )
	{
		if( module == null )
		{
			TemplateParameter parameter = type.getParameter( ref );
			if( parameter != null && RefUtils.isTypeRef( parameter.getReference() ) )
				return (Ref<Type>)parameter.getReference();
		}

		return getParentScope().getTypeRef( ref, module );
	}

	@SuppressWarnings( "unchecked" )
	@NotNull
	@Override
	public Ref<Value> getValueRef( @NotNull String ref, @Nullable String module )
	{
		if( module == null )
		{
			TemplateParameter parameter = type.getParameter( ref );
			if( parameter != null && parameter.isValueRef() )
				return (Ref<Value>)parameter.getReference();
		}
		return getParentScope().getValueRef( ref, module );
	}

	@Override
	public Type resolveType( @NotNull TypeName typeName ) throws ResolutionException
	{
		return getParentScope().resolveType( typeName );
	}

	@Override
	public Value resolveValue( @NotNull ValueName valueName ) throws ResolutionException
	{
		return getParentScope().resolveValue( valueName );
	}
}
