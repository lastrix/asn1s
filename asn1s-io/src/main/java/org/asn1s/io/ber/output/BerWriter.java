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
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

interface BerWriter extends Asn1Writer
{
	BerRules getRules();

	/**
	 * Should return true if writer supports stacked buffering
	 *
	 * @return boolean
	 */
	boolean isBufferingAvailable();

	/**
	 * Add level of buffering, allowing coders to write data and compute length after all jobs done
	 *
	 * @param sizeHint probable amount of bytes. If set to -1, then buffer size must be unlimited
	 */
	void startBuffer( int sizeHint );

	/**
	 * Write buffer data into underlying stream, write tag and length before any data copying.
	 *
	 * @param tag the data tag
	 * @throws IOException in case of I/O failure
	 */
	void stopBuffer( @NotNull Tag tag ) throws IOException;

	void write( int aByte ) throws IOException;

	void write( byte[] bytes ) throws IOException;

	/**
	 * Used by encoders to delegate work, for example, in sequences, sets
	 *
	 * @param scope       the resolution scope
	 * @param type        type info
	 * @param value       the value to write
	 * @param writeHeader if true - all headers must be written
	 * @throws IOException   if io fails
	 * @throws Asn1Exception if resolution fails
	 */
	void writeInternal( Scope scope, Type type, Value value, boolean writeHeader ) throws IOException, Asn1Exception;

	void writeTag( @NotNull Tag tag ) throws IOException;

	void writeTag( @NotNull TagClass tagClass, boolean constructed, long tagNumber ) throws IOException;

	/**
	 * Write length to output stream
	 *
	 * @param length -1, 0 or positive number
	 * @throws IOException if IO fails
	 */
	void writeLength( int length ) throws IOException;

	default void writeHeader( Tag tag, int length ) throws IOException
	{
		writeTag( tag );
		writeLength( length );
	}
}
