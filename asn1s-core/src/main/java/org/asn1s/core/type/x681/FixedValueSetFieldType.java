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
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.core.type.ConstrainedType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FixedValueSetFieldType extends AbstractFieldType
{
	public FixedValueSetFieldType( @NotNull String name, @NotNull Ref<Type> fieldTypeRef, boolean optional, @Nullable ConstraintTemplate defaultElementSetSpecs )
	{
		super( name, optional );
		RefUtils.assertTypeRef( name.substring( 1 ) );
		if( optional && defaultElementSetSpecs != null )
			throw new IllegalArgumentException( "'optional' is true and 'defaultSetTypeRef' is not null at same time" );

		this.fieldTypeRef = fieldTypeRef;

		if( fieldTypeRef instanceof Type )
			fieldType = (Type)fieldTypeRef;

		this.defaultElementSetSpecs = defaultElementSetSpecs;
	}

	private Ref<Type> fieldTypeRef;
	private Type fieldType;
	private ConstraintTemplate defaultElementSetSpecs;
	private Type defaultFieldType;

	@Override
	public boolean hasDefault()
	{
		return defaultElementSetSpecs != null;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> Ref<T> getDefault()
	{
		return (Ref<T>)defaultFieldType;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );

		if( fieldType == null )
			fieldType = fieldTypeRef.resolve( scope );

		if( !( fieldType instanceof DefinedType ) )
			fieldType.setNamespace( getFullyQualifiedName() + '.' );

		fieldType.validate( scope );

		if( defaultElementSetSpecs != null )
		{
			defaultFieldType = new ConstrainedType( defaultElementSetSpecs, fieldType );
			defaultFieldType.validate( scope );
		}
	}

	@Override
	public <T> void acceptRef( @NotNull Scope scope, Ref<T> ref ) throws ResolutionException, IllegalValueException, ConstraintViolationException
	{
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, IllegalValueException, ConstraintViolationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T optimizeRef( @NotNull Scope scope, Ref<T> ref ) throws ResolutionException, ValidationException
	{
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Type copy()
	{
		Ref<Type> fieldTypeCopy = Objects.equals( fieldTypeRef, fieldType ) ? fieldType.copy() : fieldTypeRef;
		return new FixedValueSetFieldType( getName(), fieldTypeCopy, isOptional(), defaultElementSetSpecs );
	}

	@Override
	public Kind getClassFieldKind()
	{
		return Kind.VALUE_SET;
	}

	@Override
	protected void onDispose()
	{
		fieldTypeRef = null;
		fieldType = null;
		defaultElementSetSpecs = null;
		defaultFieldType = null;
	}
}
