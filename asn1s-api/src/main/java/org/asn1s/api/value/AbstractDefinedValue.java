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

package org.asn1s.api.value;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.State;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.Type;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDefinedValue implements DefinedValue
{
	protected AbstractDefinedValue( Module module, String name )
	{
		this.module = module;
		this.name = name;
	}

	private State state = State.NONE;
	private Module module;
	private final String name;

	public Module getModule()
	{
		return module;
	}

	@NotNull
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public State getState()
	{
		return state;
	}

	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		if( !isValidated() )
			try
			{
				validate( getScope( scope ) );
			} catch( ValidationException e )
			{
				throw new ResolutionException( "Unable to validate", e );
			}

		return getValue();
	}

	@Override
	public void validate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( state != State.NONE )
			return;

		state = State.VALIDATING;

		try
		{
			onValidate( scope );
			state = State.DONE;
		} catch( Exception e )
		{
			state = State.FAILED;
			//noinspection ProhibitedExceptionThrown
			throw e;
		}
	}

	@Override
	public void dispose()
	{
		if( isDisposed() )
			return;

		state = State.DISPOSED;

		onDispose();
		module = null;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		assert isValidated();
		//noinspection CompareToUsesNonFinalVariable
		return getValue().compareTo( o );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof AbstractDefinedValue ) ) return false;

		AbstractDefinedValue definedValue = (AbstractDefinedValue)obj;

		if( !getName().equals( definedValue.getName() ) ) return false;
		//noinspection SimplifiableIfStatement
		if( !getTypeRef().equals( definedValue.getTypeRef() ) ) return false;
		return getValueRef().equals( definedValue.getValueRef() );
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + getTypeRef().hashCode();
		result = 31 * result + getValueRef().hashCode();
		return result;
	}

	@Override
	public Ref<Value> toRef()
	{
		return new ValueNameRef( getName(), module.getModuleName() );
	}

	protected abstract void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException;

	protected abstract void onDispose();

	protected abstract Ref<Type> getTypeRef();

	protected abstract Ref<Value> getValueRef();
}
