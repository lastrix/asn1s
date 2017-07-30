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

package org.asn1s.core.type;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractNestingType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TemplateTypeInstance extends AbstractNestingType
{
	TemplateTypeInstance( Ref<Type> ref, Collection<Ref<?>> arguments )
	{
		super( ref );
		this.arguments = new ArrayList<>( arguments );
	}

	private final List<Ref<?>> arguments;

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		//noinspection unchecked
		return parentScope.templateInstanceScope( (Template<Type>)getSibling(), arguments );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		new TemplateTypeInstantiator( scope ).newInstance();
	}

	@NotNull
	@Override
	public Type copy()
	{
		return new TemplateTypeInstance( cloneSibling(), arguments );
	}

	@Override
	public String toString()
	{
		return getSiblingRef() + "{" + StringUtils.join( arguments, ", " ) + '}';
	}

	@Override
	protected void onDispose()
	{
		super.onDispose();
		arguments.clear();
	}

	@Override
	protected String getSiblingNamespace()
	{
		return getNamespace() + ( (NamedType)getSibling() ).getName() + '{' + StringUtils.join( arguments, ',' ) + "}.";
	}

	private final class TemplateTypeInstantiator
	{
		private final Scope scope;
		private Scope templateScope;
		private Scope templateInstanceScope;

		TemplateTypeInstantiator( Scope scope ) throws ValidationException
		{
			if( arguments.isEmpty() )
				throw new ValidationException( "No arguments defined" );
			this.scope = scope;
		}

		void newInstance() throws ResolutionException, ValidationException
		{
			DefinedTypeTemplate template = resolveTypeTemplateOrDie();
			templateScope = template.createScope();
			templateInstanceScope = scope.templateInstanceScope( template, arguments );
			validateArguments( template );
			String siblingNamespace = getNamespace() + template.getName() + '{' + StringUtils.join( arguments, ',' ) + "}.";
			setSibling( template.newInstance( templateInstanceScope, siblingNamespace ) );
		}

		private void validateArguments( DefinedTypeTemplate template ) throws ValidationException, ResolutionException
		{
			if( arguments.size() != template.getParameterCount() )
				throw new ValidationException( "Template has " + template.getParameterCount() + " parameters, expected: " + arguments.size() );

			Collection<Ref<?>> validatedArguments = new ArrayList<>( arguments.size() );
			for( Ref<?> argument : arguments )
				validatedArguments.add( validateArgument( argument, template.getParameter( validatedArguments.size() ) ) );

			arguments.clear();
			arguments.addAll( validatedArguments );
		}

		@SuppressWarnings( "unchecked" )
		private Ref<?> validateArgument( Ref<?> argument, TemplateParameter parameter ) throws ValidationException, ResolutionException
		{
			if( RefUtils.isTypeRef( argument ) )
				return validateTypeArgument( parameter, (Ref<Type>)argument );

			if( RefUtils.isValueRef( argument ) )
				return validateValueArgument( parameter, (Ref<Value>)argument );

			throw new IllegalStateException( "Unable to use argument: " + argument );
		}

		private Ref<Value> validateValueArgument( TemplateParameter parameter, Ref<Value> argument ) throws ValidationException, ResolutionException
		{
			if( !parameter.isValueRef() )
				throw new ValidationException( "Parameter expects Type, but Value is present." );

			if( parameter.getGovernor() == null )
				throw new ValidationException( "TemplateParameter has no Governor for Value." );

			Type type = parameter.getGovernor().resolve( templateScope );
			return type.optimize( templateInstanceScope, argument );
		}

		private Ref<Type> validateTypeArgument( TemplateParameter parameter, Ref<Type> argument ) throws ValidationException, ResolutionException
		{
			if( parameter.isValueRef() )
				throw new ValidationException( "TemplateParameter expects Value, but Type is present." );

			Type type = argument.resolve( templateInstanceScope );
			assert type != null;
			if( !type.isValidated() )
				type.validate( templateScope );

			if( type.hasElementSetSpecs() || parameter.getGovernor() != null )
				assertValueSet( parameter, type );

			return type;
		}

		private void assertValueSet( TemplateParameter parameter, Type type ) throws ValidationException, ResolutionException
		{
			if( parameter.getGovernor() == null )
				throw new ValidationException( "TemplateParameter has no governor for elementSetSpecs validation" );

			Type resolve = parameter.getGovernor().resolve( templateScope );
			type.asElementSetSpecs().copyForType( templateInstanceScope, resolve );
		}

		@NotNull
		private DefinedTypeTemplate resolveTypeTemplateOrDie() throws ResolutionException, ValidationException
		{
			Type resolved = getSiblingRef().resolve( scope );
			if( !( resolved instanceof DefinedTypeTemplate ) )
				throw new ValidationException( "TemplateTypeInstance sub type must be TemplateType" );
			return (DefinedTypeTemplate)resolved;
		}
	}
}
