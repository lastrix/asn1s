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

package org.asn1s.core.type;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractNestingType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

public final class ConstrainedType extends AbstractNestingType
{
	public ConstrainedType( @NotNull ConstraintTemplate constraintTemplate, @NotNull Ref<Type> reference )
	{
		super( reference );
		this.constraintTemplate = constraintTemplate;
	}

	private ConstraintTemplate constraintTemplate;
	private Constraint constraint;

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		if( constraint != null )
			constraint.setScopeOptions( parentScope );
		return parentScope;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = getScope( scope );
		getSibling().accept( scope, valueRef );
		constraint.check( scope, valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		Value value = getSibling().optimize( scope, valueRef );
		constraint.check( scope, value );
		return value;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		scope = getScope( scope );
		super.onValidate( scope );
		constraint = constraintTemplate.build( scope, getSibling() );
	}

	@NotNull
	@Override
	public Type copy()
	{
		return new ConstrainedType( constraintTemplate, cloneSibling() );
	}

	@Override
	public String toString()
	{
		return getSiblingRef() + " " + constraint;
	}

	@Override
	protected void onDispose()
	{
		super.onDispose();
		constraintTemplate = null;
		constraint = null;
	}

	@Override
	public ElementSetSpecs asElementSetSpecs()
	{
		return (ElementSetSpecs)constraint;
	}

	@Override
	public boolean hasConstraint()
	{
		return true;
	}

	@Override
	public boolean hasElementSetSpecs()
	{
		return constraint instanceof ElementSetSpecs;
	}
}
