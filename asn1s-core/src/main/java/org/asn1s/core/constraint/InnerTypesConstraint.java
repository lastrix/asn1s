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
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.constraint.template.InnerTypesConstraintTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class InnerTypesConstraint implements Constraint
{
	public InnerTypesConstraint( @NotNull Type type, @NotNull List<Constraint> constraints, boolean partial )
	{
		this.type = type;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.constraints = constraints;
		this.partial = partial;

		if( type.getFamily() == Family.Choice )
		{
			boolean hasPresent = false;
			for( Constraint constraint : constraints )
			{
				ComponentConstraint componentConstraint = (ComponentConstraint)constraint;
				if( componentConstraint.getPresence() == Presence.Present )
				{
					if( hasPresent )
						throw new IllegalArgumentException( "For choice Types there should be at most one PRESENT keyword for component constraint, please see X.680, 51.8.10.2, G.5.6 for details." );
					else
						hasPresent = true;
				}
			}
		}
	}

	private final Type type;
	private final List<Constraint> constraints;
	private final boolean partial;


	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		type.accept( scope, value );

		if( type.getFamily() == Family.Choice )
			checkNamedValue( scope, value );
		else if( value.getKind() == Kind.NamedCollection )
			checkNamedCollection( scope, value );
		else
			throw new IllegalStateException();
	}

	private void checkNamedValue( Scope scope, Value value ) throws ValidationException, ResolutionException
	{
		if( value.getKind() != Kind.Name )
			throw new IllegalStateException();

		NamedValue namedValue = value.toNamedValue();
		NamedType namedType = type.getNamedType( namedValue.getName() );
		if( namedType == null )
			throw new ResolutionException( "There is no component with name '" + namedValue.getName() + "' in type: " + type );

		boolean checked = false;
		for( Constraint constraint : constraints )
		{
			String name = ( (ComponentConstraint)constraint ).getName();
			if( name.equals( namedValue.getName() ) )
			{
				constraint.check( scope, namedValue );
				checked = true;
				break;
			}
		}

		if( !partial && !checked )
			throw new ConstraintViolationException( "Component must be absent: " + namedValue.getName() );
	}

	private void checkNamedCollection( Scope scope, Value value ) throws ValidationException, ResolutionException
	{
		ValueCollection collection = value.toValueCollection();

		Collection<String> used = new HashSet<>();
		for( Constraint constraint : constraints )
		{
			used.add( ( (ComponentConstraint)constraint ).getName() );
			constraint.check( scope, value );
		}

		if( !partial )
		{
			List<? extends NamedType> components = type.getNamedTypes();
			for( NamedType component : components )
			{
				if( used.contains( component.getName() ) )
					continue;

				if( collection.getNamedValue( component.getName() ) != null )
					throw new ConstraintViolationException( "Component '" + component.getName() + "' must be absent" );

				used.add( component.getName() );
			}

			for( NamedValue namedValue : collection.asNamedValueList() )
				if( !used.contains( namedValue.getName() ) )
					throw new ConstraintViolationException( "Component '" + namedValue.getName() + "' must be absent" );
		}
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		InnerTypesConstraintTemplate.assertType( type );

		List<Constraint> list = new ArrayList<>( constraints.size() );
		for( Constraint component : constraints )
			list.add( component.copyForType( scope, type ) );

		return new InnerTypesConstraint( type, list, partial );
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "WITH COMPONENTS { " );
		if( partial )
			sb.append( "..., " );

		sb.append( StringUtils.join( constraints, ", " ) );
		sb.append( " }" );
		return sb.toString();
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		for( Constraint constraint : constraints )
			constraint.setScopeOptions( scope );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		for( Constraint constraint : constraints )
			constraint.assertConstraintTypes( allowedTypes );
	}
}
