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
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.value.x680.ValueCollectionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

/**
 * X.680, p 27.1
 */
public final class SetType extends AbstractCollectionType
{
	public SetType( boolean automatic )
	{
		super( automatic );
		setEncoding( TagEncoding.universal( UniversalType.Set ) );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Kind.NamedCollection && value.getKind() != Kind.Collection )
			throw new IllegalValueException( "Illegal Set value: " + value );

		new SetValidator( scope, value.toValueCollection() )
				.process();
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Kind.NamedCollection && value.getKind() != Kind.Collection )
			throw new IllegalValueException( "Illegal Set value: " + value );

		return new SetOptimizer( scope, value.toValueCollection() )
				.process();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.Set;
	}

	@Override
	protected AbstractCollectionType onCopy()
	{
		return new SetType( isAutomaticTags() );
	}

	@Override
	public String toString()
	{
		return "SET" + CoreCollectionUtils.buildComponentString( this );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		setActualComponents( new SetComponentsInterpolator( getScope( scope ), this ).interpolate() );
		updateIndices();
	}

	private abstract class AbstractSetOperator
	{
		private final Scope scope;
		private final ValueCollection collection;
		private int version = 1;
		private final Collection<ComponentType> unusedComponents;
		private final Collection<String> extensibleRequired;

		AbstractSetOperator( Scope scope, ValueCollection collection )
		{
			this.scope = scope;
			this.collection = collection;
			unusedComponents = new HashSet<>( getComponents( true ) );
			extensibleRequired = new HashSet<>();
		}

		public Scope getScope()
		{
			return scope;
		}

		Value process() throws ResolutionException, ValidationException
		{
			if( collection.isEmpty() )
			{
				if( isAllComponentsOptional() )
					return collection;
				throw new IllegalValueException( "Components required" );
			}

			for( NamedValue value : collection.asNamedValueList() )
				processComponentValue( value );

			assertLeftoverComponents();
			return getResult();
		}

		private void assertLeftoverComponents() throws IllegalValueException
		{
			for( ComponentType component : unusedComponents )
				if( component.isRequired() && component.getVersion() <= version )
					throw new IllegalValueException( "Required component is not used: " + component.getName() );

			if( !extensibleRequired.isEmpty() && version < getMaxVersion() )
				throw new IllegalValueException( "Unable to accept components: " + extensibleRequired );
		}

		@NotNull
		protected Value getResult()
		{
			return collection;
		}

		private void processComponentValue( NamedValue value ) throws ResolutionException, ValidationException
		{
			ComponentType component = getComponent( value.getName(), true );
			if( component == null )
				assertExtensibleValue( value );
			else
				processComponentValueImpl( value, component );
		}

		private void processComponentValueImpl( Ref<Value> value, ComponentType component ) throws ResolutionException, ValidationException
		{
			version = Math.max( version, component.getVersion() );
			onComponentValueProcessing( value, component );
			if( !unusedComponents.remove( component ) )
				throw new IllegalValueException( "Component occurs more than once: " + component.getName() );
		}

		private void assertExtensibleValue( NamedValue value ) throws IllegalValueException
		{
			if( !isExtensible() )
				throw new IllegalValueException( "Type has no components with name: " + value.getName() );

			extensibleRequired.add( value.getName() );
		}

		protected abstract void onComponentValueProcessing( Ref<Value> value, ComponentType component ) throws ResolutionException, ValidationException;
	}

	private final class SetOptimizer extends AbstractSetOperator
	{
		private final ValueCollection result = new ValueCollectionImpl( true );

		private SetOptimizer( Scope scope, ValueCollection collection )
		{
			super( scope, collection );
			scope.setValueLevel( result );
		}

		@Override
		protected void onComponentValueProcessing( Ref<Value> value, ComponentType component ) throws ResolutionException, ValidationException
		{
			Value optimize = component.optimize( getScope(), value );
			if( !RefUtils.isSameAsDefaultValue( getScope(), component, optimize ) )
				result.add( optimize );
		}

		@NotNull
		@Override
		protected Value getResult()
		{
			return result;
		}
	}

	private final class SetValidator extends AbstractSetOperator
	{
		private SetValidator( Scope scope, ValueCollection collection )
		{
			super( scope, collection );
			scope.setValueLevel( collection );
		}

		@Override
		protected void onComponentValueProcessing( Ref<Value> value, ComponentType component ) throws ResolutionException, ValidationException
		{
			component.accept( getScope(), value );
		}
	}
}
