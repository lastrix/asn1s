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

public class SequenceType extends AbstractCollectionType
{
	public SequenceType( boolean automaticTags )
	{
		super( automaticTags );
		setEncoding( TagEncoding.universal( UniversalType.Sequence ) );
	}

	private int extensionIndexStart = Integer.MAX_VALUE;
	private int extensionIndexEnd = Integer.MIN_VALUE;

	@Override
	void updateIndices()
	{
		super.updateIndices();
		if( getComponents() == null )
			throw new IllegalStateException();

		for( ComponentType type : getComponents( true ) )
		{
			if( type.getVersion() > 1 )
			{
				extensionIndexStart = Math.min( extensionIndexStart, type.getIndex() );
				extensionIndexEnd = Math.max( extensionIndexEnd, type.getIndex() );
			}
		}
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Kind.NamedCollection && value.getKind() != Kind.Collection )
			throw new IllegalValueException( "Illegal Sequence value: " + value );

		new SequenceValidator( scope, value.toValueCollection() )
				.process();
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Kind.NamedCollection && value.getKind() != Kind.Collection )
			throw new IllegalValueException( "Illegal Sequence value: " + value );

		return new SequenceOptimizer( scope, value.toValueCollection() )
				.process();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.Sequence;
	}

	@Override
	protected AbstractCollectionType onCopy()
	{
		return new SequenceType( isAutomaticTags() );
	}

	@Override
	public String toString()
	{
		return "SEQUENCE" + CoreCollectionUtils.buildComponentString( this );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		setActualComponents( new SequenceComponentsInterpolator( scope.typedScope( this ), this ).interpolate() );
		updateIndices();
	}

	private abstract class SequenceOperator
	{
		private final Scope scope;
		private final ValueCollection collection;
		private int previousComponentIndex = -1;
		private int version = 1;

		SequenceOperator( Scope scope, ValueCollection collection )
		{
			this.scope = scope;
			this.collection = collection;
		}

		protected Scope getScope()
		{
			return scope;
		}

		protected ValueCollection getCollection()
		{
			return collection;
		}

		Value process() throws ValidationException, ResolutionException
		{
			if( collection.isEmpty() )
			{
				if( isAllComponentsOptional() )
					return collection;
				throw new IllegalValueException( "Type does not accepts empty collections" );
			}

			for( NamedValue value : collection.asNamedValueList() )
				processNamedValue( value );

			assertComponentsOptionalityInRange( previousComponentIndex, -1, version );

			return getResult();
		}

		private void processNamedValue( NamedValue value ) throws ValidationException, ResolutionException
		{
			ComponentType component = getComponent( value.getName(), true );
			if( component == null )
				processExtensibleComponent( value );
			else
				processComponentValue( value, component );
		}

		private void processComponentValue( NamedValue value, ComponentType component ) throws ValidationException, ResolutionException
		{
			if( component.getIndex() <= previousComponentIndex )
				throw new IllegalValueException( "ComponentType order is illegal for: " + value );

			version = Math.max( version, component.getVersion() );
			assertComponentsOptionalityInRange( previousComponentIndex, component.getIndex(), version );

			onProcessComponent( value, component );
			previousComponentIndex = component.getIndex();
		}

		private void processExtensibleComponent( NamedValue value ) throws IllegalValueException
		{
			if( isExtensible() && previousComponentIndex >= extensionIndexStart && previousComponentIndex <= extensionIndexEnd )
				return;

			throw new IllegalValueException( "Type does not have component with name: " + value.getName() );
		}

		protected abstract void onProcessComponent( NamedValue value, ComponentType component ) throws ValidationException, ResolutionException;

		protected abstract Value getResult();

		private void assertComponentsOptionalityInRange( int start, int endBound, int version ) throws IllegalValueException
		{
			if( start == -1 && endBound == -1 && !isAllComponentsOptional() )
				throw new IllegalValueException( "No components" );

			if( endBound - start == 1 )
				return;

			for( ComponentType type : getComponents( true ) )
			{
				if( type.getIndex() <= start || endBound != -1 && type.getIndex() >= endBound )
					continue;

				if( type.isRequired() && type.getVersion() <= version )
					throw new IllegalValueException( "Missing required component: " + type.getComponentName() );
			}
		}

	}

	private final class SequenceValidator extends SequenceOperator
	{
		private SequenceValidator( Scope scope, ValueCollection collection )
		{
			super( scope, collection );
			scope.setValueLevel( collection );
		}

		@Override
		protected void onProcessComponent( NamedValue value, ComponentType component ) throws ValidationException, ResolutionException
		{
			component.accept( getScope(), value );
		}

		@Override
		protected Value getResult()
		{
			return getCollection();
		}
	}

	private final class SequenceOptimizer extends SequenceOperator
	{

		private final ValueCollection result;

		private SequenceOptimizer( Scope scope, ValueCollection collection )
		{
			super( scope, collection );
			result = new ValueCollectionImpl( true );
			scope.setValueLevel( result );
		}

		@Override
		protected void onProcessComponent( NamedValue value, ComponentType component ) throws ValidationException, ResolutionException
		{
			Value optimize = component.optimize( getScope(), value );
			if( !RefUtils.isSameAsDefaultValue( getScope(), component, optimize ) )
				result.add( optimize );
		}

		@Override
		protected Value getResult()
		{
			return result;
		}
	}
}
