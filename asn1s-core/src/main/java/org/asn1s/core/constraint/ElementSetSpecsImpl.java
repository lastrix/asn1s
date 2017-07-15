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

package org.asn1s.core.constraint;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintType;
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ElementSetSpecsImpl implements ElementSetSpecs
{
	public ElementSetSpecsImpl( @NotNull Type type, @NotNull Constraint setSpec, boolean extensible, @Nullable Constraint additionalSetSpec )
	{
		if( !extensible && additionalSetSpec != null )
			throw new IllegalArgumentException( "'extensible' must be true when 'additionalSetSpec' is not null." );

		this.type = type;
		this.setSpec = setSpec;
		this.extensible = extensible;
		this.additionalSetSpec = additionalSetSpec;
	}

	private final Type type;
	private final Constraint setSpec;
	private final boolean extensible;
	private final Constraint additionalSetSpec;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		type.accept( scope, value );
		try
		{
			setSpec.check( scope, value );
		} catch( ConstraintViolationException e )
		{
			if( !extensible )
				throw e;
		}

		if( additionalSetSpec != null )
		{
			try
			{
				additionalSetSpec.check( scope, value );
			} catch( ConstraintViolationException ignored )
			{
				// nothing to do
			}
		}
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		return new ElementSetSpecsImpl( type, setSpec.copyForType( scope, type ), extensible, additionalSetSpec == null ? null : additionalSetSpec.copyForType( scope, type ) );
	}

	@NotNull
	@Override
	public Value getMinimumValue( @NotNull Scope scope ) throws ResolutionException
	{
		Value minimumValue = setSpec.getMinimumValue( scope );
		if( additionalSetSpec != null )
		{
			Value additionalMinimumValue = additionalSetSpec.getMinimumValue( scope );
			return minimumValue.compareTo( additionalMinimumValue ) <= 0 ? minimumValue : additionalMinimumValue;
		}
		return minimumValue;
	}

	@NotNull
	@Override
	public Value getMaximumValue( @NotNull Scope scope ) throws ResolutionException
	{
		Value maximumValue = setSpec.getMaximumValue( scope );
		if( additionalSetSpec != null )
		{
			Value additionalMaximumValue = additionalSetSpec.getMinimumValue( scope );
			return maximumValue.compareTo( additionalMaximumValue ) >= 0 ? maximumValue : additionalMaximumValue;
		}
		return maximumValue;
	}

	@Override
	public String toString()
	{
		if( additionalSetSpec != null )
			return "(" + setSpec + ", ..., " + additionalSetSpec + ')';

		if( extensible )
			return "(" + setSpec + ", ... )";

		return "(" + setSpec + ')';
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		setSpec.setScopeOptions( scope );
		if( additionalSetSpec != null )
			additionalSetSpec.setScopeOptions( scope );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		setSpec.assertConstraintTypes( allowedTypes );
		if( additionalSetSpec != null )
			additionalSetSpec.assertConstraintTypes( allowedTypes );
	}

	@Override
	public void collectValues( @NotNull Collection<Value> values, @NotNull Collection<Kind> requiredKinds ) throws IllegalValueException
	{
		setSpec.collectValues( values, requiredKinds );
		if( additionalSetSpec != null )
			additionalSetSpec.collectValues( values, requiredKinds );
	}
}
