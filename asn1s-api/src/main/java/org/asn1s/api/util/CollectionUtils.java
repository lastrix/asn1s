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

package org.asn1s.api.util;

import org.asn1s.api.Ref;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class CollectionUtils
{

	private static final byte[] EMPTY_ARRAY = new byte[0];

	private CollectionUtils()
	{
	}

	/**
	 * Convert list of named bits to string of 0 and 1.
	 *
	 * @param valueList   the list of named bits
	 * @param desiredSize the size of result or -1
	 * @return bit string
	 * @throws IllegalArgumentException if actual result is bigger than desiredSize
	 */
	@NotNull
	public static String convertToBString( Iterable<? extends Ref<Value>> valueList, int desiredSize )
	{
		Collection<Long> values = new HashSet<>();
		Long maxValue = 0L;
		for( Ref<Value> valueRef : valueList )
		{
			NamedValue value = (NamedValue)valueRef;
			if( value.getReferenceKind() != Kind.Integer || !value.toIntegerValue().isLong() )
				throw new IllegalStateException();

			Long longValue = value.toIntegerValue().asLong();
			values.add( longValue );
			maxValue = Math.max( maxValue, longValue );
		}

		if( desiredSize > -1 )
		{
			if( maxValue > ( desiredSize - 1 ) )
				throw new IllegalArgumentException( "Unable to truncate data. Desired size is smaller than expected: current = " + ( maxValue + 1 ) + " desired = " + desiredSize );

			maxValue = (long)desiredSize - 1;
		}
		StringBuilder sb = new StringBuilder();
		sb.append( '\'' );
		for( long value = 0; value <= maxValue; value++ )
			sb.append( values.contains( value ) ? '1' : '0' );
		sb.append( "'B" );
		return sb.toString();
	}

	@Nullable
	public static BigInteger tryBuildBigInteger( @NotNull ValueCollection collection )
	{
		if( collection.getKind() != Kind.Collection
				|| collection.asValueList().isEmpty() )
			return null;

		List<Ref<Value>> list = collection.asValueList();
		BigInteger result = BigInteger.ZERO;
		for( Ref<Value> valueRef : list )
		{
			if( !( valueRef instanceof NamedValue ) )
				return null;

			NamedValue namedValue = (NamedValue)valueRef;

			if( namedValue.getReferenceKind() != Kind.Integer
					|| !namedValue.toIntegerValue().isInt() )
				return null;

			int bit = namedValue.toIntegerValue().asInt();
			result = result.setBit( bit );
		}

		return result;
	}
}
