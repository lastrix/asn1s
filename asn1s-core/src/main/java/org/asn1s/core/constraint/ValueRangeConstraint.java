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
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.core.constraint.template.ValueRangeConstraintTemplate;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ValueRangeConstraint implements Constraint
{
	private static final Value MAX_INTEGER = new IntegerValueInt( Integer.MAX_VALUE );
	private static final Value MIN_INTEGER = new IntegerValueInt( Integer.MIN_VALUE );

	public ValueRangeConstraint( @NotNull Type type, @Nullable Value min, boolean minLt, @Nullable Value max, boolean maxGt )
	{
		this.type = type;
		this.min = min;
		this.minLt = minLt;
		this.max = max;
		this.maxGt = maxGt;
	}

	private final Type type;
	private final Value min;
	private final boolean minLt;
	private final Value max;
	private final boolean maxGt;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		try
		{
			type.accept( scope, value );
		} catch( IllegalValueException e )
		{
			throw new ConstraintViolationException( "Value is not accepted by type: " + value, e );
		}
		value = RefUtils.toBasicValue( scope, value );

		if( min != null )
		{
			int result = min.compareTo( value );
			if( result > 0 || minLt && result == 0 )
				throw new ConstraintViolationException( "Lower bound constraint failure for: " + value );
		}

		if( max != null )
		{
			int result = max.compareTo( value );
			if( result < 0 || maxGt && result == 0 )
				throw new ConstraintViolationException( "Upper bound constraint failure for: " + value );
		}
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		if( !ValueRangeConstraintTemplate.isAllowed( type.getFamily() ) )
			throw new ValidationException( "Unable to apply to type: " + type );

		if( min != null )
			checkValueAccepted( scope, type, min );

		if( max != null )
			checkValueAccepted( scope, type, max );

		return new ValueRangeConstraint( type, min, minLt, max, maxGt );
	}

	@NotNull
	@Override
	public Value getMinimumValue( @NotNull Scope scope ) throws ResolutionException
	{
		return min == null ? MIN_INTEGER : min;
	}

	@NotNull
	@Override
	public Value getMaximumValue( @NotNull Scope scope ) throws ResolutionException
	{
		return max == null ? MAX_INTEGER : max;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( min == null ? "MIN" : min );

		if( minLt )
			sb.append( '<' );

		sb.append( ".." );

		if( maxGt )
			sb.append( '<' );

		sb.append( max == null ? "MAX" : max );
		return sb.toString();
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		if( !allowedTypes.contains( ConstraintType.ValueRange ) )
			throw new ValidationException( "'ValueRange' constraint is not allowed" );
	}

	private static void checkValueAccepted( @NotNull Scope scope, @NotNull Type type, Ref<Value> value ) throws ResolutionException, ValidationException
	{
		try
		{
			type.accept( scope, value );
		} catch( ConstraintViolationException e )
		{
			throw new ValidationException( "Value is not accepted: " + value, e );
		}
	}
}
