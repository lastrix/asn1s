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

import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Encodes integer in smallest amount of bytes.
 * See X.690, p 8.3
 */
final class IntegerBerEncoder implements BerEncoder
{
	private static final Tag TAG = new Tag( TagClass.UNIVERSAL, false, UniversalType.INTEGER.tagNumber() );

	@Override
	public void encode( @NotNull WriterContext context ) throws IOException
	{
		assert context.getType().getFamily() == Family.INTEGER;
		assert context.getValue().getKind() == Kind.INTEGER;
		writeLong( context.getWriter(), context.getValue().toIntegerValue().asLong(), TAG, context.isWriteHeader() );
	}

	static void writeLong( @NotNull AbstractBerWriter os, long value, @Nullable Tag tag, boolean writeHeader ) throws IOException
	{
		if( tag == null )
		{
			if( writeHeader )
				throw new IOException( "Unable to write header: tag is unavailable." );
		}

		int size = calculateByteCount( value );
		if( writeHeader )
			os.writeHeader( tag, size );

		for( int i = size - 1; i >= 0; i-- )
			os.write( getByteByIndex( value, i ) );
	}

	static byte[] toByteArray( long value )
	{
		int size = calculateByteCount( value );
		byte[] result = new byte[size];
		for( int i = size - 1, position = 0; i >= 0; i--, position++ )
			result[position] = getByteByIndex( value, i );

		return result;
	}

	private static int calculateByteCount( long value )
	{
		int i;
		for( i = 7; i >= 0; i-- )
		{
			byte current = getByteByIndex( value, i );
			byte next = i > 0 ? getByteByIndex( value, i - 1 ) : 0;
			// if 9 zeros or ones follows each other - skip them all together
			if( i == 0 || !isSkipping( current, next ) )
				break;
		}
		return i + 1;
	}

	private static byte getByteByIndex( long value, int index )
	{
		//noinspection NumericCastThatLosesPrecision
		return (byte)( ( value >> ( index * 8 ) ) & BerUtils.BYTE_MASK );
	}

	private static boolean isSkipping( byte current, byte next )
	{
		return current == BerUtils.BYTE_MASK_B && ( next & BerUtils.BYTE_SIGN_MASK_B ) != 0
				|| current == 0x00 && ( next & BerUtils.BYTE_SIGN_MASK_B ) == 0;
	}
}
