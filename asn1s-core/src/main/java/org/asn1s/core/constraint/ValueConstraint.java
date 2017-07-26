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
import org.asn1s.api.type.ClassFieldType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.core.constraint.template.ValueConstraintTemplate;
import org.asn1s.core.type.x681.InstanceOfType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * X.680, p 51.2
 */
public class ValueConstraint implements Constraint
{
	public ValueConstraint( @NotNull Type type, @NotNull Value value )
	{
		if( type instanceof InstanceOfType )
			throw new IllegalArgumentException( "Unable to use ValueConstraint for InstanceOfType. Use InstanceOfConstraint instead" );
		this.type = type;
		this.value = value;
	}

	private final Type type;
	private final Value value;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value ourValue = value;

		Value resolve = valueRef.resolve( scope );
		if( type instanceof ClassFieldType )
		{
			assert value.getKind() == Kind.OBJECT;
			Ref<?> newValue = value.toObjectValue().getFields().get( ( (NamedType)type ).getName() );
			assert newValue instanceof Value;
			ourValue = (Value)newValue;
		}

		if( !ourValue.isEqualTo( resolve ) )
			throw new ConstraintViolationException( "Illegal value: " + valueRef );
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		if( !ValueConstraintTemplate.isAllowed( type.getFamily() ) )
			throw new ResolutionException( "Unable to apply to type: " + type );

		try
		{
			type.accept( scope, value );
		} catch( ConstraintViolationException e )
		{
			throw new ValidationException( "Value is not accepted: " + value, e );
		}

		return new ValueConstraint( type, value );
	}

	@NotNull
	@Override
	public Value getMinimumValue( @NotNull Scope scope ) throws ResolutionException
	{
		return value;
	}

	@NotNull
	@Override
	public Value getMaximumValue( @NotNull Scope scope ) throws ResolutionException
	{
		return value;
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		if( !allowedTypes.contains( ConstraintType.VALUE ) )
			throw new ValidationException( "'Value' constraint is not allowed" );
	}

	@Override
	public void collectValues( @NotNull Collection<Value> values, @NotNull Collection<Kind> requiredKinds ) throws IllegalValueException
	{
		if( !requiredKinds.contains( value.getKind() ) )
			throw new IllegalStateException( "Value kind is prohibited: " + value.getKind() );

		values.add( value );
	}

	@Override
	public String toString()
	{
		return String.valueOf( value );
	}
}
