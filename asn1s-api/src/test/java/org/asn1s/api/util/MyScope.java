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

package org.asn1s.api.util;

import org.apache.commons.lang3.tuple.Pair;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeName;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class MyScope implements Scope
{
	@Override
	public Type getTypeOrDie()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Scope getParentScope()
	{
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Ref<Type> getTypeRef( @NotNull String ref, @Nullable String module )
	{
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Ref<Value> getValueRef( @NotNull String ref, @Nullable String module )
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public DefinedType resolveBuiltinTypeOrNull( @NotNull TypeName name )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Type resolveType( @NotNull TypeName typeName ) throws ResolutionException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Value resolveValue( @NotNull ValueName valueName ) throws ResolutionException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValueLevel( Value value )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Value getValueLevel()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Scope templateInstanceScope( @NotNull Template template, @NotNull List<Ref<?>> arguments )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Scope templateScope( @NotNull Template template, @Nullable Type type, @NotNull Module module )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Scope typedScope( Type type )
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public <T> T getScopeOption( String key )
	{
		return null;
	}

	@Override
	public void setScopeOption( String key, Object value )
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public Pair<Type[], Value[]> getValueLevels()
	{
		throw new UnsupportedOperationException();
	}
}
