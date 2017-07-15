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
import org.asn1s.api.State;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.Module;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.ByteArrayValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.*;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;

public class DefinedValueImpl implements DefinedValue
{
	public DefinedValueImpl( @NotNull Module module, @NotNull String name, @NotNull Ref<Type> typeRef, @NotNull Ref<Value> valueRef )
	{
		this.module = module;
		this.name = name;
		this.typeRef = typeRef;
		this.valueRef = valueRef;
	}

	private Module module;
	private final String name;
	private Ref<Type> typeRef;
	private Ref<Value> valueRef;

	private Type type;
	private Value value;
	private State state = State.None;

	Module getModule()
	{
		return module;
	}

	@NotNull
	@Override
	public Scope getScope( @NotNull Scope parentScope )
	{
		return parentScope.typedScope( type );
	}

	@NotNull
	@Override
	public String getName()
	{
		return name;
	}

	Ref<Value> getValueRef()
	{
		return valueRef;
	}

	Ref<Type> getTypeRef()
	{
		return typeRef;
	}

	@Override
	public Type getType()
	{
		return type;
	}

	void setType( Type type )
	{
		this.type = type;
	}

	@Override
	public Value getValue()
	{
		return value;
	}

	void setValue( Value value )
	{
		this.value = value;
	}

	@Override
	public Ref<Value> toRef()
	{
		return new ValueNameRef( getName(), module.getModuleName() );
	}

	@Override
	public State getState()
	{
		return state;
	}

	@NotNull
	@Override
	public Kind getKind()
	{
		if( value == null )
			return Kind.Null;

		return value.getKind();
	}

	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		if( !isValidated() )
			try
			{
				validate( scope );
			} catch( ValidationException e )
			{
				throw new ResolutionException( "Unable to validate", e );
			}

		return value;
	}

	@Override
	public void validate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		if( state != State.None )
			return;

		state = State.Validating;

		try
		{
			onValidate( scope );
			state = State.Done;
		} catch( Throwable e )
		{
			state = State.Failed;
			//noinspection ProhibitedExceptionThrown
			throw e;
		}
	}

	@Override
	public void dispose()
	{
		state = State.Disposed;
		module = null;
		typeRef = null;
		valueRef = null;
		type = null;
		value = null;
	}

	protected void onValidate( @SuppressWarnings( "ParameterCanBeLocal" ) Scope scope ) throws ValidationException, ResolutionException
	{
		scope = module.createScope();
		type = typeRef.resolve( scope );
		type.validate( scope );
		scope = getScope( scope );
		value = type.optimize( scope, valueRef.resolve( scope ) );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof DefinedValueImpl ) ) return false;

		DefinedValueImpl definedValue = (DefinedValueImpl)obj;

		if( !getName().equals( definedValue.getName() ) ) return false;
		//noinspection SimplifiableIfStatement
		if( !getTypeRef().equals( definedValue.getTypeRef() ) ) return false;
		return getValueRef().equals( definedValue.getValueRef() );
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + getTypeRef().hashCode();
		result = 31 * result + getValueRef().hashCode();
		return result;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		assert isValidated();
		//noinspection CompareToUsesNonFinalVariable
		return value.compareTo( o );
	}

	@Override
	public BooleanValue toBooleanValue()
	{
		return value.toBooleanValue();
	}

	@Override
	public IntegerValue toIntegerValue()
	{
		return value.toIntegerValue();
	}

	@Override
	public RealValue toRealValue()
	{
		return value.toRealValue();
	}

	@Override
	public NullValue toNullValue()
	{
		return value.toNullValue();
	}

	@Override
	public NamedValue toNamedValue()
	{
		return value.toNamedValue();
	}

	@Override
	public ValueCollection toValueCollection()
	{
		return value.toValueCollection();
	}

	@Override
	public StringValue toStringValue()
	{
		return value.toStringValue();
	}

	@Override
	public ByteArrayValue toByteArrayValue()
	{
		return value.toByteArrayValue();
	}

	@Override
	public DateValue toDateValue()
	{
		return value.toDateValue();
	}

	@Override
	public ObjectValue toObjectValue()
	{
		return value.toObjectValue();
	}

	@Override
	public ObjectIdentifierValue toObjectIdentifierValue()
	{
		return value.toObjectIdentifierValue();
	}

	@Override
	public OpenTypeValue toOpenTypeValue()
	{
		return value.toOpenTypeValue();
	}

	@Override
	public String toString()
	{
		return getName() + " ::= " + value;
	}
}
