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
import org.asn1s.api.value.ByteArrayValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.*;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;

public class OpenTypeValueImpl implements OpenTypeValue
{
	public OpenTypeValueImpl( @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef )
	{
		this( typeRef, valueRef, false );
	}

	private OpenTypeValueImpl( @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef, boolean resolved )
	{
		this.typeRef = typeRef;
		this.valueRef = valueRef;
		this.resolved = resolved;
	}

	private final Ref<Type> typeRef;
	private final Ref<Value> valueRef;
	private final boolean resolved;

	@Override
	public Ref<Type> getType()
	{
		return typeRef;
	}

	@Override
	public Kind getReferencedKind()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).getKind();

		return Kind.Empty;
	}

	@Override
	public Ref<Value> getValueRef()
	{
		return valueRef;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o.getKind() == Kind.OpenType )
		{
			OpenTypeValue openTypeValue = o.toOpenTypeValue();

			if( resolved )
			{
				if( getReferencedKind() == openTypeValue.getReferencedKind() )
					//noinspection OverlyStrongTypeCast
					return ( (Value)valueRef ).compareTo( (Value)openTypeValue.getValueRef() );
			}
			else
				return getReferencedKind().compareTo( openTypeValue.getReferencedKind() );
		}

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

			return new OpenTypeValueImpl( type, type.optimize( scope, valueRef ), true );
		} catch( ValidationException e )
		{
			throw new ResolutionException( "Unable to validate value: " + valueRef, e );
		}
	}

	@Override
	public BooleanValue toBooleanValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toBooleanValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public IntegerValue toIntegerValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toIntegerValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public RealValue toRealValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toRealValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public NullValue toNullValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toNullValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public NamedValue toNamedValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toNamedValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public ValueCollection toValueCollection()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toValueCollection();

		throw new UnsupportedOperationException();
	}

	@Override
	public ByteArrayValue toByteArrayValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toByteArrayValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public StringValue toStringValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toStringValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public DateValue toDateValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toDateValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectValue toObjectValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toObjectValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectIdentifierValue toObjectIdentifierValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toObjectIdentifierValue();

		throw new UnsupportedOperationException();
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
			return typeRef + " : " + valueRef;
		return "[" + typeRef + " : " + valueRef + ']';
	}
}
