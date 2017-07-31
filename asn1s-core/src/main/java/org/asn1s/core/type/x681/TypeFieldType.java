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

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.x681.AbstractFieldTypeWithDefault;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class TypeFieldType extends AbstractFieldTypeWithDefault<Type>
{
	public TypeFieldType( @NotNull String name, boolean optional, @Nullable Ref<Type> defaultTypeRef )
	{
		super( name, null, optional );
		RefUtils.assertTypeRef( name.substring( 1 ) );
		if( optional && defaultTypeRef != null )
			throw new IllegalArgumentException( "'optional' is true and 'defaultTypeRef' is not null at same time." );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );

		if( hasDefault() )
		{
			setDefault( getDefaultRef().resolve( scope ) );
			//noinspection ConstantConditions
			getDefault().validate( scope );
		}
	}

	@NotNull
	@Override
	public Type copy()
	{
		return new TypeFieldType( getName(), isOptional(), getDefaultRef() );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		// nothing to do
	}

	@Override
	public void acceptRef( @NotNull Scope scope, Ref<Type> ref ) throws ResolutionException, IllegalValueException, ConstraintViolationException
	{
		ref.resolve( scope );
		// all types acceptable, but resolution is viable.
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, IllegalValueException, ConstraintViolationException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Value.Kind.OPEN_TYPE )
			return value;

		throw new IllegalValueException( "Unable to accept value of kind: " + value.getKind() + ". Only OpenType values allowed." );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Type optimizeRef( @NotNull Scope scope, Ref<Type> ref ) throws ResolutionException, ValidationException
	{
		return ref.resolve( scope );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.OPEN_TYPE;
	}

	@Override
	public Kind getClassFieldKind()
	{
		return Kind.TYPE;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		return null;
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return Collections.emptyList();
	}

	@Override
	public boolean isConstructedValue( Scope scope, Value value )
	{
		return false;
	}

	@Nullable
	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return null;
	}
}
