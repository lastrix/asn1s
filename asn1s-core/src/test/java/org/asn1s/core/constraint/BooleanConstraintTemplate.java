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
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.ConstraintType;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

class BooleanConstraintTemplate implements ConstraintTemplate
{
	BooleanConstraintTemplate( boolean shouldFail )
	{
		this.shouldFail = shouldFail;
	}

	private final boolean shouldFail;

	@Override
	public Constraint build( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		return new ConstraintImpl( shouldFail );
	}

	private static final class ConstraintImpl implements Constraint
	{
		private final boolean shouldFail;

		private ConstraintImpl( boolean shouldFail )
		{
			this.shouldFail = shouldFail;
		}

		@Override
		public void check( Scope scope, Ref<Value> valueRef ) throws ConstraintViolationException
		{
			if( shouldFail )
				throw new ConstraintViolationException();
		}

		@NotNull
		@Override
		public Constraint copyForType( @NotNull Scope scope, @NotNull Type type )
		{
			return this;
		}

		@Override
		public void assertConstraintTypes( Collection<ConstraintType> allowedTypes )
		{

		}
	}
}
