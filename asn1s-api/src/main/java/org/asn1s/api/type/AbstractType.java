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

package org.asn1s.api.type;

import org.asn1s.api.Scope;
import org.asn1s.api.State;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractType implements Type
{
	protected AbstractType()
	{

	}

	private State state = State.None;
	private String namespace;

	@Override
	public String getNamespace()
	{
		return namespace;
	}

	@Override
	public void setNamespace( @Nullable String namespace )
	{
		this.namespace = namespace;
	}

	@Override
	public final State getState()
	{
		return state;
	}

	@Override
	public final void dispose()
	{
		if( isDisposed() )
			return;

		try
		{
			onDispose();
		} finally
		{
			state = State.Disposed;
		}
	}

	@Override
	public Type resolve( Scope scope ) throws ResolutionException
	{
		return this;
	}

	@Override
	public final void validate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( getState() != State.None )
			return;

		state = State.Validating;

		try
		{
			onValidate( scope );
			state = State.Done;
		} catch( Exception e )
		{
			state = State.Failed;
			//noinspection ProhibitedExceptionThrown
			throw e;
		}
	}

	protected abstract void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException;

	protected abstract void onDispose();
}
