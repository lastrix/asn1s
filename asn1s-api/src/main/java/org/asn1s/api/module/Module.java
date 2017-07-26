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

package org.asn1s.api.module;

import org.asn1s.api.Disposable;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Module extends Disposable
{
	Scope createScope();

	/**
	 * Return module with core types, like INTEGER, BOOLEAN, REAL, etc.
	 *
	 * @return Module
	 */
	Module getCoreModule();

	/**
	 * Return module resolver used by this module
	 *
	 * @return {@link ModuleResolver}
	 */
	@Nullable
	ModuleResolver getModuleResolver();

	@NotNull
	TypeResolver getTypeResolver();

	@NotNull
	ValueResolver getValueResolver();

	/**
	 * Return module reference
	 *
	 * @return {@link ModuleReference}
	 */
	@NotNull
	ModuleReference getModuleReference();

	/**
	 * Returns module name
	 *
	 * @return string
	 */
	@NotNull
	String getModuleName();

	/**
	 * Returns module tag method
	 *
	 * @return TagMethod
	 */
	@NotNull
	default TagMethod getTagMethod()
	{
		return TagMethod.UNKNOWN;
	}

	default void setTagMethod( TagMethod tagMethod )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns true if all types are extensible
	 *
	 * @return boolean
	 */
	boolean isAllTypesExtensible();

	/**
	 * Returns true of there are any symbols in export list,
	 * this method returns false when modules exports all symbols
	 *
	 * @return boolean
	 */
	boolean hasExports();

	/**
	 * Returns true if module exports all symbols
	 *
	 * @return boolean
	 */
	boolean isExportAll();

	/**
	 * Returns collection of exported symbol's names
	 *
	 * @return names
	 */
	@Nullable
	default Collection<String> getExports()
	{
		return null;
	}

	/**
	 * Register disposable
	 *
	 * @param disposable a disposable
	 */
	void addDisposable( Disposable disposable );

	/**
	 * Allow all types to be extensible
	 *
	 * @param flag extensible flag
	 */
	default void setAllTypesExtensible( boolean flag )
	{
		throw new UnsupportedOperationException();
	}

	default void setExports( @Nullable Collection<String> exports )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Validates this module and all its types, values.
	 *
	 * @throws ValidationException if module itself or any sibling object can not be validated
	 * @throws ResolutionException if references can not be resolved
	 */
	default void validate() throws ValidationException, ResolutionException
	{
		validate( true, true );
	}

	/**
	 * Validates this module and all its types, values.
	 *
	 * @param types  if types should be validated
	 * @param values if values should be validated
	 * @throws ValidationException if module itself or any sibling object can not be validated
	 * @throws ResolutionException if references can not be resolved
	 */
	void validate( boolean types, boolean values ) throws ValidationException, ResolutionException;
}
