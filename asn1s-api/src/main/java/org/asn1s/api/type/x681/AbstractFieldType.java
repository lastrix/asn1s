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

package org.asn1s.api.type.x681;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.type.AbstractNestingType;
import org.asn1s.api.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFieldType<T extends Ref<T>> extends AbstractNestingType implements ClassFieldType<T>
{
	protected AbstractFieldType( @NotNull String name, @Nullable Ref<Type> siblingRef, boolean optional )
	{
		super( siblingRef );
		this.name = name;
		this.optional = optional;
	}

	private final String name;
	private final boolean optional;
	private ClassType parent;

	@NotNull
	@Override
	public ClassType getParent()
	{
		return parent;
	}

	@Override
	public void setParent( @NotNull ClassType parent )
	{
		this.parent = parent;
	}

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

	@Override
	protected void onDispose()
	{
		parent = null;
		super.onDispose();
	}
}
