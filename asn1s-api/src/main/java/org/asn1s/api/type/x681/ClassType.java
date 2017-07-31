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
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ClassType extends Type
{
	<T extends Ref<T>> void add( @NotNull ClassFieldType<T> field );

	/**
	 * Find component by name, extensions ignored
	 *
	 * @param name component name
	 * @return ComponentType or null
	 */
	@SuppressWarnings( "unchecked" )
	@Nullable
	@Override
	default <T extends NamedType> T getNamedType( @NotNull String name )
	{
		return (T)getField( name );
	}

	/**
	 * Returns list of components without extensions
	 *
	 * @return list of components
	 * @see #getFields()
	 */
	@SuppressWarnings( "unchecked" )
	@NotNull
	@Override
	default <T extends NamedType> List<T> getNamedTypes()
	{
		return (List<T>)getFields();
	}

	/**
	 * Find component by name
	 *
	 * @param name component name
	 * @return ComponentType or null
	 * @see #getNamedType(String)
	 */
	@Nullable
	<T extends Ref<T>> ClassFieldType<T> getField( @NotNull String name );

	/**
	 * Return list of components
	 *
	 * @return list of components
	 * @see #getNamedTypes()
	 */
	List<ClassFieldType<?>> getFields();

	void setSyntaxList( @NotNull List<String> syntaxList );

	boolean hasSyntaxList();

	List<String> getSyntaxList();

	boolean isAllFieldsOptional();
}
