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
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ClassFieldType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.RelationItem;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x681.ObjectValue;
import org.asn1s.core.constraint.InstanceOfConstraint;
import org.asn1s.core.constraint.TableConstraint;
import org.asn1s.core.type.x681.InstanceOfType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TableConstraintTemplate implements ConstraintTemplate
{
	private static final List<Kind> REQUIRED_KINDS = Collections.singletonList( Kind.Object );
	@NotNull
	private final Ref<Type> objectSet;

	private final List<RelationItem> relationItems;

	public TableConstraintTemplate( @NotNull Ref<Type> objectSet, @Nullable List<RelationItem> relationItems )
	{
		this.objectSet = objectSet;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.relationItems = relationItems;
	}

	@Override
	public Constraint build( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		if( type instanceof ClassFieldType )
			return buildTableConstraint( scope, (NamedType)type );

		if( relationItems == null || relationItems.isEmpty() )
			return buildElementsConstraint( scope, type );

		throw new ValidationException( "Unable to build table or type constraint for type: " + type );
	}

	@NotNull
	private Constraint buildElementsConstraint( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		Type resolve = objectSet.resolve( scope );
		resolve.validate( scope );
		if( !resolve.hasElementSetSpecs() )
			throw new ValidationException( "Is not elementSetSpecs: " + objectSet );

		ElementSetSpecs specs = resolve.asElementSetSpecs();
		if( type instanceof InstanceOfType )
			return buildInstanceOfConstraint( specs );

		return specs.copyForType( scope, type );
	}

	@SuppressWarnings( "unchecked" )
	@NotNull
	private static Constraint buildInstanceOfConstraint( Constraint specs ) throws IllegalValueException
	{
		Collection<Value> values = new ArrayList<>();
		specs.collectValues( values, Collections.singletonList( Kind.Object ) );
		return new InstanceOfConstraint( (List<ObjectValue>)(Object)values );
	}

	@NotNull
	private Constraint buildTableConstraint( @NotNull Scope scope, @NotNull NamedType type ) throws ResolutionException, ValidationException
	{
		Type resolve = objectSet.resolve( scope );
		resolve.validate( scope );
		if( !resolve.hasElementSetSpecs() )
			throw new ValidationException( "Is not element set specs" );

		List<Value> values = new ArrayList<>();
		resolve.asElementSetSpecs().collectValues( values, REQUIRED_KINDS );

		String name = type.getName();
		return new TableConstraint( (ClassFieldType)type, name, values, relationItems == null ? Collections.emptyList() : relationItems );
	}
}
