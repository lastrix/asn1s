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

package org.asn1s.databind.factory.userspace;

import org.asn1s.api.type.NamedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.databind.TypeMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

final class UserClassTypeMapper implements TypeMapper
{
	UserClassTypeMapper( Class<?> javaType, NamedType asnType )
	{
		this.javaType = javaType;
		this.asnType = asnType;
	}

	private final Class<?> javaType;
	private final NamedType asnType;
	private ClassFieldInfo[] fieldMappers;
	private Instantiator instantiator;

	@Override
	public Class<?> getJavaType()
	{
		return javaType;
	}

	@Override
	public NamedType getAsn1Type()
	{
		return asnType;
	}

	ClassFieldInfo[] getFieldMappers()
	{
		return fieldMappers.clone();
	}

	void setFieldMappers( ClassFieldInfo[] fieldMappers )
	{
		this.fieldMappers = fieldMappers.clone();
	}

	void setInstantiator( Instantiator instantiator )
	{
		this.instantiator = instantiator;
	}

	@NotNull
	@Override
	public Value toAsn1( @NotNull ValueFactory factory, @NotNull Object value )
	{
		if( !Objects.equals( javaType, value.getClass() ) )
			throw new IllegalArgumentException( "Unable to handle type: " + value.getClass() );
		ValueCollection collection = factory.collection( true );

		for( ClassFieldInfo fieldMapper : fieldMappers )
		{
			Value propertyValue = toAsn1Value( factory, value, fieldMapper );
			if( propertyValue != null )
				collection.addNamed( fieldMapper.getAsnName(), propertyValue );
		}
		return collection;
	}

	@Nullable
	private static Value toAsn1Value( @NotNull ValueFactory factory, @NotNull Object value, ClassFieldInfo fieldMapper )
	{
		Object propertyValue = fieldMapper.getValue( value );
		if( propertyValue != null )
			return fieldMapper.getMapper().toAsn1( factory, propertyValue );

		if( fieldMapper.isOptional() )
			return null;

		throw new IllegalStateException( "Unable to handle null value for property: " + fieldMapper.getName() );
	}

	@NotNull
	@Override
	public Object toJava( @NotNull Value value )
	{
		if( value.getKind() != Kind.NAMED_COLLECTION )
			throw new IllegalArgumentException( "Unable to handle value of kind: " + value.getKind() );

		Iterable<NamedValue> namedValues = new LinkedList<>( value.toValueCollection().asNamedValueList() );
		Object o = createInstance( namedValues );
		for( NamedValue namedValue : namedValues )
			toJavaProperty( o, namedValue );

		return o;
	}

	private void toJavaProperty( Object o, NamedValue namedValue )
	{
		ClassFieldInfo fieldMapper = getFieldMapper( namedValue.getName() );
		assert namedValue.getValueRef() != null;
		Object java = fieldMapper.getMapper().toJava( (Value)namedValue.getValueRef() );
		fieldMapper.setValue( o, java );
	}

	private Object createInstance( Iterable<NamedValue> namedValues )
	{
		if( !instantiator.hasParameters() )
			return instantiator.newInstance();

		String[] parameters = instantiator.getParameters();
		assert parameters != null;
		Object[] arguments = new InstantiatorParameters( parameters ).buildArguments( namedValues );
		return instantiator.newInstance( arguments );
	}


	private ClassFieldInfo getFieldMapper( String name )
	{
		for( ClassFieldInfo fieldMapper : fieldMappers )
		{
			if( fieldMapper.getAsnName().equals( name ) )
				return fieldMapper;
		}
		throw new IllegalArgumentException( "No fields for name: " + name );
	}

	private final class InstantiatorParameters
	{
		private InstantiatorParameters( @NotNull String[] parameters )
		{
			this.parameters = parameters;
			arguments = new Object[parameters.length];
		}

		private final String[] parameters;
		private final Object[] arguments;

		private Object[] buildArguments( Iterable<NamedValue> namedValues )
		{
			Iterator<NamedValue> iterator = namedValues.iterator();
			while( iterator.hasNext() )
				if( createInstanceProperty( iterator.next() ) )
					iterator.remove();

			assertNonOptionalParameters();
			return arguments;
		}

		private boolean createInstanceProperty( NamedValue namedValue )
		{
			int index = findParameterIndex( namedValue.getName() );
			if( index == -1 )
				return false;

			ClassFieldInfo fieldMapper = getFieldMapper( namedValue.getName() );
			assert namedValue.getValueRef() != null;
			Object java = fieldMapper.getMapper().toJava( (Value)namedValue.getValueRef() );
			arguments[index] = java;
			return true;
		}

		private int findParameterIndex( String name )
		{
			String actualName = findParameterActualName( name );

			int i = 0;
			for( String parameter : parameters )
			{
				if( actualName.equals( parameter ) )
					return i;
				i++;
			}
			return -1;
		}

		@NotNull
		private String findParameterActualName( String name )
		{
			for( ClassFieldInfo fieldMapper : fieldMappers )
				if( name.equals( fieldMapper.getAsnName() ) )
					return fieldMapper.getName();

			throw new IllegalArgumentException( "No property for name: " + name );
		}

		private void assertNonOptionalParameters()
		{
			int count = parameters.length;
			for( int i = 0; i < count; i++ )
			{
				if( arguments[i] != null )
					continue;

				ClassFieldInfo fieldMapper = getFieldMapper( parameters[i] );
				if( !fieldMapper.isOptional() )
					throw new IllegalStateException( "Non optional property may not be initialized with null value: " + parameters[i] );
			}
		}
	}
}
