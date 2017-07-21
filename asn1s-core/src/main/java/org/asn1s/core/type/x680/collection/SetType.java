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

import org.apache.commons.lang3.StringUtils;
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
		super( Kind.Set, automatic );
		setEncoding( TagEncoding.universal( UniversalType.Set ) );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Value.Kind.NamedCollection )
			throw new IllegalValueException( "Illegal Set value: " + value );

		new SetValidator( scope, this, value.toValueCollection() )
				.validate();
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Value.Kind.NamedCollection )
			throw new IllegalValueException( "Illegal Set value: " + value );

		return new SetOptimizer( scope, this, value.toValueCollection() ).optimize();
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
		if( !isExtensible() )
			return "SET { " + StringUtils.join( getComponents(), ", " ) + '}';
		return "SET { " + StringUtils.join( getComponents(), ", " ) + ", ..., " + StringUtils.join( getExtensions(), ", " ) + ", ..., " + StringUtils.join( getComponentsLast(), ", " ) + '}';
	}

	private static final class SetOptimizer
	{
		private final Scope scope;
		private final SetType type;
		private final ValueCollection collection;
		private int version = 1;
		private final ValueCollection result = new ValueCollectionImpl( true );
		private final Collection<ComponentType> unusedComponents;
		private final Collection<String> extensibleRequired;

		private SetOptimizer( Scope scope, SetType type, ValueCollection collection )
		{
			this.scope = scope;
			this.type = type;
			this.collection = collection;
			unusedComponents = new HashSet<>( type.getComponents( true ) );
			extensibleRequired = new HashSet<>();
			scope.setValueLevel( result );
		}

		public Value optimize() throws ResolutionException, ValidationException
		{
			if( collection.isEmpty() )
			{
				if( type.isAllComponentsOptional() )
					return collection;
				throw new IllegalValueException( "Components required" );
			}

			for( NamedValue value : collection.asNamedValueList() )
				optimizeComponentValue( value );

			assertLeftoverComponents();
			return result;
		}

		private void assertLeftoverComponents() throws IllegalValueException
		{
			for( ComponentType component : unusedComponents )
				if( component.isRequired() && component.getVersion() <= version )
					throw new IllegalValueException( "Required component is not used: " + component.getName() );

			if( !extensibleRequired.isEmpty() && version < type.getMaxVersion() )
				throw new IllegalValueException( "Unable to accept components: " + extensibleRequired );
		}

		private void optimizeComponentValue( NamedValue value ) throws ResolutionException, ValidationException
		{
			ComponentType component = type.getComponent( value.getName(), true );
			if( component == null )
				assertExtensibleValue( value );
			else
				optimizeComponentValueImpl( value, component );
		}

		private void optimizeComponentValueImpl( Ref<Value> value, ComponentType component ) throws ResolutionException, ValidationException
		{
			version = Math.max( version, component.getVersion() );
			Value optimize = component.optimize( scope, value );
			if( !RefUtils.isSameAsDefaultValue( scope, component, optimize ) )
				result.add( optimize );

			if( !unusedComponents.remove( component ) )
				throw new IllegalValueException( "Component occurs more than once: " + component.getName() );
		}

		private void assertExtensibleValue( NamedValue value ) throws IllegalValueException
		{
			if( !type.isExtensible() )
				throw new IllegalValueException( "Type has no components with name: " + value.getName() );

			extensibleRequired.add( value.getName() );
		}
	}

	private static final class SetValidator
	{
		private final Scope scope;
		private final SetType type;
		private final ValueCollection collection;
		private final Collection<ComponentType> unusedComponents;
		private final Collection<String> extensibleRequired;
		private int version = 1;

		private SetValidator( Scope scope, SetType type, ValueCollection collection )
		{
			this.scope = scope;
			this.type = type;
			this.collection = collection;
			unusedComponents = new HashSet<>( type.getComponents( true ) );
			extensibleRequired = new HashSet<>();
			scope.setValueLevel( collection );
		}

		public void validate() throws ValidationException, ResolutionException
		{
			if( collection.isEmpty() )
			{
				if( !type.isAllComponentsOptional() )
					throw new IllegalValueException( "Components required" );
			}
			else
			{
				for( NamedValue value : collection.asNamedValueList() )
					validateComponentValue( value );

				assertLeftoverComponentValues();
			}
		}

		private void assertLeftoverComponentValues() throws IllegalValueException
		{
			for( ComponentType component : unusedComponents )
				if( component.isRequired() && component.getVersion() <= version )
					throw new IllegalValueException( "Required component is not used: " + component.getName() );

			if( !extensibleRequired.isEmpty() && version < type.getMaxVersion() )
				throw new IllegalValueException( "Unable to accept components: " + extensibleRequired );
		}

		private void validateComponentValue( NamedValue value ) throws ValidationException, ResolutionException
		{
			ComponentType component = type.getComponent( value.getName(), true );
			if( component == null )
				validateExtensibleValue( value );
			else
				acceptComponentValue( value, component );
		}

		private void acceptComponentValue( Ref<Value> value, ComponentType component ) throws ValidationException, ResolutionException
		{
			version = Math.max( version, component.getVersion() );
			component.accept( scope, value );
			if( !unusedComponents.remove( component ) )
				throw new IllegalValueException( "Component occurs more than once: " + component.getName() );
		}

		private void validateExtensibleValue( NamedValue value ) throws IllegalValueException
		{
			if( !type.isExtensible() )
				throw new IllegalValueException( "Type has no components with name: " + value.getName() );

			extensibleRequired.add( value.getName() );
		}
	}
}
