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
import org.asn1s.api.State;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.*;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.type.AbstractType;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * X.680, p 25.1
 * Defines component type
 */
final class ComponentTypeImpl extends AbstractType implements ComponentType
{
	ComponentTypeImpl( int index, int version, @NotNull String name, @NotNull Ref<Type> componentTypeRef, boolean optional, @Nullable Ref<Value> defaultValueRef )
	{
		RefUtils.assertValueRef( name );
		if( optional && defaultValueRef != null )
			throw new IllegalArgumentException( "Either optional or default value must be used, not both" );

		this.index = index;
		this.version = version;
		this.name = name;
		this.componentTypeRef = componentTypeRef;
		if( componentTypeRef instanceof Type )
			componentType = (Type)componentTypeRef;

		this.optional = optional;
		this.defaultValueRef = defaultValueRef;
		if( defaultValueRef instanceof Value )
			defaultValue = (Value)defaultValueRef;
	}

	private final int index;
	private final int version;
	private final String name;
	private Ref<Type> componentTypeRef;
	private Type componentType;
	private final boolean optional;
	private Ref<Value> defaultValueRef;
	private Value defaultValue;

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.typedScope( this );
	}

	@Override
	public int getIndex()
	{
		return index;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@NotNull
	@Override
	public Ref<Type> getComponentTypeRef()
	{
		return componentTypeRef;
	}

	@NotNull
	@Override
	public Type getComponentType()
	{
		if( getState() != State.Done )
			throw new IllegalStateException();
		return componentType;
	}

	@Override
	public boolean isOptional()
	{
		return optional;
	}

	@Nullable
	@Override
	public Ref<Value> getDefaultValueRef()
	{
		return defaultValueRef;
	}

	@Nullable
	@Override
	public Value getDefaultValue()
	{
		if( getState() != State.Done )
			throw new IllegalStateException();
		return defaultValue;
	}

	@Nullable
	@Override
	public Type getSibling()
	{
		return componentType;
	}

	@Override
	public boolean isExplicitlyTagged()
	{
		return componentType.isTagged() && ( (TaggedType)componentType ).getInstructions() == EncodingInstructions.Tag;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = getScope( scope );
		if( isDummy() )
		{
			Value value = valueRef.resolve( scope );
			componentType.accept( scope, value );
		}
		else
		{
			Value value = valueRef.resolve( scope );
			if( value.getKind() == Value.Kind.Name )
			{
				NamedValue namedValue = value.toNamedValue();
				if( !name.equals( namedValue.getName() ) )
					throw new IllegalValueException( "Unexpected value name: " + namedValue.getName() );
				if( namedValue.getValueRef() == null )
					throw new IllegalStateException();
				componentType.accept( scope, namedValue.getValueRef() );
			}
			else
				componentType.accept( scope, value );
		}
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = valueRef.resolve( scope );
		if( value.getKind() == Value.Kind.Name )
		{
			NamedValue namedValue = value.toNamedValue();
			if( !namedValue.getName().equals( getComponentName() ) )
				throw new IllegalValueException( "Illegal component: " + valueRef );

			Ref<Value> referenced = namedValue.getValueRef();
			assert referenced != null;

			Value referencedOptimized = componentType.optimize( scope, referenced );
			//noinspection ObjectEquality
			if( referencedOptimized == referenced )
				return value;

			return isDummy()
					? referencedOptimized
					: new NamedValueImpl( getComponentName(), referencedOptimized );
		}
		else
			return isDummy()
					? componentType.optimize( scope, value )
					: new NamedValueImpl( getComponentName(), componentType.optimize( scope, value ) );
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
	public String toString()
	{
		if( isOptional() )
			return name + ' ' + componentTypeRef + " OPTIONAL";

		if( defaultValueRef != null )
			return name + ' ' + componentTypeRef + " DEFAULT " + defaultValueRef;

		return name + ' ' + componentTypeRef;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		if( componentType == null )
			componentType = componentTypeRef.resolve( scope );

		if( !( componentType instanceof DefinedType ) )
			componentType.setNamespace( getFullyQualifiedName() + '.' );
		componentType.validate( scope );

		if( defaultValueRef != null )
			defaultValue = defaultValueRef.resolve( scope );
	}

	@NotNull
	@Override
	public Type copy()
	{
		Ref<Type> subTypeRef = Objects.equals( componentType, componentTypeRef ) ? componentType.copy() : componentTypeRef;
		return new ComponentTypeImpl( index, version, name, subTypeRef, optional, defaultValueRef );
	}

	@Nullable
	@Override
	public NamedType getNamedType( @NotNull String name )
	{
		if( componentType == null )
			return null;
		return componentType.getNamedType( name );
	}

	@NotNull
	@Override
	public List<? extends NamedType> getNamedTypes()
	{
		return componentType.getNamedTypes();
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		return componentType.getNamedValue( name );
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return componentType.getNamedValues();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return componentType.getFamily();
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return componentType.getEncoding( instructions );
	}

	@Override
	protected void onDispose()
	{
		componentTypeRef = null;
		componentType = null;
		defaultValueRef = null;
		defaultValue = null;
	}
}
