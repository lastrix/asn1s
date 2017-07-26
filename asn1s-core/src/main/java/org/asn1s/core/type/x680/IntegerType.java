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
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.type.AbstractBuiltinTypeWithNamedValues;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * X.680, p 19
 *
 * @author lastrix
 * @version 1.0
 */
public class IntegerType extends AbstractBuiltinTypeWithNamedValues
{
	private static final Log log = LogFactory.getLog( IntegerType.class );

	//////////////////////////////////// Construction //////////////////////////////////////////////////////////////////
	public IntegerType()
	{
		this( null );
	}

	public IntegerType( @Nullable Collection<NamedValue> namedValues )
	{
		super( namedValues );
		setEncoding( TagEncoding.universal( UniversalType.INTEGER ) );
	}

	//////////////////////////////////// Overrides /////////////////////////////////////////////////////////////////////

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.INTEGER;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind != Kind.INTEGER )
			throw new IllegalValueException( "Not an integer value: " + value );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind == Kind.INTEGER )
			return value;

		throw new IllegalValueException( "Unable to optimize value of kind: " + kind + ". Value: " + value );
	}

	@NotNull
	@Override
	public Type copy()
	{
		if( getValues() == null )
			log.warn( "Copying builtin type, this may be an error!" );
		return new IntegerType( getValues() );
	}


	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( getValues() != null )
			setActualValues( buildIntegerTypeValues( scope.typedScope( this ), getValues(), false ) );
	}

	//////////////////////////////////// Implementation ////////////////////////////////////////////////////////////////

	public static List<NamedValue> buildIntegerTypeValues( @NotNull Scope scope, @NotNull Iterable<NamedValue> values, boolean onlyPositive ) throws ValidationException, ResolutionException
	{
		List<NamedValue> result = new ArrayList<>();
		Collection<Long> uniqueCheck = new HashSet<>();
		for( NamedValue value : values )
			result.add( validateIntegerTypeValue( scope, value, uniqueCheck, onlyPositive ) );

		return result;
	}

	private static NamedValue validateIntegerTypeValue( Scope scope, NamedValue value, Collection<Long> uniqueCheck, boolean onlyPositive ) throws ValidationException, ResolutionException
	{
		value = value.resolve( scope ).toNamedValue();
		if( value.getReferenceKind() != Kind.INTEGER )
			throw new ValidationException( "Named value references non-integer value" );

		Long longValue = value.toIntegerValue().asLong();
		if( uniqueCheck.contains( longValue ) )
			throw new ValidationException( "Duplicate value found!" );

		if( onlyPositive && longValue < 0L )
			throw new ValidationException( "Only positive values allowed!" );

		return value;
	}
}
