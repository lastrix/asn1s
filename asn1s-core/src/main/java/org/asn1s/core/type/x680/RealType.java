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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.IntegerValue;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.RealValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.value.CoreValueFactory;
import org.asn1s.core.value.x680.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * X.680, p 21
 *
 * @author lastrix
 * @version 1.0
 */
public final class RealType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( RealType.class );

	public RealType()
	{
		setEncoding( TagEncoding.universal( UniversalType.Real ) );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind != Kind.Integer && kind != Kind.Real )
			throw new IllegalValueException( "Only numeric types acceptable" );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.Real;
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type!" );
		return new RealType();
	}

	@Override
	public String toString()
	{
		return UniversalType.Real.typeName().toString();
	}

	@Override
	protected void onValidate( @NotNull Scope scope )
	{
		// do nothing
	}

	@Override
	protected void onDispose()
	{
		// do nothing
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind == Kind.Real )
			return value;

		if( kind == Kind.Integer )
		{
			if( value instanceof IntegerValueInt )
				return new RealValueFloat( ( (IntegerValue)value ).asFloat() );

			if( value instanceof IntegerValueLong )
				return new RealValueDouble( ( (IntegerValue)value ).asDouble() );

			if( value instanceof IntegerValueBig )
				return new RealValueBig( ( (IntegerValue)value ).asBigDecimal() );

			throw new IllegalStateException( "Unsupported REAL class: " + valueRef.getClass().getName() );
		}

		if( kind == Kind.NamedCollection )
		{
			ValueCollection collection = value.toValueCollection();
			List<Value> values = new ArrayList<>( collection.asNamedValueList() );
			RealValue result = tryBuildRealValue( values );
			if( result != null )
				return result;
		}

		throw new IllegalValueException( "Unable to optimize value of kind: " + kind + ". Value: " + valueRef );
	}


	@SuppressWarnings( "MagicNumber" )
	@Nullable
	private static RealValue tryBuildRealValue( @NotNull List<Value> values )
	{
		if( values.size() != 3 )
			return null;

		Value mantisValue = values.get( 0 );
		Value baseValue = values.get( 1 );
		Value exponentValue = values.get( 2 );

		Long mantis = tryRecoverLong( mantisValue, "mantissa" );
		Long base = tryRecoverLong( baseValue, "base" );
		Long exponent = tryRecoverLong( exponentValue, "exponent" );
		if( mantis == null || base == null || exponent == null || ( base != 2L && base != 10L ) )
			return null;

		long e = exponent;
		if( ( ( e < 0 ? -e : e ) & ~0x00FFFFFFFFL ) != 0 )
			throw new IllegalStateException( "Exponent overflow" );

		boolean negative = mantis < 0;
		if( negative )
			mantis = -mantis;

		assert Math.abs( e ) <= Integer.MAX_VALUE;
		//noinspection NumericCastThatLosesPrecision
		return CoreValueFactory.createReal( mantis, base == 10L, (int)e, negative );
	}

	@Nullable
	private static Long tryRecoverLong( Value value, String name )
	{
		if( value.getKind() != Kind.Name )
			return null;

		NamedValue namedValue = value.toNamedValue();
		if( !name.equals( namedValue.getName() ) || namedValue.getReferenceKind() != Kind.Integer )
			return null;

		IntegerValue integerValue = namedValue.toIntegerValue();
		if( integerValue.isLong() )
			return integerValue.asLong();
		return null;
	}
}
