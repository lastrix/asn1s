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
		if( value.getKind() == Value.Kind.NamedCollection )
			assertNamedCollection( scope, value.toValueCollection() );
		else
			throw new IllegalValueException( "Illegal Set value: " + value );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Value.Kind.NamedCollection )
			return optimizeNamedCollection( scope, value.toValueCollection() );

		throw new IllegalValueException( "Illegal Set value: " + value );
	}

	private Value optimizeNamedCollection( Scope scope, ValueCollection collection ) throws ValidationException, ResolutionException
	{
		if( collection.isEmpty() )
		{
			if( isAllComponentsOptional() )
				return collection;
			throw new IllegalValueException( "Components required" );
		}

		Collection<ComponentType> unusedComponents = new HashSet<>( getComponents( true ) );

		ValueCollection result = new ValueCollectionImpl( true );
		scope.setValueLevel( result );
		int version = 1;
		Collection<String> extensibleRequired = new HashSet<>();
		for( NamedValue value : collection.asNamedValueList() )
		{
			ComponentType component = getComponent( value.getName(), true );
			if( component == null )
			{
				if( !isExtensible() )
					throw new IllegalValueException( "Type has no components with name: " + value.getName() );

				extensibleRequired.add( value.getName() );
				continue;
			}

			version = Math.max( version, component.getVersion() );
			Value optimize = component.optimize( scope, value );
			if( !RefUtils.isSameAsDefaultValue( scope, component, optimize ) )
				result.add( optimize );

			if( !unusedComponents.remove( component ) )
				throw new IllegalValueException( "Component occurs more than once: " + component.getName() );
		}

		for( ComponentType component : unusedComponents )
			if( component.isRequired() && component.getVersion() <= version )
				throw new IllegalValueException( "Required component is not used: " + component.getName() );

		if( !extensibleRequired.isEmpty() && version < getMaxVersion() )
			throw new IllegalValueException( "Unable to accept components: " + extensibleRequired );

		return result;
	}

	private void assertNamedCollection( Scope scope, ValueCollection collection ) throws ValidationException, ResolutionException
	{
		if( collection.isEmpty() )
		{
			if( isAllComponentsOptional() )
				return;
			throw new IllegalValueException( "Components required" );
		}
		scope.setValueLevel( collection );
		Collection<ComponentType> unusedComponents = new HashSet<>( getComponents( true ) );

		Collection<String> extensibleRequired = new HashSet<>();

		int version = 1;
		for( NamedValue value : collection.asNamedValueList() )
		{
			ComponentType component = getComponent( value.getName(), true );
			if( component == null )
			{
				if( !isExtensible() )
					throw new IllegalValueException( "Type has no components with name: " + value.getName() );

				extensibleRequired.add( value.getName() );
				continue;
			}

			version = Math.max( version, component.getVersion() );
			component.accept( scope, value );
			if( !unusedComponents.remove( component ) )
				throw new IllegalValueException( "Component occurs more than once: " + component.getName() );
		}

		for( ComponentType component : unusedComponents )
			if( component.isRequired() && component.getVersion() <= version )
				throw new IllegalValueException( "Required component is not used: " + component.getName() );

		if( !extensibleRequired.isEmpty() && version < getMaxVersion() )
			throw new IllegalValueException( "Unable to accept components: " + extensibleRequired );
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
}
