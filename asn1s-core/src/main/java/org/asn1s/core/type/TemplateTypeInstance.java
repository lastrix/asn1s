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
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TemplateTypeInstance extends AbstractType
{
	public TemplateTypeInstance( Ref<Type> ref, Collection<Ref<?>> arguments )
	{
		this.ref = ref;
		this.arguments.addAll( arguments );
	}

	private final List<Ref<?>> arguments = new ArrayList<>();
	private Ref<Type> ref;
	private DefinedTypeTemplate type;

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.templateInstanceScope( type, arguments );
	}

	@Override
	public Type getSibling()
	{
		return type;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		type.accept( getScope( scope ), valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		return type.optimize( getScope( scope ), valueRef );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( arguments.isEmpty() )
			throw new ValidationException( "No arguments defined" );

		Type resolved = ref.resolve( scope );
		if( !( resolved instanceof Template ) )
			throw new ValidationException( "TemplateTypeInstance sub type must be TemplateType" );

		Template<DefinedTypeTemplate> template = (Template<DefinedTypeTemplate>)resolved;
		String instanceNamespace = getNamespace() + template.getName() + '{' + StringUtils.join( arguments ) + "}.";
		type = template.newInstance( scope.templateInstanceScope( template, arguments ), instanceNamespace );
		if( type.getParameterCount() != arguments.size() )
			throw new ValidationException( "Template does not have same amount of parameters" );
	}

	@Nullable
	@Override
	public NamedType getNamedType( @NotNull String name )
	{
		if( type == null )
			return null;
		return type.getNamedType( name );
	}

	@NotNull
	@Override
	public List<? extends NamedType> getNamedTypes()
	{
		return type.getNamedTypes();
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		return type.getNamedValue( name );
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return type.getNamedValues();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return type.getFamily();
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return type.getEncoding( instructions );
	}

	@NotNull
	@Override
	public Type copy()
	{
		return new TemplateTypeInstance( ref, arguments );
	}

	@Override
	public String toString()
	{
		return ref + "{" + StringUtils.join( arguments, ", " ) + '}';
	}

	@Override
	public ElementSetSpecs asElementSetSpecs()
	{
		return type.asElementSetSpecs();
	}

	@Override
	public boolean hasElementSetSpecs()
	{
		return type.hasElementSetSpecs();
	}

	@Override
	protected void onDispose()
	{
		type = null;
		ref = null;
		arguments.clear();
	}
}
