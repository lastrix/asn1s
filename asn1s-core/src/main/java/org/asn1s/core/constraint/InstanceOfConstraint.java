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

import org.apache.commons.lang3.tuple.Pair;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintType;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.InstanceOfTypeSelector;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ObjectIdentifierValue;
import org.asn1s.api.value.x680.OpenTypeValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class InstanceOfConstraint implements Constraint, InstanceOfTypeSelector
{
	public InstanceOfConstraint( List<ObjectValue> objects )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.objects = objects;
	}

	private final List<ObjectValue> objects;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value resolve = valueRef.resolve( scope );
		assert resolve.getKind() == Kind.NamedCollection;

		ValueCollection collection = resolve.toValueCollection();
		NamedValue value = collection.getNamedValue( "type-id" );
		assert value != null;
		assert value.getReferenceKind() == Kind.Oid;
		Type expectedType = selectTypeById( value.toObjectIdentifierValue() );

		if( expectedType == null )
			throw new ConstraintViolationException( "There is no type identified by: " + value );

		NamedValue valueField = collection.getNamedValue( "value" );
		assert valueField != null;
		assert valueField.getReferenceKind() == Kind.OpenType;

		OpenTypeValue typeValue = valueField.toOpenTypeValue();
		if( !Objects.equals( typeValue.getType(), expectedType ) )
			throw new ConstraintViolationException( "Type constraint failed for: " + typeValue + ". Expected type: " + expectedType );
	}

	@NotNull
	@Override
	public Type resolveInstanceOfType( @NotNull Scope scope ) throws ResolutionException
	{
		ObjectIdentifierValue typeId = findTypeId( scope );
		Type selected = selectTypeById( typeId );
		if( selected == null )
			throw new ResolutionException( "Unable to select type by object identifier: " + typeId );
		return selected;
	}

	private static ObjectIdentifierValue findTypeId( @NotNull Scope scope ) throws ResolutionException
	{
		Pair<Type[], Value[]> levels = scope.getValueLevels();
		Value[] values = levels.getValue();
		if( values.length != 1 )
			throw new ResolutionException( "Corrupted data, there is no registered InstanceOf Sequence in Scope structure" );
		Value level = values[0];
		if( level != null && level.getKind() == Kind.NamedCollection )
		{
			ValueCollection collection = level.toValueCollection();
			NamedValue value = collection.getNamedValue( "type-id" );
			if( value != null && value.getReferenceKind() == Kind.Oid )
				return value.toObjectIdentifierValue();
		}

		throw new ResolutionException( "Unable to find InstanceOf sequence in Scope structure" );
	}

	@Nullable
	private Type selectTypeById( Value value )
	{
		for( ObjectValue object : objects )
		{
			Ref<?> ref = object.getFields().get( "&id" );
			assert ref instanceof Value;
			if( value.isEqualTo( (Value)ref ) )
				return (Type)object.getFields().get( "&Type" );
		}
		return null;
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		throw new ValidationException();
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		scope.setScopeOption( TypeUtils.INSTANCE_OF_TYPE_KEY, this );
	}
}
