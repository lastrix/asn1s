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

package org.asn1s.core.type.x680;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Enumerated;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.value.x680.IntegerValueLong;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * X.680, p 20
 *
 * @author lastrix
 * @version 1.0
 */
@SuppressWarnings( "WeakerAccess" )
public final class EnumeratedType extends BuiltinType implements Enumerated
{
	public EnumeratedType()
	{
		setEncoding( TagEncoding.universal( UniversalType.Enumerated ) );
	}

	private final List<NamedValue> enumeration = new ArrayList<>();
	private final List<NamedValue> additionalEnumeration = new ArrayList<>();

	private List<NamedValue> actualEnumeration;
	private List<NamedValue> actualAdditionalEnumeration;
	private boolean extensible;

	@Override
	public void addItem( @NotNull ItemKind kind, @NotNull String name, @Nullable Ref<Value> valueRef )
	{
		NamedValue value = new NamedValueImpl( name, valueRef );
		switch( kind )
		{
			case Primary:
				enumeration.add( value );
				break;

			case Extension:
				additionalEnumeration.add( value );
				break;

			default:
				throw new IllegalArgumentException( kind.name() );

		}
	}

	@NotNull
	public List<NamedValue> getEnumeration()
	{
		return Collections.unmodifiableList( enumeration );
	}

	public void setEnumeration( @NotNull Collection<NamedValue> enumeration )
	{
		this.enumeration.addAll( enumeration );
	}

	@NotNull
	public List<NamedValue> getAdditionalEnumeration()
	{
		return Collections.unmodifiableList( additionalEnumeration );
	}

	public void setAdditionalEnumeration( @NotNull Collection<NamedValue> additionalEnumeration )
	{
		this.additionalEnumeration.addAll( additionalEnumeration );
	}

	public boolean isExtensible()
	{
		return extensible;
	}

	@Override
	public void setExtensible( boolean value )
	{
		extensible = value;
	}

	@Nullable
	public NamedValue findValue( Value integerValue )
	{
		for( NamedValue value : actualEnumeration )
			if( integerValue.isEqualTo( value ) )
				return value;

		for( NamedValue value : actualAdditionalEnumeration )
			if( integerValue.isEqualTo( value ) )
				return value;

		return null;
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		for( NamedValue value : actualEnumeration )
			if( value.getName().equals( name ) )
				return value;

		for( NamedValue value : actualAdditionalEnumeration )
			if( value.getName().equals( name ) )
				return value;

		return null;
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		if( actualEnumeration == null )
			return Collections.emptyList();

		Collection<NamedValue> values = new ArrayList<>( actualEnumeration.size() + actualAdditionalEnumeration.size() );
		values.addAll( actualEnumeration );
		values.addAll( actualAdditionalEnumeration );
		return values;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		optimize( scope, valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = valueRef.resolve( scope );
		if( value.getKind() == Kind.Name )
		{
			Value registered = getNamedValue( ( (NamedValue)value ).getName() );
			if( registered != null )
				return registered;
		}

		value = RefUtils.toBasicValue( scope, value );
		if( value.getKind() == Kind.Integer )
		{
			NamedValue registered = findValue( value.toIntegerValue() );
			if( registered != null )
				return registered;
		}

		throw new IllegalValueException( "Unable to optimize value: " + valueRef );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.Enumerated;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		actualEnumeration = new ArrayList<>( enumeration.size() );
		Collection<Long> uniqueCheck = new HashSet<>();
		validateCollection( scope, enumeration, actualEnumeration, uniqueCheck );
		actualAdditionalEnumeration = new ArrayList<>( additionalEnumeration.size() );
		validateCollection( scope, additionalEnumeration, actualAdditionalEnumeration, uniqueCheck );
	}

	@Override
	protected void onDispose()
	{
		enumeration.clear();
		additionalEnumeration.clear();
		if( actualEnumeration != null )
		{
			actualEnumeration.clear();
			actualEnumeration = null;
		}
		if( actualAdditionalEnumeration != null )
		{
			actualAdditionalEnumeration.clear();
			actualAdditionalEnumeration = null;
		}
	}

	@NotNull
	@Override
	public Type copy()
	{
		EnumeratedType type = new EnumeratedType();
		type.setEnumeration( getEnumeration() );
		type.setAdditionalEnumeration( getAdditionalEnumeration() );
		type.setExtensible( isExtensible() );
		return type;
	}

	@Override
	public String toString()
	{
		if( extensible )
			return UniversalType.Enumerated.typeName() + '{' + StringUtils.join( enumeration, ", " ) + ", ...," + StringUtils.join( additionalEnumeration, ", " ) + '}';
		return UniversalType.Enumerated.typeName() + '{' + StringUtils.join( enumeration, ", " ) + '}';
	}


	private static void validateCollection( Scope scope, Iterable<NamedValue> iterable, Collection<NamedValue> result, Collection<Long> uniqueCheck ) throws ResolutionException, ValidationException
	{
		Collection<NamedValue> preprocessed = new ArrayList<>();
		for( NamedValue value : iterable )
		{
			value = value.resolve( scope ).toNamedValue();
			if( value.getReferenceKind() == Kind.Integer || value.getReferenceKind() == Kind.Empty )
				preprocessed.add( value );
			else throw new ValidationException( "Illegal value: " + value );
		}

		Long counter = 0L;
		for( NamedValue value : preprocessed )
		{
			if( value.getReferenceKind() == Kind.Integer )
			{
				result.add( value );
				continue;
			}

			counter = detectFreeId( uniqueCheck, counter );
			uniqueCheck.add( counter );
			result.add( new NamedValueImpl( value.getName(), new IntegerValueLong( counter ) ) );
		}
	}

	private static Long detectFreeId( Collection<Long> uniqueCheck, Long counter )
	{
		while( uniqueCheck.contains( counter ) )
			counter++;
		return counter;
	}
}
