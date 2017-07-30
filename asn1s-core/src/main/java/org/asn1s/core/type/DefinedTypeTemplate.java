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

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.Type;
import org.asn1s.core.CoreUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class DefinedTypeTemplate extends DefinedTypeImpl implements Template<DefinedTypeTemplate>
{
	public DefinedTypeTemplate( @NotNull Module module, @NotNull String name, @NotNull Ref<Type> reference, @Nullable Iterable<TemplateParameter> parameters )
	{
		super( module, name, reference );
		template = true;
		if( parameters != null )
			parameters.forEach( e -> parameterMap.put( e.getName(), e ) );
	}

	private DefinedTypeTemplate( @NotNull DefinedTypeTemplate typeTemplate, String namespace )
	{
		super( typeTemplate.getModule(), typeTemplate.getName(), typeTemplate.cloneSibling() );
		template = false;
		setNamespace( namespace );
		typeTemplate.parameterMap.values().forEach( e -> parameterMap.put( e.getName(), e ) );
	}

	private final boolean template;

	@Override
	public String getFullyQualifiedName()
	{
		return super.getFullyQualifiedName() + '{' + CoreUtils.paramMapToString( parameterMap ) + '}';
	}

	@NotNull
	@Override
	public Scope createScope()
	{
		return getModule().createScope().templateScope( this );
	}

	@Override
	protected boolean isUseCreateScope()
	{
		return false;
	}

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.templateScope( this );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( isTemplate() )
			throw new ValidationException( "Unable to validate templates" );

		scope = getScope( scope );
		super.onValidate( scope );
		CoreUtils.assertParameterMap( scope, parameterMap );
	}

	@NotNull
	@Override
	public DefinedTypeTemplate copy()
	{
		return new DefinedTypeTemplate( getModule(), getName(), cloneSibling(), parameterMap.values() );
	}

	@Override
	public boolean equals( Object obj )
	{
		return this == obj || obj instanceof DefinedTypeTemplate && toString().equals( obj.toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public int getParameterCount()
	{
		return parameterMap.size();
	}

	@Override
	public DefinedTypeTemplate newInstance( Scope scope, String namespace ) throws ResolutionException
	{
		DefinedTypeTemplate copy = new DefinedTypeTemplate( this, namespace );
		CoreUtils.resolutionValidate( scope, copy );
		return copy;
	}

	@Override
	public boolean isTemplate()
	{
		return template;
	}
	////////////////////////////// Parameters //////////////////////////////////////////////////////////////////////////

	private final Map<String, TemplateParameter> parameterMap = new HashMap<>();

	@Override
	@Nullable
	public TemplateParameter getParameter( @NotNull String name )
	{
		return parameterMap.get( name );
	}

	@NotNull
	@Override
	public TemplateParameter getParameter( int index )
	{
		for( TemplateParameter parameter : parameterMap.values() )
			if( parameter.getIndex() == index )
				return parameter;

		throw new IllegalArgumentException( "No parameter with index: " + index );
	}

	@Override
	public String toString()
	{
		return getName() + '{' + CoreUtils.paramMapToString( parameterMap ) + '}';
	}
}
