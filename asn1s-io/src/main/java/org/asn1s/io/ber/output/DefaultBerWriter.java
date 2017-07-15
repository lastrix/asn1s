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

import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Deque;
import java.util.LinkedList;

public final class DefaultBerWriter extends AbstractBerWriter
{
	public DefaultBerWriter( BerRules rules )
	{
		this.rules = rules;
		os = new ByteArrayOutputStream();
		internalOs = true;
	}

	public DefaultBerWriter( BerRules rules, OutputStream os )
	{
		this.rules = rules;
		this.os = os;
		internalOs = false;
	}

	private final BerRules rules;
	private final OutputStream os;
	private final Deque<ByteArrayOutputStream> outputStack = new LinkedList<>();
	private final boolean internalOs;

	@Override
	public BerRules getRules()
	{
		return rules;
	}

	@Override
	public boolean isBufferingAvailable()
	{
		return true;
	}

	@Override
	public void startBuffer( int sizeHint )
	{
		outputStack.push( new ByteArrayOutputStream() );
	}

	@Override
	public void stopBuffer( @NotNull Tag tag ) throws IOException
	{
		try( ByteArrayOutputStream stream = outputStack.pop() )
		{
			byte[] bytes = stream.toByteArray();
			writeHeader( tag, bytes.length );
			write( bytes );
		}
	}

	@Override
	public void write( int aByte ) throws IOException
	{
		//noinspection resource
		OutputStream outputStream = outputStack.peek();
		if( outputStream == null )
			outputStream = os;
		outputStream.write( aByte );
	}

	@Override
	public void write( byte[] bytes ) throws IOException
	{
		//noinspection resource
		OutputStream outputStream = outputStack.peek();
		if( outputStream == null )
			outputStream = os;
		outputStream.write( bytes );
	}

	@Override
	public void close() throws Exception
	{
		if( internalOs )
			os.close();
	}

	@Override
	public byte[] toByteArray() throws IOException
	{
		if( !internalOs )
			throw new IOException( "Unable to get bytes from provided stream" );

		return ( (ByteArrayOutputStream)os ).toByteArray();
	}
}
