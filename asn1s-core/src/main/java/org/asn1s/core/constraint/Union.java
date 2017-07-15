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

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintType;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Union implements Constraint
{
	public Union( List<Constraint> intersections )
	{
		this.intersections = new ArrayList<>( intersections );
	}

	private final Collection<Constraint> intersections;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		for( Constraint intersection : intersections )
			intersection.check( scope, valueRef );
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		List<Constraint> list = new ArrayList<>();
		for( Constraint intersection : intersections )
			list.add( intersection.copyForType( scope, type ) );

		return new Union( list );
	}

	@NotNull
	@Override
	public Value getMinimumValue( @NotNull Scope scope ) throws ResolutionException
	{
		Value minimum = null;
		for( Constraint intersection : intersections )
		{
			Value current = intersection.getMinimumValue( scope );
			if( minimum == null || current.compareTo( minimum ) < 0 )
				minimum = current;
		}
		assert minimum != null;
		return minimum;
	}

	@NotNull
	@Override
	public Value getMaximumValue( @NotNull Scope scope ) throws ResolutionException
	{
		Value maximum = null;
		for( Constraint intersection : intersections )
		{
			Value current = intersection.getMinimumValue( scope );
			if( maximum == null || current.compareTo( maximum ) > 0 )
				maximum = current;
		}
		assert maximum != null;
		return maximum;
	}

	@Override
	public String toString()
	{
		return StringUtils.join( intersections, " ^ " );
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		for( Constraint intersection : intersections )
			intersection.setScopeOptions( scope );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		for( Constraint intersection : intersections )
			intersection.assertConstraintTypes( allowedTypes );
	}
}
