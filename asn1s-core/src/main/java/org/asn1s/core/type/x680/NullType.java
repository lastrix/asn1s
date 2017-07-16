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
import org.asn1s.core.type.BuiltinType;
import org.jetbrains.annotations.NotNull;

/**
 * X.680, p 24
 *
 * @author lastrix
 * @version 1.0
 */
public class NullType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( NullType.class );

	public NullType()
	{
		setEncoding( TagEncoding.universal( UniversalType.Null ) );
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
		if( value.getKind() == Kind.Null )
			return value;

		throw new IllegalValueException( "Unable to optimize value of kind: " + value.getKind() + ". Value: " + valueRef );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.Null;
	}

	@Override
	public String toString()
	{
		return UniversalType.Null.typeName().toString();
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type!" );
		return new NullType();
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		// do nothing
	}

	@Override
	protected void onDispose()
	{
		// do nothing
	}
}
