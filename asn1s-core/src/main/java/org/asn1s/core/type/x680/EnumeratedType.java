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
		this( null, null, false );
	}

	public EnumeratedType( @Nullable Collection<NamedValue> enumeration, @Nullable Collection<NamedValue> additionalEnumeration, boolean extensible )
	{
		if( enumeration != null )
			this.enumeration.addAll( enumeration );
		if( additionalEnumeration != null )
			this.additionalEnumeration.addAll( additionalEnumeration );
		this.extensible = extensible;
		setEncoding( TagEncoding.universal( UniversalType.ENUMERATED ) );
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
			case PRIMARY:
				enumeration.add( value );
				break;

			case EXTENSION:
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

	@NotNull
	public List<NamedValue> getAdditionalEnumeration()
	{
		return Collections.unmodifiableList( additionalEnumeration );
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

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = valueRef.resolve( scope );
		if( value.getKind() == Kind.NAME )
		{
			Value registered = getNamedValue( ( (NamedValue)value ).getName() );
			if( registered != null )
				return registered;
		}

		value = RefUtils.toBasicValue( scope, value );
		if( value.getKind() == Kind.INTEGER )
		{
			NamedValue registered = findValue( value.toIntegerValue() );
			if( registered != null )
				return registered;
		}

		throw new IllegalValueException( "Unable to optimize value: " + valueRef );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		//scope = scope.typedScope( this );
		Collection<Long> uniqueCheck = new HashSet<>();
		actualEnumeration = new EnumerationValidator( scope, enumeration, uniqueCheck )
				.validate();
		actualAdditionalEnumeration = new EnumerationValidator( scope, additionalEnumeration, uniqueCheck )
				.validate();
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
		return new EnumeratedType( getEnumeration(), getAdditionalEnumeration(), extensible );
	}

	@Override
	public String toString()
	{
		if( extensible )
			return UniversalType.ENUMERATED.typeName().toString() + '{' + StringUtils.join( enumeration, ", " ) + ", ...," + StringUtils.join( additionalEnumeration, ", " ) + '}';
		return UniversalType.ENUMERATED.typeName().toString() + '{' + StringUtils.join( enumeration, ", " ) + '}';
	}

	private static final class EnumerationValidator
	{
		private final Scope scope;
		private final List<NamedValue> enumeration;
		private final Collection<Long> uniqueCheck;

		private EnumerationValidator( Scope scope, List<NamedValue> enumeration, Collection<Long> uniqueCheck )
		{
			this.scope = scope;
			this.enumeration = enumeration;
			this.uniqueCheck = uniqueCheck;
		}

		List<NamedValue> validate() throws ResolutionException, ValidationException
		{
			List<NamedValue> list = new ArrayList<>( enumeration.size() );
			validateCollection( list );
			return list;
		}

		private void validateCollection( Collection<NamedValue> result ) throws ResolutionException, ValidationException
		{
			Long counter = 0L;
			for( NamedValue value : preProcessValues() )
			{
				if( value.getReferenceKind() == Kind.INTEGER )
				{
					result.add( value );
					continue;
				}

				counter = detectFreeId( uniqueCheck, counter );
				uniqueCheck.add( counter );
				result.add( new NamedValueImpl( value.getName(), new IntegerValueLong( counter ) ) );
			}
		}

		@NotNull
		private Iterable<NamedValue> preProcessValues() throws ResolutionException, ValidationException
		{
			Collection<NamedValue> preprocessed = new ArrayList<>();
			for( NamedValue value : enumeration )
			{
				value = value.resolve( scope ).toNamedValue();
				if( value.getReferenceKind() == Kind.INTEGER || value.getReferenceKind() == Kind.EMPTY )
					preprocessed.add( value );
				else throw new ValidationException( "Illegal value: " + value );
			}
			return preprocessed;
		}

		private static Long detectFreeId( Collection<Long> uniqueCheck, Long counter )
		{
			while( uniqueCheck.contains( counter ) )
				counter++;
			return counter;
		}
	}
}
