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

package org.asn1s.core.value.x680;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.AbstractNestingValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.OpenTypeValue;
import org.jetbrains.annotations.NotNull;

public class OpenTypeValueImpl extends AbstractNestingValue implements OpenTypeValue
{
	public OpenTypeValueImpl( @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef )
	{
		this( typeRef, valueRef, false );
	}

	private OpenTypeValueImpl( @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef, boolean resolved )
	{
		super( valueRef );
		this.typeRef = typeRef;
		this.resolved = resolved;
	}

	private final Ref<Type> typeRef;
	private final boolean resolved;

	@Override
	public Ref<Type> getType()
	{
		return typeRef;
	}

	@Override
	public Kind getReferencedKind()
	{
		if( getValueRef() instanceof Value )
			return ( (Value)getValueRef() ).getKind();

		return Kind.Empty;
	}

	@NotNull
	@Override
	public Ref<Value> getValueRef()
	{
		Ref<Value> valueRef = super.getValueRef();
		assert valueRef != null;
		return valueRef;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( resolved && o.getKind() == Kind.OpenType && getReferencedKind() == o.toOpenTypeValue().getReferencedKind() )
			return ( (Value)getValueRef() ).compareTo( (Value)o.toOpenTypeValue().getValueRef() );

		return getKind().compareTo( o.getKind() );
	}

	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		if( resolved )
			return this;

		Type type = typeRef.resolve( scope );
		try
		{
			if( !type.isValidated() )
				type.validate( scope );

			return new OpenTypeValueImpl( type, type.optimize( scope, getValueRef() ), true );
		} catch( ValidationException e )
		{
			throw new ResolutionException( "Unable to validate value: " + getValueRef(), e );
		}
	}

	@Override
	public OpenTypeValue toOpenTypeValue()
	{
		return this;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof OpenTypeValueImpl ) ) return false;

		OpenTypeValue openTypeValue = (OpenTypeValue)obj;

		//noinspection SimplifiableIfStatement
		if( !getType().equals( openTypeValue.getType() ) ) return false;
		return getValueRef().equals( openTypeValue.getValueRef() );
	}

	@Override
	public int hashCode()
	{
		int result = typeRef.hashCode();
		result = 31 * result + getValueRef().hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		if( resolved )
			return typeRef + " : " + getValueRef();
		return "[" + typeRef + " : " + getValueRef() + ']';
	}
}
