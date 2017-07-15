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

package org.asn1s.databind.mapper;

import org.asn1s.api.UniversalType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

public interface TypeMapper
{
	String MARKER_GLOBAL_VARIABLE = "$";
	String MARKER_LOCAL_VARIABLE = "#";

	/**
	 * Map type into ASN.1 Schema, resolve ASN.1 type name using annotations or stub generation in form:
	 * T-Java-Bind-{typeNameWithDotsReplacedByMinus}
	 *
	 * @param type the type to map
	 * @return mapped type with all information
	 */
	@NotNull
	MappedType mapType( @NotNull Type type );

	/**
	 * Map type into ASN.1 Schema, resolve ASN.1 type name (if parameter asn1TypeName is null)
	 * using annotations or stub generation in form:
	 * T-Java-Bind-{typeNameWithDotsReplacedByMinus}
	 *
	 * @param type         the type to map
	 * @param asn1TypeName the asn.1 type name which should be used for mapping,
	 *                     may be an existing ASN.1 Type loaded into context module
	 * @return mapped type
	 */
	@NotNull
	MappedType mapType( @NotNull Type type, @Nullable String asn1TypeName );

	/**
	 * Bind class for universal type, it will require special adapter to handle them.
	 *
	 * @param type          the type to be bound with universal type
	 * @param universalType the universal type to bound with type
	 * @param isDefault     set to true if binding must be default for type.
	 *                      Example Instant=>UTCTime and Instant=>GeneralizedTime, only one of them
	 *                      should have isDefault flag set to true. When true, the method will add
	 *                      another binding if user don't know how target ASN.1 is called
	 */
	void bindClassToUniversalType( @NotNull Type type, @NotNull UniversalType universalType, boolean isDefault );
}
