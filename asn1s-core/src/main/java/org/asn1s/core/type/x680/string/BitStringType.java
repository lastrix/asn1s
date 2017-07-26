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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.ConstraintUtils;
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
import org.asn1s.core.type.AbstractBuiltinTypeWithNamedValues;
import org.asn1s.core.type.x680.IntegerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * X.680, p 22.1
 *
 * @author lastrix
 * @version 1.0
 */
public class BitStringType extends AbstractBuiltinTypeWithNamedValues
{
	private static final Log log = LogFactory.getLog( BitStringType.class );

	public BitStringType()
	{
		this( null );
	}

	public BitStringType( @Nullable Collection<NamedValue> namedValues )
	{
		super( namedValues );
		setEncoding( TagEncoding.universal( UniversalType.BitString ) );
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
			return optimizeCollection( scope, value );

		throw new IllegalValueException( "Unable to optimize value: " + valueRef );
	}

	@NotNull
	private static Value optimizeCollection( @NotNull Scope scope, Value value ) throws IllegalValueException, ResolutionException
	{
		int desiredSize = -1;
		if( Boolean.TRUE.equals( scope.getScopeOption( ConstraintUtils.OPTION_HAS_SIZE_CONSTRAINT ) ) )
		{
			assert scope.getScopeOption( ConstraintUtils.OPTION_SIZE_CONSTRAINT ) != null;
			//noinspection ConstantConditions
			desiredSize = scope.getScopeOption( ConstraintUtils.OPTION_SIZE_CONSTRAINT );
		}
		String bString = CollectionUtils.convertToBString( assertCollection( scope, value.toValueCollection() ), desiredSize );
		return CoreUtils.byteArrayFromBitString( bString );
	}

	@NotNull
	@Override
	public Type copy()
	{
		if( getValues() == null )
			log.warn( "Copying builtin type!" );
		return new BitStringType( getValues() );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( getValues() != null )
			setActualValues( IntegerType.buildIntegerTypeValues( scope.typedScope( this ), getValues(), true ) );
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
