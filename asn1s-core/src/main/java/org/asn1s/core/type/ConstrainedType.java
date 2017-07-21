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
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class ConstrainedType extends AbstractType
{
	public ConstrainedType( @NotNull ConstraintTemplate constraintTemplate, @Nullable Ref<Type> reference )
	{
		this.constraintTemplate = constraintTemplate;
		this.reference = reference;
		if( reference instanceof Type )
			type = (Type)reference;
	}

	private ConstraintTemplate constraintTemplate;
	private Constraint constraint;
	private Ref<Type> reference;
	private Type type;

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		constraint.setScopeOptions( parentScope );
		return parentScope;
	}

	@Nullable
	@Override
	public Type getSibling()
	{
		return type;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		type.accept( scope, valueRef );
		constraint.check( scope, valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		Value value = type.optimize( scope, valueRef );
		constraint.check( scope, value );
		return value;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( type == null )
			type = reference.resolve( scope );

		if( !( type instanceof DefinedType ) )
			type.setNamespace( getNamespace() );
		type.validate( scope );
		constraint = constraintTemplate.build( scope, type );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return type.getFamily();
	}

	@NotNull
	@Override
	public Type copy()
	{
		Ref<Type> sub = Objects.equals( reference, type ) ? type.copy() : reference;
		return new ConstrainedType( constraintTemplate, sub );
	}

	@Override
	public String toString()
	{
		return reference + " " + constraint;
	}

	@Override
	protected void onDispose()
	{
		reference = null;
		type = null;
		constraintTemplate = null;
		constraint = null;
	}

	@Nullable
	@Override
	public NamedType getNamedType( @NotNull String name )
	{
		if( type == null )
			return null;
		return type.getNamedType( name );
	}

	@NotNull
	@Override
	public List<? extends NamedType> getNamedTypes()
	{
		return type.getNamedTypes();
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return type.isConstructedValue( scope, value );
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		return type.getNamedValue( name );
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return type.getNamedValues();
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return type.getEncoding( instructions );
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
