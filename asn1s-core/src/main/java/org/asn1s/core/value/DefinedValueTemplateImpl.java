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

package org.asn1s.core.value;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.Module;
import org.asn1s.api.value.Value;
import org.asn1s.core.CoreUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DefinedValueTemplateImpl extends DefinedValueImpl implements Template<Value>
{
	public DefinedValueTemplateImpl( @NotNull Module module, @NotNull String name, @NotNull Iterable<TemplateParameter> parameters )
	{
		this( module, name, parameters, true );
	}

	private DefinedValueTemplateImpl( @NotNull Module module, @NotNull String name, @NotNull Iterable<TemplateParameter> parameters, boolean template )
	{
		super( module, name );
		this.template = template;
		for( TemplateParameter parameter : parameters )
			parameterMap.put( parameter.getName(), parameter );
	}

	private final Map<String, TemplateParameter> parameterMap = new HashMap<>();
	private final boolean template;

	private Map<String, TemplateParameter> getParameterMap()
	{
		return parameterMap;
	}

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.templateScope( this );
	}

	@Nullable
	@Override
	public TemplateParameter getParameter( @NotNull String name )
	{
		return parameterMap.get( name );
	}

	@Override
	public boolean isTemplate()
	{
		return template;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		setType( getTypeRef().resolve( getModule().createScope() ) );
		getType().validate( scope );
		scope = getScope( scope );
		CoreUtils.assertParameterMap( scope, parameterMap );

		if( !isTemplate() )
			setValue( getType().optimize( scope, getValueRef() ) );
	}

	@Override
	protected void onDispose()
	{
		parameterMap.clear();
		super.onDispose();
	}

	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		if( isTemplate() )
			return this;

		return super.resolve( scope );
	}

	@Override
	public int getParameterCount()
	{
		return parameterMap.size();
	}

	@Override
	public Value newInstance( Scope scope, String namespace ) throws ResolutionException
	{
		DefinedValueTemplateImpl result = new DefinedValueTemplateImpl( getModule(), getName(), parameterMap.values(), false );
		result.setTypeRef( getTypeRef() );
		result.setValueRef( getValueRef() );
		try
		{
			result.validate( scope );
		} catch( ValidationException e )
		{
			throw new ResolutionException( "Unable to create new template instance", e );
		}
		return result.getValue();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof DefinedValueTemplateImpl ) ) return false;
		if( !super.equals( obj ) ) return false;

		DefinedValueTemplateImpl valueTemplate = (DefinedValueTemplateImpl)obj;

		return isTemplate() == valueTemplate.isTemplate() && getParameterMap().equals( valueTemplate.getParameterMap() );
	}

	@Override
	public int hashCode()
	{
		int result = super.hashCode();
		result = 31 * result + parameterMap.hashCode();
		result = 31 * result + ( isTemplate() ? 1 : 0 );
		return result;
	}

	@Override
	public String toString()
	{
		if( isValidated() )
			return getName() + ' ' + getType() + " {" + paramsAsString() + "} ::= " + getValue();

		return getName();
	}

	private String paramsAsString()
	{
		List<TemplateParameter> parameters = new ArrayList<>( parameterMap.values() );
		parameters.sort( Comparator.comparingInt( TemplateParameter:: getIndex ) );
		return StringUtils.join( parameters, ", " );
	}
}
