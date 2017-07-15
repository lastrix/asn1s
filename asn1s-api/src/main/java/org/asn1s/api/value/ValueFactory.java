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

package org.asn1s.api.value;

import org.asn1s.api.Ref;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.x680.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

public interface ValueFactory
{
	@NotNull
	RealValue rZero();

	@NotNull
	RealValue rNegativeZero();

	@NotNull
	RealValue rPositiveInfinity();

	@NotNull
	RealValue rNegativeInfinity();

	@NotNull
	RealValue rNan();

	@NotNull
	RealValue real( @NotNull String value );

	@NotNull
	RealValue real( float value );

	@NotNull
	RealValue real( double value );

	@NotNull
	RealValue real( @NotNull BigDecimal value );

	@NotNull
	RealValue real( long mantissa, boolean decimal, int exponent, boolean negative );

	@NotNull
	RealValue real( BigInteger mantissa, boolean decimal, int exponent, boolean negative );

	@NotNull
	RealValue real( @NotNull IntegerValue mantissa, boolean decimal, @NotNull IntegerValue exponent, boolean negative );

	@NotNull
	IntegerValue integer( @NotNull byte[] bytes );

	@NotNull
	IntegerValue integer( @NotNull String value );

	@NotNull
	IntegerValue integer( int value );

	@NotNull
	IntegerValue integer( long value );

	@NotNull
	IntegerValue integer( @NotNull BigInteger value );

	@NotNull
	StringValue cString( @NotNull String value );

	@NotNull
	NamedValue named( @NotNull String name, @Nullable Ref<Value> valueRef );

	@NotNull
	ValueCollection collection( boolean named );

	@NotNull
	ByteArrayValue hString( @NotNull String content );

	@NotNull
	ByteArrayValue bString( @NotNull String content );

	@NotNull
	ByteArrayValue byteArrayValue( int bits, @Nullable byte[] bytes );

	@NotNull
	ByteArrayValue emptyByteArray();

	@NotNull
	OpenTypeValue openTypeValue( @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef );

	@NotNull
	DateValue timeValue( @NotNull Instant instant );

	@NotNull
	ObjectIdentifierValue objectIdentifier( @NotNull List<Ref<Value>> oidRefs );
}
