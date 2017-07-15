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

import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.value.ValueFactory;

import java.io.IOException;
import java.io.InputStream;

public class DefaultBerReader extends AbstractBerReader
{
	public DefaultBerReader( InputStream is, ValueFactory valueFactory )
	{
		super( valueFactory );
		this.is = is;
		position = 0;
	}

	private final InputStream is;
	private int position;

	@Override
	public int position()
	{
		return position;
	}

	@Override
	public void skip( int amount ) throws IOException
	{
		if( amount != 0 && is.skip( amount ) != amount )
			throw new IOException( "Unexpected EOF" );
	}

	@Override
	public void skipToEoc() throws IOException
	{
		Tag tag = readTag();
		int length = readLength();

		if( tag.isEoc() )
		{
			if( length != 0 )
				throw new IOException( "Corrupted data, tag is EOC, but length is non zero" );
		}
		else
		{
			if( length >= 0 )
				skip( length );
			else
				skipToEoc();
		}
	}

	@SuppressWarnings( "NumericCastThatLosesPrecision" )
	@Override
	public byte read() throws IOException
	{
		int read = is.read();
		if( read != -1 )
			position++;
		return (byte)read;
	}

	@Override
	public int read( byte[] buffer ) throws IOException
	{
		int read = is.read( buffer );
		if( read != -1 )
			position += read;
		return read;
	}

	@Override
	public void close() throws Exception
	{
		is.close();
	}
}
