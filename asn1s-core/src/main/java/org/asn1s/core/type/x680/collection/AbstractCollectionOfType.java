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
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.value.x680.ValueCollectionImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

abstract class AbstractCollectionOfType extends AbstractCollectionType implements CollectionOfType
{
	AbstractCollectionOfType( @NotNull Kind kind )
	{
		super( kind, false );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Value.Kind.Collection && value.getKind() != Value.Kind.NamedCollection )
			throw new IllegalValueException( "Unable to accept value of kind: " + value.getKind() );

		ComponentType componentType = getComponentType();
		if( componentType == null )
			throw new IllegalStateException();

		boolean isDummy = componentType.isDummy();

		if( value.getKind() == Value.Kind.Collection && isDummy )
		{
			scope.setValueLevel( value );
			for( Ref<Value> ref : value.toValueCollection().asValueList() )
				componentType.accept( scope, ref );
		}
		else if( value.getKind() == Value.Kind.NamedCollection && !isDummy )
		{
			scope.setValueLevel( value );
			for( NamedValue ref : value.toValueCollection().asNamedValueList() )
				componentType.accept( scope, ref );
		}
		else if( value.getKind() != Value.Kind.NamedCollection && !value.toValueCollection().isEmpty() )
			throw new IllegalValueException( "Unable to accept value: " + valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Value.Kind.Collection )
			return optimizeCollection( scope, value.toValueCollection() );

		if( value.getKind() == Value.Kind.NamedCollection )
			return optimizeNamedCollection( scope, value.toValueCollection() );

		ComponentType componentType = getComponentType();
		assert componentType != null;
		return componentType.optimize( scope, valueRef );
	}

	private Value optimizeNamedCollection( Scope scope, ValueCollection collection ) throws ResolutionException, ValidationException
	{
		ComponentType componentType = getComponentType();
		assert componentType != null;

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
		assert componentType != null;

		ValueCollection result = new ValueCollectionImpl( false );
		scope.setValueLevel( result );
		for( Ref<Value> ref : collection.asValueList() )
			result.add( componentType.optimize( scope, ref ) );

		return result;
	}

	@Nullable
	@Override
	public ComponentType getComponentType()
	{
		List<ComponentType> actualComponents = getComponents( true );
		return actualComponents.isEmpty() ? null : actualComponents.get( 0 );
	}
}
