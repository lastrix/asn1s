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

package org.asn1s.databind.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.annotation.AnnotationUtils;
import org.asn1s.annotation.Asn1Enumeration;
import org.asn1s.annotation.Asn1EnumerationItem;
import org.asn1s.api.Asn1Factory;
import org.asn1s.api.type.Enumerated;
import org.asn1s.api.type.Enumerated.ItemKind;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueFactory;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.databind.TypeMapper;
import org.asn1s.databind.TypeMapperContext;
import org.asn1s.databind.TypeMapperUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Converts enum values to ENUMERATED type, generates TypeMapper for handling I/O.
 */
public final class EnumTypeMapperFactory implements TypeMapperFactory
{
	private static final Log log = LogFactory.getLog( EnumTypeMapperFactory.class );

	public EnumTypeMapperFactory( TypeMapperContext context, Asn1Factory factory )
	{
		this.context = context;
		this.factory = factory;
	}

	private final TypeMapperContext context;
	private final Asn1Factory factory;

	@Override
	public int getPriority()
	{
		return -1;
	}

	@Override
	public boolean isSupportedFor( Type type )
	{
		return type instanceof Class<?> && ( (Class<?>)type ).isEnum();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public TypeMapper mapType( Type type )
	{
		if( !isSupportedFor( type ) )
			throw new IllegalArgumentException( "Only enum types allowed" );

		return mapEnum( (Class<Enum<?>>)type );
	}

	private TypeMapper mapEnum( Class<Enum<?>> type )
	{
		NamedType namedType = context.getNamedTypeForJavaName( type.getTypeName() );
		if( namedType != null )
			throw new IllegalArgumentException( "Unable to redefine type: " + type.getTypeName() );

		TypeMapper mapper = createEnumTypeMapper( type );

		// register defined type as default type mapping
		context.registerJavaClassForNamedType( type, mapper.getAsn1Type() );
		context.registerTypeMapper( mapper );
		return mapper;
	}

	@NotNull
	private TypeMapper createEnumTypeMapper( Class<Enum<?>> type )
	{
		EnumConverter converter = new EnumConverter( factory, type );
		converter.convert();
		TypeMapper mapper = new EnumTypeMapper( converter );
		if( log.isDebugEnabled() )
			log.debug( "Created new type mapper: " + mapper.getKey() );
		return mapper;
	}

	private static final class EnumConverter
	{
		private EnumConverter( Asn1Factory factory, Class<Enum<?>> enumClass )
		{
			this.factory = factory;
			this.enumClass = enumClass;
			type = factory.types().enumerated();
		}

		private final Asn1Factory factory;
		private final Class<Enum<?>> enumClass;
		private final Enumerated type;
		private final Map<Enum<?>, Field> enumFieldMap = new HashMap<>();

		private EnumEntry[] entries;
		private NamedType namedType;
		private boolean useAnnotationValue;
		private int i;

		void convert()
		{
			initializeConverter();
			for( Entry<Enum<?>, Field> entry : enumFieldMap.entrySet() )
				bindConstant( entry.getKey(), entry.getValue() );

			Asn1Enumeration annotation = enumClass.getAnnotation( Asn1Enumeration.class );
			namedType = factory.types().define( getAsn1TypeName( annotation ), type, null );
		}

		private void initializeConverter()
		{
			EnumConstantsCollector collector = new EnumConstantsCollector( enumClass );
			useAnnotationValue = collector.collectFields();
			enumFieldMap.putAll( collector.getEnumFieldMap() );
			entries = new EnumEntry[enumFieldMap.size()];
			i = 0;
		}

		@NotNull
		private String getAsn1TypeName( Asn1Enumeration annotation )
		{
			return annotation == null || AnnotationUtils.DEFAULT.equals( annotation.name() )
					? TypeMapperUtils.getDefaultAsnTypeName( enumClass )
					: annotation.name();
		}

		private void bindConstant( Enum<?> enumValue, Field field )
		{
			Asn1EnumerationItem annotation = field.getAnnotation( Asn1EnumerationItem.class );
			String name = getConstantName( annotation, enumValue );
			assertName( name );
			int value = useAnnotationValue ? annotation.value() : enumValue.ordinal();
			type.addItem( annotation.extension() ? ItemKind.EXTENSION : ItemKind.PRIMARY, name, factory.values().integer( value ) );
			entries[i] = new EnumEntry( enumValue, value, factory.values().namedInteger( name, value ) );
			i++;

			if( annotation.extension() )
				type.setExtensible( true );
		}

		private void assertName( String name )
		{
			for( int k = i - 1; k >= 0; k-- )
				if( entries[k].getNamedValue().getName().equals( name ) )
					throw new IllegalStateException( "Duplicate enum name: " + name );
		}

		private static String getConstantName( Asn1EnumerationItem annotation, Enum<?> enumValue )
		{
			if( AnnotationUtils.DEFAULT.equals( annotation.name() ) )
				return enumValue.name().toLowerCase().replace( '_', '-' );
			return annotation.name();
		}


		EnumEntry[] getEntries()
		{
			return entries.clone();
		}

		Class<Enum<?>> getEnumClass()
		{
			return enumClass;
		}

		NamedType getNamedType()
		{
			return namedType;
		}
	}

	private static final class EnumConstantsCollector
	{
		private EnumConstantsCollector( Class<Enum<?>> enumClass )
		{
			this.enumClass = enumClass;
			enumConstants = enumClass.getEnumConstants();
		}

		private final Class<Enum<?>> enumClass;
		private final Enum<?>[] enumConstants;
		private final Map<Enum<?>, Field> enumFieldMap = new HashMap<>();
		private boolean requireIndex;
		private boolean allHasIndex;

		private boolean collectFields()
		{
			enumFieldMap.clear();
			requireIndex = false;
			allHasIndex = true;
			for( Field field : enumClass.getDeclaredFields() )
				collectField( field );

			if( enumFieldMap.isEmpty() )
				throw new IllegalStateException( "No enum constants for type: " + enumClass.getTypeName() );
			return requireIndex;
		}

		private void collectField( Field field )
		{
			if( !field.isEnumConstant() )
				return;

			Asn1EnumerationItem item = field.getAnnotation( Asn1EnumerationItem.class );
			if( item == null )
				return;

			assertValidity( field, item );
			enumFieldMap.put( findEnumValue( field.getName() ), field );
		}

		private void assertValidity( Member field, Asn1EnumerationItem item )
		{
			if( item.value() == -1 )
			{
				if( requireIndex )
					throw new IllegalStateException( "Enum constant should have unique index: " + field.getName() );
				allHasIndex = false;
			}
			else
			{
				if( !allHasIndex )
					throw new IllegalStateException( "Some enum constants has no index in class: " + enumClass.getTypeName() );
				requireIndex = true;
			}
		}

		Map<Enum<?>, Field> getEnumFieldMap()
		{
			return Collections.unmodifiableMap( enumFieldMap );
		}

		private Enum<?> findEnumValue( String name )
		{
			for( Enum<?> constant : enumConstants )
			{
				if( constant.name().equals( name ) )
					return constant;
			}
			throw new IllegalArgumentException( "No enum constant: " + name );
		}

	}

	private static final class EnumEntry
	{
		private EnumEntry( Enum<?> enumValue, int value, NamedValue namedValue )
		{
			this.enumValue = enumValue;
			this.value = value;
			this.namedValue = namedValue;
		}

		private final Enum<?> enumValue;
		private final int value;
		private final NamedValue namedValue;

		Enum<?> getEnumValue()
		{
			return enumValue;
		}

		int getValue()
		{
			return value;
		}

		NamedValue getNamedValue()
		{
			return namedValue;
		}
	}

	private static final class EnumTypeMapper implements TypeMapper
	{
		private EnumTypeMapper( EnumConverter converter )
		{
			enumClass = converter.getEnumClass();
			asnType = converter.getNamedType();
			entries = converter.getEntries();
		}

		private final Class<Enum<?>> enumClass;
		private final NamedType asnType;
		private final EnumEntry[] entries;

		@Override
		public Type getJavaType()
		{
			return enumClass;
		}

		@Override
		public NamedType getAsn1Type()
		{
			return asnType;
		}

		@NotNull
		@Override
		public Value toAsn1( @NotNull ValueFactory factory, @NotNull Object value )
		{
			if( !enumClass.isAssignableFrom( value.getClass() ) )
				throw new IllegalArgumentException( "Unable to convert value: " + value );

			Enum<?> enumValue = (Enum<?>)value;
			for( EnumEntry entry : entries )
				if( entry.getEnumValue() == enumValue )
					return entry.getNamedValue();

			throw new IllegalArgumentException( "Unable to use enum constant: " + value );
		}

		@NotNull
		@Override
		public Object toJava( @NotNull Value value )
		{
			Kind kind = value.getKind();
			if( kind != Kind.NAME && kind != Kind.INTEGER )
				throw new IllegalArgumentException( "Unable to convert value of kind: " + kind );

			if( kind == Kind.NAME )
				return findConstantByNameOrDie( value.toNamedValue().getName() );

			return findByValue( value.toIntegerValue().asInt() );
		}

		private Object findByValue( int intValue )
		{
			for( EnumEntry entry : entries )
				if( entry.getValue() == intValue )
					return entry.getEnumValue();

			throw new IllegalArgumentException( "No enum constant for value: " + intValue );
		}

		@NotNull
		private Object findConstantByNameOrDie( @NotNull String name )
		{
			for( EnumEntry entry : entries )
				if( entry.getNamedValue().getName().equals( name ) )
					return entry.getEnumValue();

			throw new IllegalArgumentException( "No enum constant: " + name );
		}
	}
}
