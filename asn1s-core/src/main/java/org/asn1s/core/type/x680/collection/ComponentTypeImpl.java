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
import org.asn1s.api.type.*;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * X.680, p 25.1
 * Defines component type
 */
final class ComponentTypeImpl extends AbstractComponentType
{
	ComponentTypeImpl( int index, @NotNull String name, @NotNull Ref<Type> componentTypeRef )
	{
		super( index, name, componentTypeRef );
	}

	ComponentTypeImpl( ComponentType ofThis, int version )
	{
		super( ofThis.getIndex(), ofThis.getName(), ofThis.getComponentTypeRef() );
		setVersion( version );
		optional = ofThis.isOptional();
		defaultValueRef = ofThis.getDefaultValueRef();
	}

	private boolean optional;
	private Ref<Value> defaultValueRef;
	private Value defaultValue;

	@Override
	public boolean isOptional()
	{
		return optional;
	}

	public void setOptional( boolean optional )
	{
		if( optional && getDefaultValueRef() != null )
			throw new IllegalArgumentException( "Either default value or optional must be present" );

		this.optional = optional;
	}

	@Nullable
	@Override
	public Ref<Value> getDefaultValueRef()
	{
		return defaultValueRef;
	}

	void setDefaultValueRef( @Nullable Ref<Value> defaultValueRef )
	{
		if( defaultValueRef != null && isOptional() )
			throw new IllegalArgumentException( "Either default value or optional must be present" );
		this.defaultValueRef = defaultValueRef;
	}

	@Nullable
	@Override
	public Value getDefaultValue()
	{
		return defaultValue;
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
		if( componentValue == null )
			return value;
		return isDummy() ? componentValue : new NamedValueImpl( getComponentName(), componentValue );
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

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		super.onValidate( scope );
		if( defaultValueRef != null )
			defaultValue = getComponentType().optimize( scope, defaultValueRef );
	}

	@NotNull
	@Override
	public ComponentType copy()
	{
		Type type = getComponentTypeOrNull();
		if( type == null && getComponentTypeRef() instanceof Type && !( getComponentTypeRef() instanceof DefinedType ) )
			type = (Type)getComponentTypeRef();

		Ref<Type> subTypeRef = type == null ? getComponentTypeRef() : type.copy();
		ComponentTypeImpl componentType = new ComponentTypeImpl( getIndex(), getName(), subTypeRef );
		componentType.setVersion( getVersion() );
		componentType.setOptional( isOptional() );
		componentType.setDefaultValueRef( getDefaultValueRef() );
		return componentType;
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

	@Override
	protected void onDispose()
	{
		defaultValueRef = null;
		defaultValue = null;
	}
}
