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
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.constraint.template.ComponentConstraintTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ComponentConstraint implements Constraint
{
	public ComponentConstraint( @NotNull String name, @Nullable Constraint constraint, @NotNull Presence presence )
	{
		this.name = name;
		this.constraint = constraint;
		this.presence = presence;
	}

	private final String name;
	private final Constraint constraint;
	private final Presence presence;

	public String getName()
	{
		return name;
	}

	public Presence getPresence()
	{
		return presence;
	}

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		if( value.getKind() == Kind.NAMED_COLLECTION )
		{
			NamedValue actualValue = value.toValueCollection().getNamedValue( name );

			if( actualValue == null )
			{
				if( presence == Presence.PRESENT )
					throw new ConstraintViolationException( "Field is not present: " + name );
			}
			else
			{
				if( presence == Presence.ABSENT )
					throw new ConstraintViolationException( "Field must not be present: " + name );

				if( constraint != null )
					constraint.check( scope, actualValue );
			}
		}
		else if( value.getKind() == Kind.NAME )
		{
			NamedValue actual = value.toNamedValue();
			if( !actual.getName().equals( name ) )
				throw new IllegalStateException();

			if( presence == Presence.ABSENT )
				throw new ConstraintViolationException( "Field must not be present: " + name );

			if( constraint != null )
				constraint.check( scope, actual );
		}
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		ComponentConstraintTemplate.assertType( type );
		NamedType namedType = type.getNamedType( name );
		if( namedType == null )
			throw new ValidationException( "There is no field '" + name + "' in type: " + type );
		return new ComponentConstraint( name, constraint == null ? null : constraint.copyForType( scope, namedType ), presence );
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( name ).append( ' ' );
		if( constraint != null )
			sb.append( constraint );
		if( presence != Presence.NONE )
			sb.append( ' ' ).append( presence );
		return sb.toString();
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		if( constraint != null )
			constraint.setScopeOptions( scope );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		if( constraint != null )
			constraint.assertConstraintTypes( allowedTypes );
	}
}
