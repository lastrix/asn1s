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

package org.asn1s.databind.binder;

import org.apache.commons.lang3.ArrayUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.databind.Asn1Context;
import org.asn1s.databind.mapper.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class JavaValueBinderImpl implements JavaValueBinder
{
	public JavaValueBinderImpl( Asn1Context context )
	{
		this.context = context;
	}

	private final Asn1Context context;

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T toJava( Value asn1Value, MappedType type )
	{
		if( type instanceof BuiltinMappedType )
			return (T)builtinToJava( asn1Value, type );

		if( type instanceof SequenceMappedType )
			return sequenceToJava( asn1Value, (SequenceMappedType)type );

		if( type instanceof SequenceOfMappedType )
			return (T)sequenceOfToJava( asn1Value, (SequenceOfMappedType)type );

		throw new UnsupportedOperationException();
	}

	private static Object builtinToJava( Value asn1Value, MappedType type )
	{
		Type javaType = type.getJavaType();
		if( Objects.equals( javaType, int.class ) || Objects.equals( javaType, Integer.class ) )
			return asn1Value.toIntegerValue().asInt();

		if( Objects.equals( javaType, long.class ) || Objects.equals( javaType, Long.class ) )
			return asn1Value.toIntegerValue().asLong();

		if( Objects.equals( javaType, BigInteger.class ) )
			return asn1Value.toIntegerValue().asBigInteger();

		if( Objects.equals( javaType, float.class ) || Objects.equals( javaType, Float.class ) )
			return asn1Value.toRealValue().asFloat();

		if( Objects.equals( javaType, double.class ) || Objects.equals( javaType, Double.class ) )
			return asn1Value.toRealValue().asDouble();

		if( Objects.equals( javaType, BigDecimal.class ) )
			return asn1Value.toRealValue().asBigDecimal();

		if( Objects.equals( javaType, String.class ) )
			return asn1Value.toStringValue().asString();

		if( Objects.equals( javaType, Instant.class ) )
			return asn1Value.toDateValue().asInstant();

		throw new UnsupportedOperationException( "Unable to handle builtin type: " + type.getTypeName() );
	}

	private <T> T sequenceToJava( Value asn1Value, SequenceMappedType type )
	{
		assert asn1Value.getKind() == Kind.NamedCollection;
		ValueCollection collection = asn1Value.toValueCollection();
		T result = BinderUtils.newInstance( type, sequenceMappedTypeParameters( type, collection ) );
		for( MappedField field : type.getFields() )
			if( !field.isReadonly() )
				setSequenceField( type, collection, result, field );

		return result;
	}

	private <T> void setSequenceField( SequenceMappedType type, ValueCollection collection, T result, MappedField field )
	{
		if( field instanceof BasicMappedField )
			setSequenceBasicField( type, collection, result, (BasicMappedField)field );
		else
			throw new UnsupportedOperationException();
	}

	private <T> void setSequenceBasicField( SequenceMappedType type, ValueCollection collection, T result, BasicMappedField field )
	{
		try
		{
			Object propertyValue = propertyToJava( field.getPropertyName(), type, collection );
			if( propertyValue != null )
			{
				if( field.getSetter() == null )
				{
					assert field.getField() != null;
					field.getField().set( result, propertyValue );
				}
				else
					field.getSetter().invoke( result, propertyValue );
			}
		} catch( IllegalAccessException | InvocationTargetException e )
		{
			throw new IllegalStateException( "Unable to set field: " + field.getPropertyName(), e );
		}
	}

	@Nullable
	private Object[] sequenceMappedTypeParameters( SequenceMappedType type, ValueCollection collection )
	{
		String[] constructorParameters = type.getConstructorParameters();
		if( ArrayUtils.isEmpty( constructorParameters ) )
			return null;

		Object[] parameters = new Object[constructorParameters.length];
		int i = 0;
		for( String parameter : constructorParameters )
		{
			if( parameter.startsWith( TypeMapper.MARKER_GLOBAL_VARIABLE ) )
				parameters[i] = context.getGlobalParameter( parameter.substring( 1 ) );
			else
				parameters[i] = propertyToJava( parameter.substring( 1 ), type, collection );
			i++;
		}
		return parameters;
	}

	@Nullable
	private Object propertyToJava( String property, SequenceMappedType type, ValueCollection collection )
	{
		NamedValue value = collection.getNamedValue( property );
		MappedField field = type.getFieldOrDie( property );
		if( value == null )
		{
			if( !field.isOptional() )
				throw new IllegalStateException( "Missing property value: " + field.getPropertyName() );
			return null;
		}

		assert value.getValueRef() instanceof Value;
		return toJava( (Value)value.getValueRef(), field.getType() );
	}

	private Object sequenceOfToJava( Value asn1Value, SequenceOfMappedType type )
	{
		assert asn1Value.getKind() == Kind.Collection || asn1Value.getKind() == Kind.NamedCollection;
		ValueCollection collection = asn1Value.toValueCollection();
		Type javaType = type.getJavaType();
		if( javaType instanceof Class<?> )
		{
			assert ( (Class<?>)javaType ).isArray();
			return arrayToJava( type, collection );
		}

		if( javaType instanceof ParameterizedType )
		{
			ParameterizedType pJavaType = (ParameterizedType)javaType;
			if( Objects.equals( pJavaType.getRawType(), List.class ) )
				return listToJava( type, collection );
		}
		throw new UnsupportedOperationException();
	}

	private Object arrayToJava( SequenceOfMappedType type, ValueCollection collection )
	{
		Object array = Array.newInstance( (Class<?>)type.getComponentType().getJavaType(), collection.size() );
		MappedType componentType = type.getComponentType();
		int i = 0;
		for( Ref<Value> ref : collection.asValueList() )
		{
			Object javaValue = toJava( unwrapRef( ref ), componentType );
			Array.set( array, i, javaValue );
			i++;
		}
		return array;
	}

	private Object listToJava( SequenceOfMappedType type, ValueCollection collection )
	{
		Collection<Object> result = new ArrayList<>( collection.size() );
		MappedType componentType = type.getComponentType();
		for( Ref<Value> ref : collection.asValueList() )
			result.add( toJava( unwrapRef( ref ), componentType ) );

		return result;
	}

	@NotNull
	private static Value unwrapRef( Ref<Value> ref )
	{
		if( !( ref instanceof Value ) )
			throw new IllegalStateException( "Unable to use references: " + ref );

		Value value = (Value)ref;
		if( value.getKind() == Kind.Name )
		{
			ref = value.toNamedValue().getValueRef();
			if( !( ref instanceof Value ) )
				throw new IllegalStateException( "Unable to use references: " + ref );

			value = (Value)ref;
		}
		return value;
	}
}
