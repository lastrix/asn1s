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
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.ObjectIdentifierValue;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ObjectIDBerEncoder implements BerEncoder
{
	private static final Tag TAG = new Tag( TagClass.Universal, false, UniversalType.ObjectIdentifier.tagNumber() );
	private static final int MASK_7_BIT = 0x7F;
	private static final int MASK_MORE_BYTES = 0x80;
	private static final int MASK_NO_BYTES = 0x0;

	@Override
	public void encode( @NotNull WriterContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == Family.Oid;
		assert context.getValue().getKind() == Kind.Oid;

		if( !context.isWriteHeader() )
			writeObjectIDImpl( context.getWriter(), context.getValue().toObjectIdentifierValue() );
		else if( context.isBufferingAvailable() )
		{
			context.startBuffer( -1 );
			writeObjectIDImpl( context.getWriter(), context.getValue().toObjectIdentifierValue() );
			context.stopBuffer( TAG );
		}
		else
			throw new Asn1Exception( "Buffering is required for ObjectIdentifier" );
	}

	private static void writeObjectIDImpl( AbstractBerWriter os, ObjectIdentifierValue value ) throws IOException
	{
		Long[] array = value.asIDArray();
		long first = array[0] * BerUtils.OID_FIRST_BYTE_MULTIPLIER + array[1];

		writeObjectIDItem( first, os );
		for( int i = 2; i < array.length; i++ )
			writeObjectIDItem( array[i], os );
	}

	private static void writeObjectIDItem( long item, AbstractBerWriter os ) throws IOException
	{
		assert item >= 0;
		if( item == 0 )
		{
			os.write( 0 );
			return;
		}

		int n = 0;
		while( ( item >> n ) != 0 )
			n += 7;
		if( ( item >> n ) == 0 )
			n -= 7;

		for( int i = n; i >= 0; i -= 7 )
		{
			//noinspection NumericCastThatLosesPrecision
			byte value = (byte)( ( item >> i ) & MASK_7_BIT | ( i == 0 ? MASK_NO_BYTES : MASK_MORE_BYTES ) );
			os.write( value );
		}
	}
}
