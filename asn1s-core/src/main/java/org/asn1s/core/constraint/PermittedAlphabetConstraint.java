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

package org.asn1s.core.constraint;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintType;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.StringValue;
import org.asn1s.core.constraint.template.PermittedAlphabetConstraintTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PermittedAlphabetConstraint implements Constraint
{
	public PermittedAlphabetConstraint( Type type, Constraint constraint )
	{
		this.type = type;
		this.constraint = constraint;
	}

	private final Type type;
	private final Constraint constraint;


	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		type.accept( scope, value );

		assert value.getKind() == Kind.CString;
		String s = value.toStringValue().asString();
		InternalStringValue temp = new InternalStringValue();
		try
		{
			int length = s.length();
			for( int i = 0; i < length; i++ )
			{
				temp.setValue( String.valueOf( s.charAt( i ) ) );
				constraint.check( scope, temp );
			}
		} catch( ConstraintViolationException e )
		{
			throw new ConstraintViolationException( "Value has illegal characters: " + s, e );
		}
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		PermittedAlphabetConstraintTemplate.assertType( type );
		return new PermittedAlphabetConstraint( type, constraint.copyForType( scope, type ) );
	}

	@Override
	public String toString()
	{
		return "FROM " + constraint;
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		if( !allowedTypes.contains( ConstraintType.PermittedAlphabet ) )
			throw new ValidationException( "'PermittedAlphabet' constraint is not allowed" );
	}

	private static final class InternalStringValue implements StringValue
	{
		private String value;

		public String getValue()
		{
			return value;
		}

		public void setValue( String value )
		{
			this.value = value;
		}

		@Override
		public String asString()
		{
			return value;
		}

		@Override
		public int length()
		{
			return value.length();
		}

		@Override
		public int compareTo( @NotNull Value o )
		{
			if( o.getKind() == Kind.CString )
				//noinspection CompareToUsesNonFinalVariable
				return value.compareTo( o.toStringValue().asString() );

			return getKind().compareTo( o.getKind() );
		}

		@Override
		public boolean equals( Object obj )
		{
			if( this == obj ) return true;
			if( !( obj instanceof InternalStringValue ) ) return false;

			InternalStringValue other = (InternalStringValue)obj;

			return getValue() != null ? getValue().equals( other.getValue() ) : other.getValue() == null;
		}

		@Override
		public int hashCode()
		{
			return getValue() != null ? getValue().hashCode() : 0;
		}

		@Override
		public String toString()
		{
			return String.valueOf( value );
		}
	}
}
