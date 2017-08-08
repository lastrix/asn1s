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

package org.asn1s.obsolete.databind.binder;

import org.asn1s.api.Asn1Factory;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.ValueCollection;
import org.asn1s.obsolete.databind.Asn1Context;
import org.asn1s.obsolete.databind.mapper.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Asn1ValueBinderImpl implements Asn1ValueBinder
{
	public Asn1ValueBinderImpl( Asn1Context context )
	{
		this.context = context;
	}

	private final Asn1Context context;

	@Nullable
	@Override
	public Value toAsn1( @Nullable Object javaValue, @NotNull MappedType type )
	{
		if( javaValue == null )
			return null;

		if( type instanceof BuiltinMappedType )
			return builtinToAsn1( javaValue, type );

		if( type instanceof SequenceMappedType )
			return sequenceToAsn1( javaValue, (SequenceMappedType)type );

		if( type instanceof SequenceOfMappedType )
			return sequenceOfToAsn1( javaValue, (SequenceOfMappedType)type );

		throw new UnsupportedOperationException();
	}

	private Value builtinToAsn1( Object javaValue, MappedType type )
	{
		Type javaType = type.getJavaType();
		Asn1Factory factory = context.getAsn1Factory();
		ValueFactory valueFactory = factory.values();
		if( Objects.equals( javaType, int.class ) || Objects.equals( javaType, Integer.class ) )
			return valueFactory.integer( (Integer)javaValue );

		if( Objects.equals( javaType, long.class ) || Objects.equals( javaType, Long.class ) )
			return valueFactory.integer( (Long)javaValue );

		if( Objects.equals( javaType, BigInteger.class ) )
			return valueFactory.integer( (BigInteger)javaValue );

		if( Objects.equals( javaType, float.class ) || Objects.equals( javaType, Float.class ) )
			return valueFactory.real( (Float)javaValue );

		if( Objects.equals( javaType, double.class ) || Objects.equals( javaType, Double.class ) )
			return valueFactory.real( (Double)javaValue );

		if( Objects.equals( javaType, BigDecimal.class ) )
			return valueFactory.real( (BigDecimal)javaValue );

		if( Objects.equals( javaType, String.class ) )
			return valueFactory.cString( (String)javaValue );

		if( Objects.equals( javaType, Instant.class ) )
			return valueFactory.timeValue( (Instant)javaValue );

		throw new UnsupportedOperationException( "Unable to handle builtin type: " + type.getTypeName() );
	}

	@NotNull
	private Value sequenceToAsn1( Object javaValue, SequenceMappedType type )
	{
		ValueCollection collection = context.getAsn1Factory().values().collection( true );
		for( MappedField field : type.getFields() )
		{
			Value value = fieldToAsn1( field, javaValue );
			if( value == null )
			{
				if( !field.isOptional() )
					throw new IllegalStateException( "Required field is missing: " + field.getPropertyName() );
			}
			else
				collection.addNamed( field.getPropertyName(), value );
		}

		try
		{
			return type.getAsnType().optimize( type.getAsnType().createScope(), collection );
		} catch( ResolutionException | ValidationException e )
		{
			throw new IllegalStateException( "Unable to build value for: " + type.getTypeName(), e );
		}
	}

	@Nullable
	private Value fieldToAsn1( MappedField field, Object javaValue )
	{
		if( field instanceof BasicMappedField )
			return toAsn1( basicFieldToAsn1( (BasicMappedField)field, javaValue ), field.getType() );

		throw new UnsupportedOperationException();
	}

	private static Object basicFieldToAsn1( BasicMappedField field, Object javaValue )
	{
		try
		{
			if( field.getGetter() == null )
			{
				assert field.getField() != null;
				return field.getField().get( javaValue );
			}

			return field.getGetter().invoke( javaValue );
		} catch( IllegalAccessException | InvocationTargetException e )
		{
			throw new IllegalStateException( "Unable to fetch value for property: " + field.getPropertyName(), e );
		}
	}

	private Value sequenceOfToAsn1( Object javaValue, SequenceOfMappedType type )
	{
		Type javaType = type.getJavaType();
		if( javaType instanceof Class<?> )
		{
			assert ( (Class<?>)javaType ).isArray();
			return arrayToAsn1( javaValue, type );
		}

		if( javaType instanceof ParameterizedType )
		{
			ParameterizedType pJavaType = (ParameterizedType)javaType;
			if( Objects.equals( pJavaType.getRawType(), List.class ) )
				return listToAsn1( javaValue, type );
		}

		throw new UnsupportedOperationException();
	}

	private Value listToAsn1( Object javaValue, SequenceOfMappedType type )
	{
		ValueCollection collection = context.getAsn1Factory().values().collection( false );
		MappedType componentType = type.getComponentType();

		Iterable<?> list = (Iterable<?>)javaValue;
		for( Object o : list )
		{
			Value value = toAsn1( o, componentType );
			if( value == null )
				throw new IllegalStateException( "Unable to convert array to ASN.1 structure: " + type.getTypeName() );
			collection.add( value );
		}
		return collection;
	}

	@NotNull
	private Value arrayToAsn1( Object javaValue, SequenceOfMappedType type )
	{
		ValueCollection collection = context.getAsn1Factory().values().collection( false );
		MappedType componentType = type.getComponentType();
		int length = Array.getLength( javaValue );
		for( int i = 0; i < length; i++ )
		{
			Value value = toAsn1( Array.get( javaValue, i ), componentType );
			if( value == null )
				throw new IllegalStateException( "Unable to convert array to ASN.1 structure: " + type.getTypeName() );
			collection.add( value );
		}
		return collection;
	}
}
