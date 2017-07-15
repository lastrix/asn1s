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

public class SequenceType extends AbstractCollectionType
{
	public SequenceType( boolean automaticTags )
	{
		super( Kind.Sequence, automaticTags );
		setEncoding( TagEncoding.universal( UniversalType.Sequence ) );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Value.Kind.NamedCollection )
			assertNamedCollection( scope, value.toValueCollection() );
		else
			throw new IllegalValueException( "Illegal Sequence value: " + value );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = scope.typedScope( this );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Value.Kind.NamedCollection )
			return optimizeNamedCollection( scope, value.toValueCollection() );

		throw new IllegalValueException( "Illegal Sequence value: " + value );
	}

	private Value optimizeNamedCollection( Scope scope, ValueCollection collection ) throws ValidationException, ResolutionException
	{
		if( collection.isEmpty() )
		{
			if( isAllComponentsOptional() )
				return collection;
			throw new IllegalValueException( "Type does not accepts empty collections" );
		}

		ValueCollection result = new ValueCollectionImpl( true );
		scope.setValueLevel( result );
		int version = 1;
		int previousComponentIndex = -1;
		for( NamedValue value : collection.asNamedValueList() )
		{
			ComponentType component = getComponent( value.getName(), true );

			if( component == null )
			{
				if( isExtensible() && previousComponentIndex >= getExtensionIndexStart() && previousComponentIndex <= getExtensionIndexEnd() )
					continue;

				throw new IllegalValueException( "Type does not have component with name: " + value.getName() );
			}

			if( component.getIndex() <= previousComponentIndex )
				throw new IllegalValueException( "ComponentType order is illegal for: " + value );

			version = Math.max( version, component.getVersion() );
			assertComponentsOptionalityInRange( previousComponentIndex, component.getIndex(), version );

			Value optimize = component.optimize( scope, value );
			if( !RefUtils.isSameAsDefaultValue( scope, component, optimize ) )
				result.add( optimize );
			previousComponentIndex = component.getIndex();
		}

		assertComponentsOptionalityInRange( previousComponentIndex, -1, version );
		return result;
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
		if( !isExtensible() )
			return "SEQUENCE { " + StringUtils.join( getComponents(), ", " ) + '}';
		return "SEQUENCE { " + StringUtils.join( getComponents(), ", " ) + ", ..., " + StringUtils.join( getExtensions(), ", " ) + ", ..., " + StringUtils.join( getComponentsLast(), ", " ) + '}';
	}

	private void assertNamedCollection( Scope scope, ValueCollection collection ) throws ValidationException, ResolutionException
	{
		if( collection.isEmpty() )
		{
			if( isAllComponentsOptional() )
				return;
			throw new IllegalValueException( "Type does not accepts empty collections" );
		}

		scope.setValueLevel( collection );
		int previousComponentIndex = -1;
		int version = 1;
		for( NamedValue value : collection.asNamedValueList() )
		{
			ComponentType component = getComponent( value.getName(), true );

			if( component == null )
			{
				if( isExtensible() && previousComponentIndex >= getExtensionIndexStart() && previousComponentIndex <= getExtensionIndexEnd() )
					continue;

				throw new IllegalValueException( "Type does not have component with name: " + value.getName() );
			}

			if( component.getIndex() <= previousComponentIndex )
				throw new IllegalValueException( "ComponentType order is illegal for: " + value );

			version = Math.max( version, component.getVersion() );
			assertComponentsOptionalityInRange( previousComponentIndex, component.getIndex(), version );

			component.accept( scope, value );
			previousComponentIndex = component.getIndex();
		}

		assertComponentsOptionalityInRange( previousComponentIndex, -1, version );
	}

	@Override
	public void assertComponentsOptionalityInRange( int start, int endBound, int version ) throws IllegalValueException
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
