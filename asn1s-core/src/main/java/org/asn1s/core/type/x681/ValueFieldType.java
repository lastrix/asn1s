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
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.x681.AbstractFieldTypeWithDefault;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;

public class ValueFieldType extends AbstractFieldTypeWithDefault<Value>
{
	public ValueFieldType( @NotNull String name, @NotNull Ref<Type> fieldTypeRef, boolean unique, boolean optional )
	{
		super( name, fieldTypeRef, optional );
		RefUtils.assertValueRef( name.substring( 1 ) );
		this.unique = unique;
	}

	private final boolean unique;

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		super.onValidate( scope );

		if( !hasSibling() )
			throw new ValidationException( "FixedValueSetFieldType must have sibling type" );

		if( hasDefault() )
			setDefault( getSibling().optimize( scope, getDefaultRef() ) );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void acceptRef( @NotNull Scope scope, Ref<Value> ref ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		Value value = ref.resolve( scope );

		Value.Kind kind = value.getKind();
		if( kind == Value.Kind.OBJECT )
		{
			ObjectValue object = value.toObjectValue();
			Ref<Value> valueRef = object.getField( getName() );
			assert valueRef != null;
			Value resolve = valueRef.resolve( scope );
			getSibling().accept( scope, resolve );
		}
		else
			getSibling().accept( scope, value );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Value optimizeRef( @NotNull Scope scope, Ref<Value> ref ) throws ResolutionException, ValidationException
	{
		if( ref instanceof ValueNameRef || ref instanceof Value )
			return optimize( scope, ref );

		throw new IllegalValueException( "Unable to optimize ref: " + ref );
	}

	@NotNull
	@Override
	public Type copy()
	{
		ValueFieldType type = new ValueFieldType( getName(), cloneSibling(), unique, isOptional() );
		type.setDefaultRef( getDefaultRef() );
		return type;
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return getSibling().getFamily();
	}

	@Override
	public boolean isUnique()
	{
		return unique;
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
