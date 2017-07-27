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
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.StringType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.StringValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.core.type.BuiltinType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Restricted string type
 */
public class StringTypeImpl extends BuiltinType implements StringType
{
	private static final Log log = LogFactory.getLog( StringTypeImpl.class );

	public StringTypeImpl( UniversalType type )
	{
		if( type.charset() == null )
			throw new IllegalArgumentException();

		this.type = type;
		setEncoding( TagEncoding.universal( type ) );
	}

	private final UniversalType type;

	@Override
	public Charset getCharset()
	{
		Charset charset = type.charset();
		if( charset == null )
			throw new IllegalStateException();
		return charset;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Kind.C_STRING )
		{
			assertCString( value );
			return;
		}

		if( value.getKind() == Kind.COLLECTION )
		{
			ValueCollection collection = value.toValueCollection();
			StringValue stringValue = tryBuildStringValue( scope, collection );
			if( stringValue != null )
				accept( scope, stringValue );
		}

		throw new IllegalValueException( "Unable to accept value of kind: " + value.getKind() );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Kind.C_STRING )
		{
			assertCString( value );
			return value;
		}

		if( value.getKind() == Kind.COLLECTION )
		{
			ValueCollection collection = value.toValueCollection();
			StringValue stringValue = tryBuildStringValue( scope, collection );
			if( stringValue != null )
			{
				assertCString( stringValue );
				return value;
			}
		}

		throw new IllegalValueException( "Unable to accept value of kind: " + value.getKind() );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.RESTRICTED_STRING;
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type" );
		return new StringTypeImpl( type );
	}

	@Override
	@Nullable
	public StringValue tryBuildStringValue( Scope scope, ValueCollection collection )
	{
		// TODO: unimplemented
		return null;
	}

	private void assertCString( Value value ) throws ConstraintViolationException
	{
		Pattern pattern = type.pattern();
		if( pattern != null && !pattern.matcher( value.toStringValue().asString() ).matches() )
			throw new ConstraintViolationException( "Unable to match input to pattern: " + type.pattern() );
	}

	@Override
	public String toString()
	{
		return type.name();
	}
}
