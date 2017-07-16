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
import org.asn1s.api.module.Module;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.CoreUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class DefinedTypeTemplate extends DefinedTypeImpl implements Template<DefinedTypeTemplate>
{
	public DefinedTypeTemplate( @NotNull Module module, @NotNull String name, @NotNull Ref<Type> reference, @Nullable Iterable<TemplateParameter> parameters )
	{
		this( module, name, reference, parameters, true );
	}

	private DefinedTypeTemplate( @NotNull Module module, @NotNull String name, @NotNull Ref<Type> reference, @Nullable Iterable<TemplateParameter> parameters, boolean template )
	{
		super( module, name, reference );
		this.template = template;
		if( parameters != null )
			parameters.forEach( this :: addParameter );
	}

	private final boolean template;

	@Override
	public String getFullyQualifiedName()
	{
		return super.getFullyQualifiedName() + '{' + getParametersString() + '}';
	}

	@NotNull
	@Override
	public Scope createScope()
	{
		return getModule().createScope().templateScope( this );
	}

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.templateScope( this );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		getType().accept( getScope( scope ), valueRef );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		scope = getScope( scope );
		if( getType() == null )
			setType( getReference().resolve( scope ) );

		getType().validate( scope );

		CoreUtils.assertParameterMap( scope, parameterMap );
	}

	@NotNull
	@Override
	public DefinedTypeTemplate copy()
	{
		return copy( template );
	}

	@NotNull
	private DefinedTypeTemplate copy( boolean template )
	{
		Ref<Type> subType = Objects.equals( getReference(), getType() ) ? getType().copy() : getReference();
		return new DefinedTypeTemplate( getModule(), getName(), subType, parameterMap.values(), template );
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
	public String toString()
	{
		return getName() + '{' + StringUtils.join( parameterMap.values(), ", " ) + '}';
	}

	@Override
	public int getParameterCount()
	{
		return parameterMap.size();
	}

	@Override
	public DefinedTypeTemplate newInstance( Scope scope, String namespace ) throws ResolutionException
	{
		DefinedTypeTemplate copy = copy( false );
		copy.setNamespace( namespace );
		try
		{
			copy.validate( scope );
		} catch( ValidationException e )
		{
			throw new ResolutionException( "Unable to create new template type instance", e );
		}
		return copy;
	}

	@Override
	public boolean isTemplate()
	{
		return template;
	}
	////////////////////////////// Parameters //////////////////////////////////////////////////////////////////////////

	private final Map<String, TemplateParameter> parameterMap = new HashMap<>();

	private String getParametersString()
	{
		List<TemplateParameter> list = new ArrayList<>( parameterMap.values() );
		list.sort( Comparator.comparingInt( TemplateParameter:: getIndex ) );
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for( TemplateParameter parameter : list )
		{
			if( first )
				first = false;
			else
				sb.append( ", " );

			sb.append( parameter.getName() );
			if( parameter.getGovernor() != null )
				sb.append( ": " ).append( parameter.getGovernor() );
		}
		return sb.toString();
	}

	@Override
	@Nullable
	public TemplateParameter getParameter( @NotNull String name )
	{
		return parameterMap.get( name );
	}

	private void addParameter( @NotNull TemplateParameter parameter )
	{
		parameterMap.put( parameter.getName(), parameter );
	}
}
