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

package org.asn1s.api.constraint;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * How to work with constraints:
 * 1. Build constraint for specific type using resolve(Scope), where Scope must be Type
 * 2. Run checks for whatever reason on any value that was accepted by type
 */
public interface Constraint
{
	void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException;

	default void collectValues( @NotNull Collection<Value> values, @NotNull Collection<Kind> requiredKinds ) throws IllegalValueException
	{
		throw new UnsupportedOperationException();
	}

	@NotNull
	Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException;

	default void setScopeOptions( Scope scope )
	{

	}

	@NotNull
	default Value getMinimumValue( @NotNull Scope scope ) throws ResolutionException
	{
		throw new UnsupportedOperationException( "Not supported for: " + getClass().getName() );
	}

	@NotNull
	default Value getMaximumValue( @NotNull Scope scope ) throws ResolutionException
	{
		throw new UnsupportedOperationException( "Not supported for: " + getClass().getName() );
	}

	/**
	 * Check constraint tree has constraints from allowed types only.
	 *
	 * @param allowedTypes the collection of allowed types
	 * @throws ValidationException if constraint type is not allowed by parameter
	 */
	void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException;
}
