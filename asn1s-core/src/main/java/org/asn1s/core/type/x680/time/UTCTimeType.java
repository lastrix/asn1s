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

package org.asn1s.core.type.x680.time;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.util.TimeUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.value.x680.DateValueImpl;
import org.jetbrains.annotations.NotNull;

/**
 * X.680, p 47.1
 * UTCTime ::= [UNIVERSAL 23] IMPLICIT VisibleString
 *
 * @author lastrix
 * @version 1.0
 */
public final class UTCTimeType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( UTCTimeType.class );

	public UTCTimeType()
	{
		setEncoding( TagEncoding.create( TagMethod.Unknown, TagMethod.Implicit, TagClass.Universal, UniversalType.UTCTime.tagNumber() ) );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		if( value.getKind() != Kind.Time )
			throw new IllegalValueException( "Unable to use value: " + value );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind == Kind.Time )
			return value;

		if( kind == Kind.CString )
		{
			String timeValueString = value.toStringValue().asString();
			if( TimeUtils.isUTCTimeValue( timeValueString ) )
				return new DateValueImpl( TimeUtils.parseUTCTime( timeValueString ) );
		}

		throw new IllegalValueException( "Unable to optimize value: " + valueRef );
	}

	@Override
	public String toString()
	{
		return UniversalType.UTCTime.typeName().toString();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.UTCTime;
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type!" );
		return new UTCTimeType();
	}

	@Override
	protected void onValidate( @NotNull Scope scope )
	{
	}

	@Override
	protected void onDispose()
	{
	}
}
