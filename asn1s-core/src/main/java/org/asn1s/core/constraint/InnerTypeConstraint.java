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
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.constraint.template.InnerTypeConstraintTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class InnerTypeConstraint implements Constraint
{
	public InnerTypeConstraint( Type type, Constraint constraint )
	{
		this.type = type;
		this.constraint = constraint;
	}

	private final Type type;
	private final Constraint constraint;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		type.accept( scope, value );

		value = RefUtils.toBasicValue( scope, value );
		if( value.getKind() == Kind.NAMED_COLLECTION )
		{
			for( NamedValue componentValue : value.toValueCollection().asNamedValueList() )
				constraint.check( scope, componentValue );
		}
		else if( value.getKind() == Kind.COLLECTION )
		{
			for( Ref<Value> ref : value.toValueCollection().asValueList() )
				constraint.check( scope, ref );
		}
		else
			throw new IllegalStateException();
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		InnerTypeConstraintTemplate.assertType( type );
		List<? extends NamedType> types = type.getNamedTypes();
		if( types.size() != 1 )
			throw new ValidationException( "Type should have just 1 component: " + type );

		NamedType component = types.get( 0 );
		return new InnerTypeConstraint( type, constraint.copyForType( scope, component ) );
	}

	@Override
	public String toString()
	{
		return "WITH COMPONENT " + constraint;
	}

	@Override
	public void setScopeOptions( Scope scope )
	{
		constraint.setScopeOptions( scope );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		constraint.assertConstraintTypes( allowedTypes );
	}
}
