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

package org.asn1s.core;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractTemplateInstantiator
{
	private Scope templateScope;
	private Scope templateInstanceScope;
	private final Template newTemplate = new Template( true );

	protected void setTemplateScope( Scope templateScope )
	{
		this.templateScope = templateScope;
	}

	protected void setTemplateInstanceScope( Scope templateInstanceScope )
	{
		this.templateInstanceScope = templateInstanceScope;
	}

	protected Scope getTemplateInstanceScope()
	{
		return templateInstanceScope;
	}

	protected Template getNewTemplate()
	{
		return newTemplate;
	}

	protected List<Ref<?>> resolveTemplateInstance( Template template, Collection<Ref<?>> arguments ) throws ValidationException, ResolutionException
	{
		if( arguments.size() != template.getParameterCount() )
			throw new ValidationException( "Template has " + template.getParameterCount() + " parameters, expected: " + arguments.size() );

		List<Ref<?>> validatedArguments = new ArrayList<>( arguments.size() );
		for( Ref<?> argument : arguments )
		{
			TemplateParameter parameter = template.getParameter( validatedArguments.size() );
			validatedArguments.add( resolveArgument( argument, parameter ) );
		}

		return validatedArguments;
	}

	@SuppressWarnings( "unchecked" )
	private Ref<?> resolveArgument( Ref<?> argument, TemplateParameter parameter ) throws ValidationException, ResolutionException
	{
		if( RefUtils.isTypeRef( argument ) )
			return resolveTypeArgument( parameter, (Ref<Type>)argument );

		if( RefUtils.isValueRef( argument ) )
			return resolveValueArgument( parameter, (Ref<Value>)argument );

		throw new IllegalStateException( "Unable to use argument: " + argument );
	}

	private Ref<Value> resolveValueArgument( TemplateParameter parameter, Ref<Value> argument ) throws ValidationException, ResolutionException
	{
		if( !parameter.isValueRef() )
			throw new ValidationException( "Parameter expects Type, but Value is present." );

		if( parameter.getGovernor() == null )
			throw new ValidationException( "TemplateParameter has no Governor for Value." );

		Type type = parameter.getGovernor().resolve( templateScope );
		newTemplate.addParameter( new TemplateParameter( parameter.getIndex(), parameter.getReference(), type ) );
		return type.optimize( templateInstanceScope, argument );
	}

	private Ref<Type> resolveTypeArgument( TemplateParameter parameter, Ref<Type> argument ) throws ValidationException, ResolutionException
	{
		if( parameter.isValueRef() )
			throw new ValidationException( "TemplateParameter expects Value, but Type is present." );

		Type type = argument.resolve( templateInstanceScope );
		assert type != null;
		if( !type.isValidated() )
			type.validate( templateScope );

		boolean isValueSet = type.hasElementSetSpecs() || parameter.getGovernor() != null;
		TemplateParameter newParameter =
				new TemplateParameter(
						parameter.getIndex(),
						parameter.getReference(),
						isValueSet ? validateValueSet( parameter, type ) : null );
		newTemplate.addParameter( newParameter );
		return type;
	}

	private Ref<Type> validateValueSet( TemplateParameter parameter, Type type ) throws ValidationException, ResolutionException
	{
		if( parameter.getGovernor() == null )
			throw new ValidationException( "TemplateParameter has no governor for elementSetSpecs validation" );

		Type resolve = parameter.getGovernor().resolve( templateScope );
		type.asElementSetSpecs().copyForType( templateInstanceScope, resolve );
		return resolve;
	}
}
