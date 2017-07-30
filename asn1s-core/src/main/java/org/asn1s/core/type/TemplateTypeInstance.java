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
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractNestingType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.core.AbstractTemplateInstantiator;
import org.asn1s.core.CoreUtils;
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
		Template template = ( (DefinedType)getSibling() ).getTemplate();
		assert template != null;
		return parentScope.templateInstanceScope( template, arguments );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		new TemplateInstantiator( scope ).newInstance();
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

	private final class TemplateInstantiator extends AbstractTemplateInstantiator
	{
		private final Scope scope;

		TemplateInstantiator( Scope scope ) throws ValidationException
		{
			if( arguments.isEmpty() )
				throw new ValidationException( "No arguments defined" );
			this.scope = scope;
		}

		void newInstance() throws ResolutionException, ValidationException
		{
			DefinedType templateType = resolveTypeTemplateOrDie();
			setTemplateScope( templateType.createScope() );
			//noinspection ConstantConditions already checked non-null
			setTemplateInstanceScope( scope.templateInstanceScope( templateType.getTemplate(), arguments ) );
			setSibling( createInstanceOf( templateType ) );
		}

		private Type createInstanceOf( DefinedType templateType ) throws ResolutionException, ValidationException
		{
			String siblingNamespace = getNamespace() + templateType.getName() + '{' + StringUtils.join( arguments, ',' ) + "}.";
			DefinedTypeImpl instance = (DefinedTypeImpl)templateType.copy();
			instance.setNamespace( siblingNamespace );
			//noinspection ConstantConditions already checked non-null
			List<Ref<?>> list = resolveTemplateInstance( templateType.getTemplate(), arguments );
			arguments.clear();
			arguments.addAll( list );
			instance.setTemplate( getNewTemplate() );
			instance.validate( scope.templateInstanceScope( getNewTemplate(), arguments ) );
			return instance;
		}

		@NotNull
		private DefinedType resolveTypeTemplateOrDie() throws ResolutionException, ValidationException
		{
			Type resolved = getSiblingRef().resolve( scope );
			if( !isTemplateType( resolved ) )
				throw new ValidationException( "TemplateTypeInstance sub type must be DefinedType and has a non-instance Template" );
			//noinspection ConstantConditions
			CoreUtils.assertParameterMap( scope, ( (DefinedType)resolved ).getTemplate() );
			return (DefinedType)resolved;
		}

		private boolean isTemplateType( Type resolved )
		{
			if( !( resolved instanceof DefinedType ) )
				return false;
			DefinedType type = (DefinedType)resolved;
			return type.getTemplate() != null && !type.getTemplate().isInstance();
		}
	}
}
