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
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FixedValueFieldType extends AbstractFieldType
{
	public FixedValueFieldType( @NotNull String name, @NotNull Ref<Type> fieldTypeRef, boolean unique, boolean optional, @Nullable Ref<Value> defaultValueRef )
	{
		super( name, optional );
		RefUtils.assertValueRef( name.substring( 1 ) );
		if( optional && defaultValueRef != null )
			throw new IllegalArgumentException( "'optional' is true and 'defaultValueRef' is not null at same time" );

		this.fieldTypeRef = fieldTypeRef;

		if( fieldTypeRef instanceof Type )
			fieldType = (Type)fieldTypeRef;

		this.unique = unique;
		this.defaultValueRef = defaultValueRef;
		if( defaultValueRef instanceof Value )
			defaultValue = (Value)defaultValueRef;
	}

	private Ref<Type> fieldTypeRef;
	private Type fieldType;
	private final boolean unique;
	private Ref<Value> defaultValueRef;
	private Value defaultValue;

	@Override
	public boolean hasDefault()
	{
		return defaultValueRef != null;
	}

	@Override
	public Ref<? extends Value> getDefault()
	{
		return defaultValue;
	}

	@Nullable
	@Override
	public Type getSibling()
	{
		return fieldType;
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

		if( defaultValueRef != null )
		{
			if( defaultValue == null )
				defaultValue = defaultValueRef.resolve( fieldType.getScope( scope ) );

			try
			{
				fieldType.accept( scope, defaultValue );
			} catch( ConstraintViolationException e )
			{
				throw new ValidationException( "Value is not accepted by field type: " + defaultValue, e );
			}
		}
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void acceptRef( @NotNull Scope scope, Ref<?> ref ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		Object rawObject = ref.resolve( scope );
		if( !( rawObject instanceof Value ) )
			throw new IllegalValueException( "Unable to accept ref: " + ref );

		Value.Kind kind = ( (Value)rawObject ).getKind();
		if( kind == Value.Kind.OBJECT )
		{
			ObjectValue value = ( (Value)rawObject ).toObjectValue();
			Ref<?> valueRef = value.getFields().get( getName() );
			Object resolve = valueRef.resolve( scope );
			assert resolve instanceof Value;
			fieldType.accept( scope, (Ref<Value>)resolve );
		}
		else
			fieldType.accept( scope, (Ref<Value>)rawObject );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		return fieldType.optimize( scope, valueRef );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Ref<? extends Value> optimizeRef( @NotNull Scope scope, Ref<?> ref ) throws ResolutionException, ValidationException
	{
		if( ref instanceof ValueNameRef || ref instanceof Value )
			return optimize( scope, (Ref<Value>)ref );

		throw new IllegalValueException( "Unable to optimize ref: " + ref );
	}

	@NotNull
	@Override
	public Type copy()
	{
		Ref<Type> fieldTypeCopy = Objects.equals( fieldTypeRef, fieldType ) ? fieldType.copy() : fieldTypeRef;
		return new FixedValueFieldType( getName(), fieldTypeCopy, unique, isOptional(), defaultValueRef );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return fieldType.getFamily();
	}

	@Override
	public boolean isUnique()
	{
		return unique;
	}

	@Override
	protected void onDispose()
	{
		fieldTypeRef = null;
		fieldType = null;
		defaultValue = null;
		defaultValueRef = null;
	}

	@Nullable
	@Override
	public NamedType getNamedType( @NotNull String name )
	{
		return fieldType.getNamedType( name );
	}

	@NotNull
	@Override
	public List<? extends NamedType> getNamedTypes()
	{
		return fieldType.getNamedTypes();
	}

	@Nullable
	@Override
	public NamedValue getNamedValue( @NotNull String name )
	{
		return fieldType.getNamedValue( name );
	}

	@NotNull
	@Override
	public Collection<NamedValue> getNamedValues()
	{
		return fieldType.getNamedValues();
	}

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return fieldType.getEncoding( instructions );
	}

	@Override
	public Kind getClassFieldKind()
	{
		return Kind.VALUE;
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
