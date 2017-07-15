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

import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeName;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueName;
import org.asn1s.api.value.x680.DefinedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Module extends Disposable
{

	void addImports( ModuleReference moduleReference, Iterable<String> symbols );

	Scope createScope();

	/**
	 * Return module resolver used by this module
	 *
	 * @return {@link ModuleResolver}
	 */
	@Nullable
	ModuleResolver getModuleResolver();

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
	TagMethod getTagMethod();

	void setTagMethod( TagMethod tagMethod );

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
	Collection<String> getExports();

	/**
	 * Returns collection of types present in this module
	 *
	 * @return {@link Collection} of {@link DefinedType}
	 */
	@NotNull
	Collection<DefinedType> getTypes();

	/**
	 * Return type declared in this module
	 *
	 * @param name type name
	 * @return {@link DefinedType} or null
	 */
	@Nullable
	DefinedType getType( String name );

	@NotNull
	Type resolveType( @NotNull TypeName typeName ) throws ResolutionException;

	/**
	 * Returns collection of values present in this module
	 *
	 * @return {@link Collection} of {@link DefinedValue}
	 */
	@NotNull
	Collection<DefinedValue> getValues();

	/**
	 * Returns value declared in this module
	 *
	 * @param name value name
	 * @return {@link DefinedValue} or null
	 */
	@Nullable
	DefinedValue getValue( String name );

	Value resolveValue( @NotNull ValueName valueName ) throws ResolutionException;

	/**
	 * Register disposable
	 *
	 * @param disposable a disposable
	 */
	void addDisposable( Disposable disposable );

	@NotNull
	Ref<Type> getTypeRef( @NotNull String ref, @Nullable String module );

	/**
	 * Registers type in this module
	 *
	 * @param type named type
	 */
	void addType( @NotNull DefinedType type );

	@NotNull
	Ref<Value> getValueRef( @NotNull String ref, @Nullable String module );

	/**
	 * Registers new value in this module
	 *
	 * @param value named value
	 */
	void addValue( @NotNull DefinedValue value );

	/**
	 * Allow all types to be extensible
	 *
	 * @param flag extensible flag
	 */
	void setAllTypesExtensible( boolean flag );

	void setExports( @Nullable Collection<String> exports );

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
