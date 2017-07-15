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

package org.asn1s.core.type.x681;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TypeFieldType extends AbstractFieldType
{
	public TypeFieldType( @NotNull String name, boolean optional, @Nullable Ref<Type> defaultTypeRef )
	{
		super( name, optional );
		RefUtils.assertTypeRef( name.substring( 1 ) );
		if( optional && defaultTypeRef != null )
			throw new IllegalArgumentException( "'optional' is true and 'defaultTypeRef' is not null at same time." );

		this.defaultTypeRef = defaultTypeRef;
		if( defaultTypeRef instanceof Type )
			defaultType = (Type)defaultTypeRef;
	}

	private Ref<Type> defaultTypeRef;
	private Type defaultType;

	@Override
	public boolean hasDefault()
	{
		return defaultTypeRef != null;
	}

	@Override
	public Ref<?> getDefault()
	{
		return defaultType;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );

		if( defaultTypeRef != null )
		{
			if( defaultType == null )
				defaultType = defaultTypeRef.resolve( scope );

			defaultType.validate( scope );
		}
	}

	@NotNull
	@Override
	public Type copy()
	{
		return new TypeFieldType( getName(), isOptional(), Objects.equals( defaultTypeRef, defaultType ) ? defaultType.copy() : defaultTypeRef );
	}

	@Override
	protected void onDispose()
	{
		defaultTypeRef = null;
		defaultType = null;
	}

	@Override
	public void acceptRef( @NotNull Scope scope, Ref<?> ref ) throws ResolutionException, IllegalValueException, ConstraintViolationException
	{

	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, IllegalValueException, ConstraintViolationException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Value.Kind.OpenType )
			return value;

		throw new IllegalValueException( "Unable to accept value of kind: " + value.getKind() + ". Only OpenType values allowed." );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Ref<?> optimizeRef( @NotNull Scope scope, Ref<?> ref ) throws ResolutionException, ValidationException
	{
		Object resolve = ref.resolve( scope );
		if( resolve instanceof Value )
			return optimize( scope, (Ref<Value>)resolve );

		return (Ref<?>)resolve;
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.OpenType;
	}

	@Override
	public Kind getClassFieldKind()
	{
		return Kind.Type;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
