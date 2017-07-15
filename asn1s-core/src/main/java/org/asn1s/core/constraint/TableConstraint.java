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
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ClassFieldType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.RelationItem;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class TableConstraint implements Constraint
{
	private final ClassFieldType type;
	private final String name;
	private final List<Value> values;
	private final List<RelationItem> relationItems;

	public TableConstraint( ClassFieldType type, String name, List<Value> values, List<RelationItem> relationItems )
	{
		this.type = type;
		this.name = name;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.values = values;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.relationItems = relationItems;
	}

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value checkValue = RefUtils.toBasicValue( scope, valueRef );

		for( Value value : values )
		{
			assert value.getKind() == Kind.Object;
			if( isFiltered( value.toObjectValue(), scope ) )
				continue;
			Ref<?> ref = value.toObjectValue().getFields().get( name );
			if( !( ref instanceof Value ) )
				throw new ResolutionException( "Unable to use non-value ref: " + ref );

			if( ( (Value)ref ).isEqualTo( checkValue ) )
				return;
		}

		throw new ConstraintViolationException( "Table constraint failure for value: " + valueRef + ". Type: " + type );
	}

	private boolean isFiltered( ObjectValue value, Scope scope ) throws ValidationException
	{
		if( relationItems == null || relationItems.isEmpty() )
			return false;

		for( RelationItem item : relationItems )
		{
			if( isAcceptedByItem( item, value, scope ) )
				return false;
		}

		return true;
	}

	private static boolean isAcceptedByItem( RelationItem item, ObjectValue value, Scope scope ) throws ValidationException
	{
		if( !item.getPath().isEmpty() )
			throw new UnsupportedOperationException( "Unable to check inside custom paths, use root or level values instead." );

		Pair<Type[], Value[]> levels = scope.getValueLevels();
		Value levelValue = levels.getValue()[item.getLevel()];
		Type levelType = levels.getKey()[item.getLevel()];

		if( levelValue.getKind() != Kind.NamedCollection )
			throw new IllegalValueException( "Unable to fetch collection value: " + levelValue );

		Value filter = levelValue.toValueCollection().getNamedValue( item.getName() );
		if( filter == null )
			throw new IllegalValueException( "There is no field with name: " + item.getName() );
		Type filterType = levelType.getNamedType( item.getName() );
		while( filterType != null && !( filterType instanceof ClassFieldType ) )
			filterType = filterType.getSibling();

		if( filterType == null )
			throw new ValidationException( "Unable to fetch filter type name '" + item.getName() + "' from: " + levelType );

		String filterName = ( (NamedType)filterType ).getName();

		Ref<?> ref = value.getFields().get( filterName );
		if( !( ref instanceof Value ) )
			throw new ValidationException( "Not an value ref: " + ref );

		return filter.isEqualTo( (Value)ref );
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
}
