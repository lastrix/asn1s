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

package org.asn1s.core.type.x681;

import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.type.AbstractType;
import org.asn1s.api.type.ClassFieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractFieldType extends AbstractType implements ClassFieldType
{
	AbstractFieldType( @NotNull String name, boolean optional )
	{
		this.name = name;
		this.optional = optional;
	}

	private final String name;
	private final boolean optional;

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.typedScope( this );
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Nullable
	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return null;
	}

	@Override
	public boolean isUnique()
	{
		return false;
	}

	@Override
	public boolean isOptional()
	{
		return optional;
	}


}
