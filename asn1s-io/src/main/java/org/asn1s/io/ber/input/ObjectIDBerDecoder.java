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

package org.asn1s.io.ber.input;

import org.asn1s.api.Ref;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.ObjectIdentifierValue;
import org.asn1s.io.ber.BerUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObjectIDBerDecoder implements BerDecoder
{
	@SuppressWarnings( "NumericCastThatLosesPrecision" )
	private static final byte MASK_MORE_BYTES = (byte)0x80;
	private static final int MASK_7_BIT = 0x7F;
	private static final long MAX_PLAIN_FIRST_OID = 80L;

	@Override
	public Value decode( @NotNull ReaderContext context ) throws IOException, Asn1Exception
	{
		assert context.getType().getFamily() == Family.Oid;
		assert context.getLength() > 0;

		List<Ref<Value>> list = new ArrayList<>();
		int length = context.getLength();
		while( length > 0 )
			length = readObjectIDItem( context.getReader(), length, list );

		ObjectIdentifierValue objectIdentifierValue = context.getValueFactory().objectIdentifier( list );
		return context.getType().optimize( context.getScope(), objectIdentifierValue );
	}

	private static int readObjectIDItem( AbstractBerReader is, int length, Collection<Ref<Value>> collection ) throws IOException
	{
		long value = 0;
		while( true )
		{
			byte buffer = is.read();
			length--;
			value = value << 7 | ( (long)buffer & MASK_7_BIT );

			if( ( buffer & MASK_MORE_BYTES ) == 0 )
				break;
		}
		addOidToCollection( is.getValueFactory(), collection, value );

		return length;
	}

	private static void addOidToCollection( ValueFactory factory, Collection<Ref<Value>> collection, long value )
	{
		if( !collection.isEmpty() )
			collection.add( factory.integer( value ) );
		else if( value < MAX_PLAIN_FIRST_OID )
		{
			collection.add( factory.integer( value / BerUtils.OID_FIRST_BYTE_MULTIPLIER ) );
			collection.add( factory.integer( value % BerUtils.OID_FIRST_BYTE_MULTIPLIER ) );
		}
		else
		{
			collection.add( factory.integer( 2 ) );
			collection.add( factory.integer( value - MAX_PLAIN_FIRST_OID ) );
		}
	}
}
