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
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TemplateValueInstance implements Value
{
	public TemplateValueInstance( @NotNull Ref<Value> valueRef, @NotNull List<Ref<?>> refs )
	{
		this.valueRef = valueRef;
		this.refs = new ArrayList<>( refs );
	}

	private final Ref<Value> valueRef;
	private final List<Ref<?>> refs;

	private Ref<Value> getValueRef()
	{
		return valueRef;
	}

	private List<Ref<?>> getRefs()
	{
		return refs;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		Value value = valueRef.resolve( scope );
		if( !( value instanceof Template ) )
			throw new ResolutionException( "ValueRef must point to value template" );

		Template<Value> template = (Template<Value>)value;
		if( template.getParameterCount() != refs.size() )
			throw new ResolutionException( "Template does not have same amount of parameters" );
		scope = scope.templateInstanceScope( template, refs );
		return template.newInstance( scope, template.getName() + '{' + StringUtils.join( getRefs(), ", " ) + '}' );
	}

	@NotNull
	@Override
	public Kind getKind()
	{
		return Kind.TemplateInstance;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		return getKind().compareTo( o.getKind() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof TemplateValueInstance ) ) return false;

		TemplateValueInstance instance = (TemplateValueInstance)obj;

		//noinspection SimplifiableIfStatement
		if( !getValueRef().equals( instance.getValueRef() ) ) return false;
		return getRefs().equals( instance.getRefs() );
	}

	@Override
	public int hashCode()
	{
		int result = valueRef.hashCode();
		result = 31 * result + refs.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return valueRef + "{" + StringUtils.join( refs, ", " ) + '}';
	}
}
