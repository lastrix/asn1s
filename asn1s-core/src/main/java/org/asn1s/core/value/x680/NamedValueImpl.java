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
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.ByteArrayValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x680.*;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NamedValueImpl implements NamedValue
{
	private static final String DUMMY = "#nameless";

	public static NamedValue nameless( Value value )
	{
		return new NamedValueImpl( DUMMY, value );
	}

	public NamedValueImpl( @NotNull String name, @Nullable Ref<Value> valueRef )
	{
		this( name, valueRef, false );
	}

	private NamedValueImpl( @NotNull String name, @Nullable Ref<Value> valueRef, boolean resolved )
	{
		this.name = name;
		this.valueRef = valueRef;
		this.resolved = valueRef == null;
	}

	private final boolean resolved;

	@NotNull
	private final String name;

	@Nullable
	private final Ref<Value> valueRef;

	@NotNull
	@Override
	public String getName()
	{
		return name;
	}

	@Nullable
	@Override
	public Ref<Value> getValueRef()
	{
		return valueRef;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		if( o instanceof NamedValue && ( (NamedValue)o ).getValueRef() instanceof Value )
		{
			NamedValue namedValue = (NamedValue)o;
			int result = getName().compareTo( namedValue.getName() );
			if( result != 0 )
				return result;

			o = (Value)namedValue.getValueRef();
		}

		if( valueRef instanceof Value )
			//noinspection OverlyStrongTypeCast
			return ( (Value)valueRef ).compareTo( o );

		throw new UnsupportedOperationException();
	}

	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		//noinspection ObjectEquality
		if( resolved )
			return this;

		assert valueRef != null;
		if( valueRef instanceof Value )
			return new NamedValueImpl( name, valueRef.resolve( scope ), true );

		Type type = scope.getTypeOrDie();
		NamedType namedType = type.getNamedType( name );
		if( namedType != null )
			scope = namedType.getScope( scope );
		return new NamedValueImpl( name, valueRef.resolve( scope ), true );
	}

	@Override
	public String toString()
	{
		return getName() + ' ' + valueRef;
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
		return this;
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
	public OpenTypeValue toOpenTypeValue()
	{
		if( valueRef instanceof Value )
			return ( (Value)valueRef ).toOpenTypeValue();

		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof NamedValueImpl ) ) return false;

		NamedValue namedValue = (NamedValue)obj;

		//noinspection SimplifiableIfStatement
		if( !getName().equals( namedValue.getName() ) ) return false;
		return getValueRef() != null ? getValueRef().equals( namedValue.getValueRef() ) : namedValue.getValueRef() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + ( getValueRef() != null ? getValueRef().hashCode() : 0 );
		return result;
	}
}
