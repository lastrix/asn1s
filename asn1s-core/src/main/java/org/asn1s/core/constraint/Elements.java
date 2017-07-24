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

public class Elements implements Constraint
{
	public Elements( @NotNull Constraint elements, @Nullable Constraint exclusion )
	{
		this.elements = elements;
		this.exclusion = exclusion;
	}

	private final Constraint elements;
	private final Constraint exclusion;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		elements.check( scope, valueRef );

		if( exclusion != null )
		{
			ConstraintViolationException violation = null;
			try
			{
				exclusion.check( scope, valueRef );
			} catch( ConstraintViolationException e )
			{
				violation = e;
			}
			if( violation == null )
				throw new ConstraintViolationException( "Value must not be in: " + exclusion );
		}
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		return new Elements( elements.copyForType( scope, type ), exclusion == null ? null : exclusion.copyForType( scope, type ) );
	}

	@NotNull
	@Override
	public Value getMinimumValue( @NotNull Scope scope ) throws ResolutionException
	{
		Value value = elements.getMinimumValue( scope );
		assertExclusions( scope, value, "Constraint structure is too complex to find minimum value" );
		return value;
	}

	@NotNull
	@Override
	public Value getMaximumValue( @NotNull Scope scope ) throws ResolutionException
	{
		Value value = elements.getMaximumValue( scope );
		assertExclusions( scope, value, "Constraint structure is too complex to find maximum value" );
		return value;
	}

	@Override
	public String toString()
	{
		if( exclusion == null )
			return elements.toString();
		return elements + " EXCEPT " + exclusion;
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		elements.setScopeOptions( scope );
		if( exclusion != null )
			exclusion.setScopeOptions( scope );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		elements.assertConstraintTypes( allowedTypes );
		if( exclusion != null )
			exclusion.assertConstraintTypes( allowedTypes );
	}

	@Override
	public void collectValues( @NotNull Collection<Value> values, @NotNull Collection<Kind> requiredKinds ) throws IllegalValueException
	{
		elements.collectValues( values, requiredKinds );
		if( exclusion != null )
			exclusion.collectValues( values, requiredKinds );
	}

	private void assertExclusions( @NotNull Scope scope, Ref<Value> value, String message ) throws ResolutionException
	{
		if( exclusion != null )
		{
			try
			{
				exclusion.check( scope, value );
				throw new ResolutionException( message );
			} catch( ValidationException ignored )
			{

			}
		}
	}
}
