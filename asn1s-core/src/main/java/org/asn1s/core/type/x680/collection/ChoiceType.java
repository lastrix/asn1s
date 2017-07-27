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
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;

public final class ChoiceType extends AbstractCollectionType
{
	public ChoiceType( boolean automaticTags )
	{
		super( automaticTags );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		new ChoiceValidator( scope, valueRef ).process();
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		return new ChoiceOptimizer( scope, valueRef ).process();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.CHOICE;
	}

	@Override
	protected AbstractCollectionType onCopy()
	{
		return new ChoiceType( isAutomaticTags() );
	}

	@Override
	public String toString()
	{
		return "CHOICE" + CoreCollectionUtils.buildComponentString( this );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		setActualComponents( new ChoiceComponentsInterpolator( getScope( scope ), this ).interpolate() );
		updateIndices();
	}

	private abstract class AbstractOperator
	{
		private final Scope scope;
		private final Value value;

		AbstractOperator( Scope scope, Ref<Value> valueRef ) throws ResolutionException
		{
			this.scope = scope.typedScope( ChoiceType.this );
			value = valueRef.resolve( scope );
			if( value.getKind() != Kind.NAME )
				throw new IllegalArgumentException( "Unable to accept value of kind: " + value.getKind() );
			this.scope.setValueLevel( value );
		}

		public Scope getScope()
		{
			return scope;
		}

		public Value getValue()
		{
			return value;
		}

		Value process() throws ResolutionException, ValidationException
		{
			NamedValue namedValue = value.toNamedValue();

			ComponentType componentType = getNamedType( namedValue.getName() );
			if( componentType == null )
				return onExtensibleComponent( namedValue );

			return onComponent( componentType, namedValue );
		}

		private Value onExtensibleComponent( NamedValue namedValue ) throws IllegalValueException
		{
			if( !isExtensible() )
				throw new IllegalValueException( "Unable to find component for name: " + namedValue.getName() );

			return value;
		}

		protected abstract Value onComponent( ComponentType componentType, NamedValue namedValue ) throws ResolutionException, ValidationException;
	}

	private final class ChoiceValidator extends AbstractOperator
	{
		private ChoiceValidator( Scope scope, Ref<Value> valueRef ) throws ResolutionException
		{
			super( scope, valueRef );
		}

		@Override
		protected Value onComponent( ComponentType componentType, NamedValue namedValue ) throws ResolutionException, ValidationException
		{
			componentType.accept( getScope(), namedValue );
			return getValue();
		}
	}

	private final class ChoiceOptimizer extends AbstractOperator
	{
		private ChoiceOptimizer( Scope scope, Ref<Value> valueRef ) throws ResolutionException
		{
			super( scope, valueRef );
		}

		@Override
		protected Value onComponent( ComponentType componentType, NamedValue namedValue ) throws ResolutionException, ValidationException
		{
			return componentType.optimize( getScope(), namedValue );
		}
	}
}
