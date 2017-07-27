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
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.CollectionOfType;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.value.x680.ValueCollectionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

abstract class AbstractCollectionOfType extends BuiltinType implements CollectionOfType
{
	AbstractCollectionOfType( @Nullable ComponentType sourceComponentType )
	{
		this.sourceComponentType = sourceComponentType;
	}

	private ComponentType sourceComponentType;
	private ComponentType actualComponentType;

	@Override
	public void setComponent( @NotNull String componentName, @NotNull Ref<Type> componentTypeRef )
	{
		if( sourceComponentType != null )
			throw new IllegalStateException( "Source component type already set" );

		sourceComponentType = new ComponentTypeImpl( 0, componentName, componentTypeRef );
	}

	ComponentType getSourceComponentType()
	{
		return sourceComponentType;
	}

	@SuppressWarnings( "unchecked" )
	@Nullable
	@Override
	public <T extends NamedType> T getNamedType( @NotNull String name )
	{
		return name.equals( actualComponentType.getComponentName() ) ? (T)actualComponentType : null;
	}

	@SuppressWarnings( "unchecked" )
	@NotNull
	@Override
	public <T extends NamedType> List<T> getNamedTypes()
	{
		return Collections.singletonList( (T)actualComponentType );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Kind.COLLECTION && value.getKind() != Kind.NAMED_COLLECTION )
			throw new IllegalValueException( "Unable to accept value of kind: " + value.getKind() );

		ComponentType componentType = getComponentType();
		boolean isDummy = componentType.isDummy();

		if( value.getKind() == Kind.COLLECTION && isDummy )
		{
			scope.setValueLevel( value );
			for( Ref<Value> ref : value.toValueCollection().asValueList() )
				componentType.accept( scope, ref );
		}
		else if( value.getKind() == Kind.NAMED_COLLECTION && !isDummy )
		{
			scope.setValueLevel( value );
			for( NamedValue ref : value.toValueCollection().asNamedValueList() )
				componentType.accept( scope, ref );
		}
		else if( value.getKind() != Kind.NAMED_COLLECTION && !value.toValueCollection().isEmpty() )
			throw new IllegalValueException( "Unable to accept value: " + valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Kind.COLLECTION )
			return optimizeCollection( scope, value.toValueCollection() );

		if( value.getKind() == Kind.NAMED_COLLECTION )
			return optimizeNamedCollection( scope, value.toValueCollection() );

		ComponentType componentType = getComponentType();
		return componentType.optimize( scope, valueRef );
	}

	private Value optimizeNamedCollection( Scope scope, ValueCollection collection ) throws ResolutionException, ValidationException
	{
		ComponentType componentType = getComponentType();
		ValueCollection result = new ValueCollectionImpl( false );
		scope.setValueLevel( result );
		for( NamedValue value : collection.asNamedValueList() )
		{
			if( !value.getName().equals( componentType.getComponentName() ) )
				throw new IllegalValueException( "Not valid component: " + value );

			result.add( componentType.optimize( scope, value ) );
		}

		return result;
	}

	private Value optimizeCollection( Scope scope, ValueCollection collection ) throws ResolutionException, ValidationException
	{
		ComponentType componentType = getComponentType();
		ValueCollection result = new ValueCollectionImpl( false );
		scope.setValueLevel( result );
		for( Ref<Value> ref : collection.asValueList() )
			result.add( componentType.optimize( scope, ref ) );

		return result;
	}

	@NotNull
	@Override
	public ComponentType getComponentType()
	{
		return actualComponentType;
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return value.getKind() == Kind.COLLECTION;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		actualComponentType = interpolateSingleComponent( scope );
	}

	@NotNull
	private ComponentType interpolateSingleComponent( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		ComponentType componentType = new ComponentTypeImpl( sourceComponentType, 1 );
		componentType.validate( scope );
		return componentType;
	}

	@Override
	protected void onDispose()
	{
		if( sourceComponentType != null )
		{
			sourceComponentType.dispose();
			sourceComponentType = null;
		}

		if( actualComponentType != null )
		{
			actualComponentType.dispose();
			actualComponentType = null;
		}
	}
}
