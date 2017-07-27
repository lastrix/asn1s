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

package org.asn1s.api.type;

import org.asn1s.api.*;
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Type extends Ref<Type>, Disposable, Validation, Scoped
{
	/**
	 * Returns true if current type is tagged. Exactly this instance.
	 *
	 * @return boolean
	 */
	default boolean isTagged()
	{
		return false;
	}

	/**
	 * Get type scope
	 *
	 * @param parentScope parent scope for new one
	 * @return new scope or parentScope
	 */
	@Override
	@NotNull
	default Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope;
	}

	/**
	 * Returns sibling type if any
	 *
	 * @return Type
	 * @throws IllegalStateException if type is not validated
	 */
	@Nullable
	default Type getSibling()
	{
		return null;
	}

	/**
	 * Accept value, use scope for resolution. You should chain scopes if this is required for some reason
	 *
	 * @param scope    resolution scope
	 * @param valueRef value reference
	 * @throws ValidationException if value is malformed for this type
	 * @throws ResolutionException if reference was not resolved, or data can not be acquired
	 * @see #optimize(Scope, Ref)
	 */
	default void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		throw new UnsupportedOperationException( "Not implemented or not allowed" );
	}

	/**
	 * Optimize value to type native value kind.
	 *
	 * @param scope    the resolution scope
	 * @param valueRef the value ref to optimize
	 * @return new object that is default for type
	 * @throws ValidationException if value is malformed for this type
	 * @throws ResolutionException if reference was not resolved, or data can not be acquired
	 * @see #accept(Scope, Ref)
	 */
	@NotNull
	default Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		throw new UnsupportedOperationException( "Not implemented or not allowed" );
	}

	/**
	 * Return component type by name
	 *
	 * @param name component name
	 * @return Component type based on parent - either class component, or collection component
	 */
	@Nullable
	default <T extends NamedType> T getNamedType( @NotNull String name )
	{
		return null;
	}

	/**
	 * Returns list of all named types
	 *
	 * @return list of named types
	 */
	@NotNull
	default <T extends NamedType> List<T> getNamedTypes()
	{
		return Collections.emptyList();
	}

	/**
	 * Return named value by name
	 *
	 * @param name value name
	 * @return named value
	 */
	@Nullable
	default NamedValue getNamedValue( @NotNull String name )
	{
		return null;
	}

	@NotNull
	default Collection<NamedValue> getNamedValues()
	{
		return Collections.emptyList();
	}

	/**
	 * Return type family
	 *
	 * @return TypeFamily
	 */
	@NotNull
	default Family getFamily()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Return type encodings
	 *
	 * @param instructions encoding instructions
	 * @return IEncoding
	 */
	default IEncoding getEncoding( EncodingInstructions instructions )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Copy this type. It's state must be None and validation required.
	 * This method is used for template handling {@link Template}
	 *
	 * @return Type
	 */
	@NotNull
	Type copy();

	String getNamespace();

	void setNamespace( String namespace );

	/**
	 * Convert type to value set if possible
	 *
	 * @return ValueSet
	 */
	default ElementSetSpecs asElementSetSpecs()
	{
		throw new UnsupportedOperationException( "There is no elementSetSpecs, did you checked by #hasElementSetSpecs()?" );
	}

	default boolean hasConstraint()
	{
		return false;
	}

	/**
	 * Returns true if this type has value set associated with it.
	 *
	 * @return true if may be used as value set
	 */
	default boolean hasElementSetSpecs()
	{
		return false;
	}

	default boolean isConstructedValue( Scope scope, Value value )
	{
		return false;
	}

	enum Family
	{
		UNKNOWN,
		BIT_STRING,
		BOOLEAN,
		CHARACTER_STRING,
		EMBEDDED_PDV,
		EXTERNAL,
		INTEGER,
		OID,
		OID_IRI,
		NULL,
		OCTET_STRING,
		OPEN_TYPE,
		OBJECT_CLASS,
		REAL,
		RELATIVE_OID,
		RELATIVE_OID_IRI,
		UTC_TIME,
		GENERALIZED_TIME,
		TIME,
		ENUMERATED,
		CHOICE,
		SEQUENCE,
		SEQUENCE_OF,
		SET,
		SET_OF,
		RESTRICTED_STRING,
		OBJECT_DESCRIPTOR
	}
}
