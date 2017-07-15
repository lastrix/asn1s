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

package org.asn1s.io;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface Asn1Writer extends AutoCloseable
{

	/**
	 * Serialize value using type info
	 *
	 * @param scope   the resolution scope, must not be null
	 * @param typeRef type ref to use for serialization, must point to valid type
	 * @param value   the value to encode, the typeRef must point to type that should accept value, or otherwise IllegalValueException thrown
	 * @throws IOException   in case of IO problem
	 * @throws Asn1Exception if value is not accepted, type cannot be resolved or constraint failed
	 */
	void write( @NotNull Scope scope, @NotNull Ref<Type> typeRef, @NotNull Value value ) throws IOException, Asn1Exception;

	/**
	 * Recover all written data as byte array.
	 * Throws UnsupportedOperationException by default.
	 *
	 * @return byte array
	 * @throws IOException if io fails
	 */
	default byte[] toByteArray() throws IOException
	{
		throw new UnsupportedOperationException();
	}
}
