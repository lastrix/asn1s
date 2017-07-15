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
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

interface BerEncoder
{
	/**
	 * Write value to Ber OutputStream.
	 * Encoders MUST NOT write TAG, only length is allowed to be written by one.
	 *
	 * @param os          the tag output stream
	 * @param scope       the resolution scope
	 * @param type        type info
	 * @param value       value to write
	 * @param writeHeader if true encoder must write the object header
	 * @throws IOException   if io fails
	 * @throws Asn1Exception if resolution fails, value can not be encoded
	 */
	void encode( @NotNull BerWriter os, @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception;
}
