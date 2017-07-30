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
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeName;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


final class TemplateInstanceScope extends AbstractScope
{
	TemplateInstanceScope( @NotNull Scope scope, @NotNull Template template, @NotNull List<Ref<?>> arguments )
	{
		super( scope );
		this.template = template;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.arguments = arguments;
	}

	private final Template template;
	private final List<Ref<?>> arguments;

	@NotNull
	@Override
	public Ref<Type> getTypeRef( @NotNull String ref, @Nullable String module )
	{
		return getParentScope().getTypeRef( ref, module );
	}

	@NotNull
	@Override
	public Ref<Value> getValueRef( @NotNull String ref, @Nullable String module )
	{
		return getParentScope().getValueRef( ref, module );
	}

	@Override
	public Type resolveType( @NotNull TypeName typeName ) throws ResolutionException
	{
		if( typeName.getModuleName() == null )
		{
			// TODO: valueSet checks?
			Ref<?> argument = getArgument( typeName.getName() );
			if( RefUtils.isTypeRef( argument ) )
				return (Type)argument.resolve( getParentScope() );
		}
		return getParentScope().resolveType( typeName );
	}

	@Override
	public Value resolveValue( @NotNull ValueName valueName ) throws ResolutionException
	{
		if( valueName.getModuleName() == null )
		{
			TemplateParameter parameter = template.getParameter( valueName.getName() );
			if( parameter != null && RefUtils.isValueRef( parameter.getReference() ) )
				return resolveTemplateValue( parameter );
		}
		return getParentScope().resolveValue( valueName );
	}

	@NotNull
	private Value resolveTemplateValue( TemplateParameter parameter ) throws ResolutionException
	{
		Ref<?> ref = arguments.get( parameter.getIndex() );
		if( !RefUtils.isValueRef( ref ) )
			throw new ResolutionException( "Illegal reference used for value TemplateParameter: " + ref );

		if( parameter.getGovernor() == null )
			throw new ResolutionException( "No governor defined for template parameter: " + parameter );

		Value value = (Value)ref.resolve( getParentScope() );
		try
		{
			parameter.getGovernor().resolve( this ).accept( this, value );
		} catch( ValidationException e )
		{
			throw new ResolutionException( "Governor does not accepts value: " + value, e );
		}
		return value;
	}

	@NotNull
	private Ref<?> getArgument( @NotNull TemplateParameter parameter )
	{
		return arguments.get( parameter.getIndex() );
	}

	@Nullable
	private Ref<?> getArgument( @NotNull String name )
	{
		TemplateParameter parameter = template.getParameter( name );
		if( parameter == null )
			return null;

		return getArgument( parameter );
	}
}
