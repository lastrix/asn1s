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

package org.asn1s.core.type.x680.string;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.CollectionUtils;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.CoreUtils;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.type.x680.IntegerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * X.680, p 22.1
 *
 * @author lastrix
 * @version 1.0
 */
public class BitStringType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( BitStringType.class );

	public BitStringType()
	{
		this( null );
	}

	public BitStringType( @Nullable Collection<NamedValue> namedValues )
	{
		setEncoding( TagEncoding.universal( UniversalType.BitString ) );
		values = namedValues == null ? null : new ArrayList<>( namedValues );
	}

	private final List<NamedValue> values;
	private List<NamedValue> actualValues;

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		if( actualValues == null )
			return null;

		for( NamedValue value : actualValues )
			if( name.equals( value.getName() ) )
				return value;

		return null;
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		if( actualValues == null )
			return Collections.emptyList();

		return Collections.unmodifiableCollection( actualValues );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.BitString;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		boolean isConvertibleToBA = kind == Kind.CString && CoreUtils.isConvertibleToByteArrayValue( value.toStringValue().asString() );
		if( kind == Kind.Collection )
			assertCollection( scope, value.toValueCollection() );
		else if( kind != Kind.ByteArray && !isConvertibleToBA )
			throw new IllegalValueException( "Illegal BIT STRING value: " + valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind == Kind.ByteArray )
			return value;

		if( kind == Kind.Collection )
		{
			int desiredSize = -1;
			if( Boolean.TRUE.equals( scope.getScopeOption( Constraint.OPTION_HAS_SIZE_CONSTRAINT ) ) )
			{
				assert scope.getScopeOption( Constraint.OPTION_SIZE_CONSTRAINT ) != null;
				//noinspection ConstantConditions
				desiredSize = scope.getScopeOption( Constraint.OPTION_SIZE_CONSTRAINT );
			}
			String bString = CollectionUtils.convertToBString( assertCollection( scope, value.toValueCollection() ), desiredSize );
			return CoreUtils.byteArrayFromBitString( bString );
		}

		throw new IllegalValueException( "Unable to optimize value: " + valueRef );
	}

	@Override
	public String toString()
	{
		if( values == null )
			return UniversalType.BitString.typeName().toString();

		return UniversalType.BitString.typeName() + " { " + StringUtils.join( values, ", " ) + " }";
	}

	@NotNull
	@Override
	public Type copy()
	{
		if( values == null )
			log.warn( "Copying builtin type!" );
		return new BitStringType( values );
	}

	@Override
	protected void onDispose()
	{
		if( values != null )
			values.clear();

		if( actualValues != null )
		{
			actualValues.clear();
			actualValues = null;
		}
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( values != null )
			actualValues = IntegerType.buildIntegerTypeValues( scope, values, true );
	}

	private static Iterable<Value> assertCollection( Scope scope, ValueCollection collection ) throws IllegalValueException, ResolutionException
	{
		Collection<Value> references = new HashSet<>();
		for( Ref<Value> valueRef : collection.asValueList() )
		{
			Value value = valueRef.resolve( scope );
			if( references.contains( value ) )
				throw new IllegalValueException( "Duplicate value: " + value );
			references.add( value );
		}
		return references;
	}
}
