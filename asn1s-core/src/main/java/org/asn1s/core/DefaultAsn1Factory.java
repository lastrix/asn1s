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

package org.asn1s.core;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.constraint.ConstraintFactory;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.type.TypeFactory;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.core.constraint.CoreConstraintFactory;
import org.asn1s.core.type.CoreTypeFactory;
import org.asn1s.core.value.CoreValueFactory;
import org.jetbrains.annotations.Nullable;

public final class DefaultAsn1Factory implements Asn1Factory
{
	public DefaultAsn1Factory()
	{
		this( null );
	}

	public DefaultAsn1Factory( @Nullable ModuleResolver resolver )
	{
		typeFactory = new CoreTypeFactory( resolver );
	}

	private final TypeFactory typeFactory;
	private final ValueFactory valueFactory = new CoreValueFactory();
	private final ConstraintFactory constraintFactory = new CoreConstraintFactory();

	@Override
	public TypeFactory types()
	{
		return typeFactory;
	}

	@Override
	public ValueFactory values()
	{
		return valueFactory;
	}

	@Override
	public ConstraintFactory constraints()
	{
		return constraintFactory;
	}
}
