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

import java.lang.reflect.InvocationTargetException;
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

	Instantiator getInstantiator()
	{
		return instantiator;
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
		try
		{
			for( ClassFieldInfo fieldMapper : fieldMappers )
			{
				Value propertyValue = toAsn1Value( factory, value, fieldMapper );
				if( propertyValue != null )
					collection.addNamed( fieldMapper.getAsnName(), propertyValue );
			}
		} catch( Exception e )
		{
			throw new IllegalStateException( e );
		}
		return collection;
	}

	@Nullable
	private static Value toAsn1Value( @NotNull ValueFactory factory, @NotNull Object value, ClassFieldInfo fieldMapper ) throws InvocationTargetException, IllegalAccessException
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

		try
		{
			Iterable<NamedValue> namedValues = new LinkedList<>( value.toValueCollection().asNamedValueList() );
			Object o = createInstance( namedValues );
			for( NamedValue namedValue : namedValues )
			{
				ClassFieldInfo fieldMapper = getFieldMapper( namedValue.getName() );
				assert namedValue.getValueRef() != null;
				Object java = fieldMapper.getMapper().toJava( (Value)namedValue.getValueRef() );
				fieldMapper.setValue( o, java );
			}
			return o;
		} catch( Exception e )
		{
			throw new IllegalStateException( e );
		}
	}

	private Object createInstance( Iterable<NamedValue> namedValues )
	{
		if( !instantiator.hasParameters() )
			return instantiator.newInstance();

		String[] parameters = instantiator.getParameters();
		assert parameters != null;
		Object[] arguments = new Object[parameters.length];
		Iterator<NamedValue> iterator = namedValues.iterator();
		while( iterator.hasNext() )
		{
			NamedValue next = iterator.next();
			int index = findParameterIndex( parameters, next.getName() );
			if( index == -1 )
				continue;
			ClassFieldInfo fieldMapper = getFieldMapper( next.getName() );
			assert next.getValueRef() != null;
			Object java = fieldMapper.getMapper().toJava( (Value)next.getValueRef() );
			arguments[index] = java;
			iterator.remove();
		}

		assertNonOptionalParameters( parameters, arguments );
		return instantiator.newInstance( arguments );
	}

	private void assertNonOptionalParameters( String[] parameters, Object[] arguments )
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

	private int findParameterIndex( String[] parameters, String name )
	{
		String actualName = null;
		for( ClassFieldInfo fieldMapper : fieldMappers )
		{
			if( name.equals( fieldMapper.getAsnName() ) )
			{
				actualName = fieldMapper.getName();
				break;
			}
		}
		if( actualName == null )
			throw new IllegalArgumentException( "No property for name: " + name );

		int i = 0;
		for( String parameter : parameters )
		{
			if( actualName.equals( parameter ) )
				return i;
			i++;
		}
		return -1;
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
}
