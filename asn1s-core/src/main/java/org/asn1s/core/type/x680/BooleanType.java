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
import org.asn1s.api.value.x680.BooleanValue;
import org.asn1s.core.type.BuiltinType;
import org.jetbrains.annotations.NotNull;

/**
 * X.680, p 18
 *
 * @author lastrix
 * @version 1.0
 */
public final class BooleanType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( BooleanType.class );

	public BooleanType()
	{
		setEncoding( TagEncoding.universal( UniversalType.BOOLEAN ) );
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
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind == Kind.BOOLEAN )
			return value;

		if( kind == Kind.INTEGER )
			return value.toIntegerValue().isZero() ? BooleanValue.FALSE : BooleanValue.TRUE;

		throw new IllegalValueException( "Unable to optimize value: " + valueRef );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.BOOLEAN;
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type, this may be an error!" );
		// although you shouldn't copy this type at all
		return new BooleanType();
	}

	@Override
	public String toString()
	{
		return UniversalType.BOOLEAN.typeName().toString();
	}
}
