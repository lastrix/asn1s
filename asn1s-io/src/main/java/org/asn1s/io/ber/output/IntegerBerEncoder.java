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

package org.asn1s.io.ber.output;

import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Encodes integer in smallest amount of bytes.
 * See X.690, p 8.3
 */
@SuppressWarnings( "NumericCastThatLosesPrecision" )
final class IntegerBerEncoder implements BerEncoder
{
	private static final Tag TAG = new Tag( TagClass.Universal, false, UniversalType.Integer.tagNumber() );
	private static final byte[] EMPTY_ARRAY = new byte[0];

	@Override
	public void encode( @NotNull BerWriter os, @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		if( value.getKind() != Kind.Integer )
			throw new IllegalValueException( "Unable to encode values of kind: " + value.getKind() );

		writeLong( os, value.toIntegerValue().asLong(), TAG, writeHeader );
	}

	public static void writeLong( @NotNull BerWriter os, long value, Tag tag, boolean writeHeader ) throws IOException
	{
		boolean skipping = true;
		for( int i = 7; i >= 0; i-- )
		{
			int current = (int)( ( value >> ( i * 8 ) ) & BerUtils.BYTE_MASK );
			if( skipping )
			{
				int next = i > 0 ? (int)( ( value >> ( ( i - 1 ) * 8 ) ) & BerUtils.BYTE_MASK ) : 0;
				// if 9 zeros or ones follows each other - skip them all together
				if( i > 0
						&& (
						current == BerUtils.BYTE_MASK && ( next & BerUtils.BYTE_SIGN_MASK ) != 0 || current == 0x00 && ( next & BerUtils.BYTE_SIGN_MASK ) == 0
				) )
					continue;
			}

			if( skipping )
			{
				if( writeHeader )
					os.writeHeader( tag, i + 1 );

				skipping = false;
			}

			os.write( current );
		}
	}

	public static byte[] toByteArray( long value )
	{
		boolean skipping = true;
		byte[] result = null;
		int position = 0;
		for( int i = 7; i >= 0; i-- )
		{
			int current = (int)( ( value >> ( i * 8 ) ) & BerUtils.BYTE_MASK );
			if( skipping )
			{
				int next = i > 0 ? (int)( ( value >> ( ( i - 1 ) * 8 ) ) & BerUtils.BYTE_MASK ) : 0;
				// if 9 zeros or ones follows each other - skip them all together
				if( i > 0
						&& (
						current == BerUtils.BYTE_MASK && ( next & BerUtils.BYTE_SIGN_MASK ) != 0 || current == 0x00 && ( next & BerUtils.BYTE_SIGN_MASK ) == 0
				) )
					continue;
			}

			if( skipping )
			{
				result = new byte[i + 1];
				skipping = false;
			}

			result[position] = (byte)current;
			position++;
		}
		if( result == null )
			result = EMPTY_ARRAY;
		return result;
	}
}
