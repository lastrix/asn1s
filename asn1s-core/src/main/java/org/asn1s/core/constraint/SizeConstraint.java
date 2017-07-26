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
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintType;
import org.asn1s.api.constraint.ConstraintUtils;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.core.constraint.template.SizeConstraintTemplate;
import org.asn1s.core.value.x680.IntegerValueInt;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * X.680, p 51.5
 */
public class SizeConstraint implements Constraint
{

	public SizeConstraint( Type type, Constraint constraint, int minimumValue )
	{
		if( minimumValue < 0 )
			throw new IllegalArgumentException( "Parameter 'minimumValue' can not be negative" );

		this.type = type;
		this.constraint = constraint;
		this.minimumValue = minimumValue;
	}

	private final Type type;
	private final Constraint constraint;
	private final int minimumValue;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		type.accept( scope, value );
		value = RefUtils.toBasicValue( scope, value );

		int size = -1;
		if( value.getKind() == Kind.COLLECTION || value.getKind() == Kind.NAMED_COLLECTION )
			size = value.toValueCollection().size();
		else if( value.getKind() == Kind.BYTE_ARRAY )
			size = value.toByteArrayValue().size( type.getFamily() == Family.BIT_STRING );
		else if( value.getKind() == Kind.C_STRING )
			size = value.toStringValue().length();

		if( size == -1 )
			throw new IllegalValueException( "Unable to get size of value: " + value );

		try
		{
			constraint.check( scope, new IntegerValueInt( size ) );
		} catch( ConstraintViolationException e )
		{
			throw new ConstraintViolationException( "Value size is illegal: '" + size + "', allowed minimum is '" + minimumValue + '\'', e );
		}
	}

	@Override
	public String toString()
	{
		return "SIZE " + constraint;
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		if( !SizeConstraintTemplate.isAllowed( type.getFamily() ) )
			throw new ValidationException( "Type is not allowed for Size constraint: " + type );

		Type intType = UniversalType.INTEGER.ref().resolve( scope );
		return new SizeConstraint( type, constraint.copyForType( scope, intType ), minimumValue );
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		scope.setScopeOption( ConstraintUtils.OPTION_HAS_SIZE_CONSTRAINT, true );
		scope.setScopeOption( ConstraintUtils.OPTION_SIZE_CONSTRAINT, minimumValue );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		if( !allowedTypes.contains( ConstraintType.SIZE ) )
			throw new ValidationException( "'Size' constraint is not allowed" );
	}
}
