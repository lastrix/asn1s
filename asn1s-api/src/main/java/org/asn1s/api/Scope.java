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

package org.asn1s.api;

import org.apache.commons.lang3.tuple.Pair;
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

public interface Scope
{
	Type getTypeOrDie();

	@NotNull
	Module getModule();

	Scope getParentScope();

	/**
	 * Get or create reference by name
	 *
	 * @param ref    type reference name
	 * @param module referenced module name
	 * @return Ref&lt;Type&gt;
	 */
	@NotNull
	Ref<Type> getTypeRef( @NotNull String ref, @Nullable String module );

	/**
	 * Get or create reference by name
	 *
	 * @param ref    value reference name
	 * @param module referenced module name
	 * @return Ref&lt;Value&gt;
	 */
	@NotNull
	Ref<Value> getValueRef( @NotNull String ref, @Nullable String module );

	@Nullable
	DefinedType resolveBuiltinTypeOrNull( @NotNull TypeName name );

	/**
	 * Resolves type in this module
	 *
	 * @param typeName type name
	 * @return actual {@link Type}
	 * @throws ResolutionException if type not found
	 */
	Type resolveType( @NotNull TypeName typeName ) throws ResolutionException;

	/**
	 * Resolves value in this module
	 *
	 * @param valueName value name
	 * @return actual {@link Value}
	 * @throws ResolutionException if value not found
	 */
	Value resolveValue( @NotNull ValueName valueName ) throws ResolutionException;

	void setValueLevel( Value value );

	Value getValueLevel();

	Scope templateInstanceScope( @NotNull Template<?> template, @NotNull List<Ref<?>> arguments );

	Scope templateScope( @NotNull Template<?> template );

	Scope typedScope( Type type );

	@Nullable
	<T> T getScopeOption( String key );

	void setScopeOption( String key, Object value );

	Pair<Type[], Value[]> getValueLevels();
}
