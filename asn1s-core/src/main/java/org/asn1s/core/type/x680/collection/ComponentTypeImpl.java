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

package org.asn1s.core.type.x680.collection;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractComponentType;
import org.asn1s.api.type.TaggedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * X.680, p 25.1
 * Defines component type
 */
final class ComponentTypeImpl extends AbstractComponentType
{
	ComponentTypeImpl( int index, int version, @NotNull String name, @NotNull Ref<Type> componentTypeRef, boolean optional, @Nullable Ref<Value> defaultValueRef )
	{
		super( index, version, name, optional );
		RefUtils.assertValueRef( name );
		if( optional && defaultValueRef != null )
			throw new IllegalArgumentException( "Either optional or default value must be used, not both" );

		setComponentTypeRef( componentTypeRef );
		setDefaultValueRef( defaultValueRef );
	}

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.typedScope( this );
	}

	@Override
	public boolean isExplicitlyTagged()
	{
		return getComponentType().isTagged() && ( (TaggedType)getComponentType() ).getInstructions() == EncodingInstructions.Tag;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = getScope( scope );
		Value value = valueRef.resolve( scope );
		Value componentValue = value;
		if( !isDummy() && value.getKind() == Value.Kind.Name )
		{
			NamedValue namedValue = value.toNamedValue();
			if( !getComponentName().equals( namedValue.getName() ) || !( namedValue.getValueRef() instanceof Value ) )
				throw new IllegalValueException( "Illegal component: " + value );
			componentValue = (Value)namedValue.getValueRef();
		}
		getComponentType().accept( scope, componentValue );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		Value value = valueRef.resolve( scope );
		Value componentValue = optimizeComponentValue( scope, value );
		return componentValue == null || isDummy() ? value : new NamedValueImpl( getComponentName(), componentValue );
	}

	@Nullable
	private Value optimizeComponentValue( Scope scope, Value value ) throws ResolutionException, ValidationException
	{
		Ref<Value> referenced = isDummy() || value.getKind() != Value.Kind.Name
				? value
				: getReferencedValueOrDie( value );

		Value referencedOptimized = getComponentType().optimize( scope, referenced );
		//noinspection ObjectEquality
		return referencedOptimized == referenced ? null : referencedOptimized;
	}

	@NotNull
	private Ref<Value> getReferencedValueOrDie( Value value ) throws IllegalValueException
	{
		NamedValue namedValue = value.toNamedValue();
		if( !namedValue.getName().equals( getComponentName() ) || namedValue.getValueRef() == null )
			throw new IllegalValueException( "Illegal component: " + value );
		return namedValue.getValueRef();
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj == this || obj instanceof ComponentTypeImpl && toString().equals( obj.toString() );
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@NotNull
	@Override
	public Type copy()
	{
		Ref<Type> subTypeRef = Objects.equals( getComponentType(), getComponentTypeRef() ) ? getComponentType().copy() : getComponentTypeRef();
		return new ComponentTypeImpl( getIndex(), getVersion(), getName(), subTypeRef, isOptional(), getDefaultValueRef() );
	}

	@Override
	public String toString()
	{
		if( isOptional() )
			return getComponentName() + ' ' + getComponentTypeRef() + " OPTIONAL";

		if( getDefaultValueRef() != null )
			return getComponentName() + ' ' + getComponentTypeRef() + " DEFAULT " + getDefaultValueRef();

		return getComponentName() + ' ' + getComponentTypeRef();
	}
}
