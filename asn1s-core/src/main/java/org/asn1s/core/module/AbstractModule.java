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

package org.asn1s.core.module;

import org.asn1s.api.Disposable;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.*;
import org.asn1s.core.scope.ModuleScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

abstract class AbstractModule implements Module
{
	AbstractModule( @NotNull ModuleReference name, @Nullable ModuleResolver resolver )
	{
		this.name = name;
		this.resolver = resolver == null ? new ModuleSet() : resolver;
		typeResolver = new TypeResolverImpl( name.getName(), this.resolver );
		valueResolver = new ValueResolverImpl( name.getName(), this.resolver );
	}

	private final ModuleReference name;
	private final ModuleResolver resolver;
	private final TypeResolverImpl typeResolver;
	private final ValueResolverImpl valueResolver;
	private final Collection<Disposable> disposables = new ArrayList<>();

	@Nullable
	@Override
	public ModuleResolver getModuleResolver()
	{
		return resolver;
	}

	@NotNull
	@Override
	public TypeResolver getTypeResolver()
	{
		return typeResolver;
	}

	@NotNull
	@Override
	public ValueResolver getValueResolver()
	{
		return valueResolver;
	}

	@NotNull
	@Override
	public ModuleReference getModuleReference()
	{
		return name;
	}

	@NotNull
	@Override
	public String getModuleName()
	{
		return name.getName();
	}

	@Override
	public Scope createScope()
	{
		return new ModuleScope( this );
	}

	/////////////////////////////// Misc operations ////////////////////////////////////////////////////////////////////
	@Override
	public void dispose()
	{
		disposables.forEach( Disposable:: dispose );
		disposables.clear();
		typeResolver.dispose();
		valueResolver.dispose();
	}

	@Override
	public void addDisposable( Disposable disposable )
	{
		disposables.add( disposable );
	}

	@Override
	public final void validate( boolean types, boolean values ) throws ValidationException, ResolutionException
	{
		onValidate();
		Scope scope = createScope();
		if( values )
			valueResolver.validate( scope );

		if( types )
			typeResolver.validate( scope );
	}

	protected abstract void onValidate();
}
