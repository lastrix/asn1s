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

package org.asn1s.core.value.x680;

import org.asn1s.api.util.HexUtils;
import org.asn1s.api.value.ByteArrayValue;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ByteArrayValueImpl implements ByteArrayValue
{
	private static final byte[] EMPTY = new byte[0];

	public ByteArrayValueImpl( int bits, @Nullable byte[] bytes )
	{
		this.bits = bits;
		this.bytes = bytes == null ? EMPTY : bytes;
	}

	private final int bits;
	private final byte[] bytes;

	@Override
	public int getUsedBits()
	{
		return bits;
	}

	@Override
	public byte[] asByteArray()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return bytes;
	}

	@Override
	public int size( boolean bits )
	{
		if( bits )
			return this.bits;

		return bytes.length;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.ByteArray
				|| o.getKind() == Kind.Name && o.toNamedValue().getReferenceKind() == Kind.ByteArray )
		{
			ByteArrayValue other = o.toByteArrayValue();
			if( isEmpty() && other.isEmpty() )
				return 0;
			return compareTo( bytes, other.asByteArray() );
		}

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof ByteArrayValue ) ) return false;

		ByteArrayValue byteArrayValue = (ByteArrayValue)obj;

		return Arrays.equals( asByteArray(), byteArrayValue.asByteArray() );
	}

	@Override
	public int hashCode()
	{
		return Arrays.hashCode( bytes );
	}

	private static int compareTo( byte[] left, byte[] right )
	{
		int length = Math.min( left.length, right.length );
		int i;
		for( i = 0; i < length; i++ )
		{
			int result = Byte.compare( left[i], right[i] );
			if( result != 0 )
				return result;
		}
		if( left.length == right.length )
			return 0;

		if( left.length > i )
		{
			if( isRestZeros( left, i ) )
				return 0;
		}
		else
		{
			if( isRestZeros( right, i ) )
				return 0;
		}

		return Integer.compare( left.length, right.length );
	}

	private static boolean isRestZeros( byte[] bytes, int position )
	{
		for( int i = position; i < bytes.length; i++ )
			if( bytes[i] != 0 )
				return false;

		return true;
	}

	@Override
	public boolean isEmpty()
	{
		if( bytes.length == 0 )
			return true;

		for( byte aByte : bytes )
		{
			if( aByte != 0 )
				return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return HexUtils.toHexString( asByteArray() );
	}
}
