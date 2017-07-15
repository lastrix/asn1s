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

package org.asn1s.core.constraint.template;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.core.constraint.ValueRangeConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

public class ValueRangeConstraintTemplate implements ConstraintTemplate
{
	public ValueRangeConstraintTemplate( @Nullable Ref<Value> minRef, boolean minLt, @Nullable Ref<Value> maxRef, boolean maxGt )
	{
		this.minRef = minRef;
		this.minLt = minLt;
		this.maxRef = maxRef;
		this.maxGt = maxGt;
	}

	private final Ref<Value> minRef;
	private final boolean minLt;
	private final Ref<Value> maxRef;
	private final boolean maxGt;

	@Override
	public Constraint build( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		if( isAllowed( type.getFamily() ) )
		{
			Value min = minRef == null ? null : minRef.resolve( scope );
			Value max = maxRef == null ? null : maxRef.resolve( scope );

			if( min != null )
				min = type.optimize( scope, min );
			if( max != null )
				max = type.optimize( scope, max );

			return new ValueRangeConstraint( type, min, minLt, max, maxGt );
		}
		throw new ValidationException( "ValueRange constraint can not be built for: " + type );
	}

	public static boolean isAllowed( @NotNull Family family )
	{
		return ALLOWED.contains( family );
	}

	private static final Collection<Family> ALLOWED =
			EnumSet.copyOf(
					Arrays.asList(
							Family.Integer,
							Family.Real,
							Family.RestrictedString
					)
			);
}
