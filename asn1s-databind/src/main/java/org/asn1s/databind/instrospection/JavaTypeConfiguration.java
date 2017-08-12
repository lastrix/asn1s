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

package org.asn1s.databind.instrospection;

import org.asn1s.annotation.AnnotationUtils;
import org.asn1s.annotation.Asn1Type;
import org.asn1s.annotation.Asn1Type.Kind;
import org.asn1s.databind.TypeMapperUtils;

public final class JavaTypeConfiguration
{
	public JavaTypeConfiguration( Class<?> aClass )
	{
		Asn1Type type = aClass.getAnnotation( Asn1Type.class );
		asn1TypeName = AnnotationUtils.isDefault( type ) ? TypeMapperUtils.getDefaultAsnTypeName( aClass ) : type.name();
		kind = type.kind();
		extensible = type.extensible();
	}

	private final String asn1TypeName;
	private final Kind kind;
	private final boolean extensible;

	public String getAsn1TypeName()
	{
		return asn1TypeName;
	}

	public Kind getKind()
	{
		return kind;
	}

	public boolean isExtensible()
	{
		return extensible;
	}
}
