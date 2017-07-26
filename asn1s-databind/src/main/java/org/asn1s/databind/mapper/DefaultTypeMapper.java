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

package org.asn1s.databind.mapper;

import org.asn1s.annotation.Property;
import org.asn1s.annotation.TypeAccessKind;
import org.asn1s.annotation.TypeAccessKind.AccessKind;
import org.asn1s.api.Ref;
import org.asn1s.api.UniversalType;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.*;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type.Family;
import org.asn1s.databind.Asn1Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultTypeMapper implements TypeMapper
{
	public DefaultTypeMapper( Asn1Context context )
	{
		this.context = context;
		initBuiltinTypes();
	}

	private final Asn1Context context;

	@NotNull
	@Override
	public MappedType mapType( @NotNull Type type )
	{
		return mapType( type, null );
	}

	/**
	 * Map type into ASN.1 Schema, resolve ASN.1 type name (if parameter asn1TypeName is null)
	 * using annotations or stub generation in form:
	 * T-Java-Bind-{typeNameWithDotsReplacedByMinus}
	 *
	 * @param type         the type to map
	 * @param asn1TypeName the asn.1 type name which should be used for mapping,
	 *                     may be an existing ASN.1 Type loaded into context module
	 * @return mapped type
	 */
	@NotNull
	private MappedType mapType( @NotNull Type type, @Nullable String asn1TypeName )
	{
		if( type instanceof Class<?> )
			return mapClass( (Class<?>)type, asn1TypeName );

		if( type instanceof ParameterizedType )
			return mapParameterizedType( (ParameterizedType)type, asn1TypeName );

		if( type instanceof GenericArrayType )
			return mapGenericArrayType( (GenericArrayType)type, asn1TypeName );

		if( type instanceof TypeVariable<?> )
			return mapTypeVariable( (TypeVariable<?>)type, asn1TypeName );

		if( type instanceof WildcardType )
			return mapWildcard( (WildcardType)type, asn1TypeName );

		// sanity check
		throw new IllegalArgumentException( "Unrecognized Type: " + type );
	}

	private MappedType mapClass( Class<?> type, @Nullable String asn1TypeName )
	{
		if( type.isAnnotation() || type.isSynthetic() )
			throw new IllegalArgumentException( "Annotations, Enums, Synthetics not supported, there is no way to implement it." );

		if( type.isAnonymousClass() )
			throw new IllegalArgumentException( "Unable to use anonymous classes." );

		if( asn1TypeName == null )
			asn1TypeName = MapperUtils.getAsn1TypeNameForClass( type );

		if( type.isArray() )
			return mapArrayType( type, asn1TypeName );

		if( type.isEnum() )
			return mapEnum( type, asn1TypeName );

		return mapClassImpl( type, asn1TypeName );
	}

	private static MappedType mapEnum( Class<?> type, @NotNull String asn1TypeName )
	{
		Object[] values = type.getEnumConstants();
		throw new UnsupportedOperationException();
	}

	private MappedType mapArrayType( Class<?> type, @NotNull String asn1TypeName )
	{
		asn1TypeName = ARRAY_BRACES_REPLACE.matcher( asn1TypeName ).replaceAll( Matcher.quoteReplacement( "-Array" ) );
		String canonicalName = type.getCanonicalName();
		MappedType result = context.getMappedType( canonicalName, asn1TypeName );
		if( result != null )
			return result;

		result = new SequenceOfMappedType( type );
		context.putMappedType( canonicalName, asn1TypeName, result );
		( (SequenceOfMappedType)result ).setComponentType( mapType( type.getComponentType() ) );
		bindSequenceOfTypeToAsn1Type( (SequenceOfMappedType)result, asn1TypeName );
		context.putDefinedTypeForClassName( type.getCanonicalName(), result.getAsnType() );
		return result;
	}

	private MappedType mapClassImpl( Class<?> type, @NotNull String asn1TypeName )
	{
		String canonicalName = type.getCanonicalName();
		MappedType result = context.getMappedType( canonicalName, asn1TypeName );
		if( result != null )
			return result;

		Constructor<?> constructor = MapperUtils.chooseConstructor( type );
		String[] parameters = MapperUtils.fetchConstructorParameterNames( constructor );

		SequenceMappedType mapped = new SequenceMappedType( type, constructor, parameters );
		context.putMappedType( canonicalName, asn1TypeName, mapped );
		mapped.setFields( mapClassFields( type, fetchFields( parameters ) ) );
		bindSequenceTypeToAsn1Type( mapped, asn1TypeName );
		context.putDefinedTypeForClassName( type.getCanonicalName(), mapped.getAsnType() );
		return mapped;
	}

	private static Collection<String> fetchFields( String[] parameters )
	{
		Collection<String> set = new HashSet<>();
		for( String parameter : parameters )
			if( parameter.startsWith( TypeMapper.MARKER_LOCAL_VARIABLE ) )
				set.add( parameter.substring( 1 ) );

		return set;
	}

	private MappedType mapParameterizedType( @NotNull ParameterizedType type, @Nullable String asn1TypeName )
	{
		if( Objects.equals( type.getRawType(), List.class ) )
			return mapListType( type, asn1TypeName );

		throw new UnsupportedOperationException();
	}

	private MappedType mapListType( @NotNull ParameterizedType type, @Nullable String asn1TypeName )
	{
		Type listItemType = type.getActualTypeArguments()[0];

		MappedType listItemMappedType = mapType( listItemType, null );
		if( asn1TypeName == null )
			asn1TypeName = "T-Java-List-Of-" + listItemMappedType.getAsnType().getName();

		MappedType mappedType = context.getMappedType( type.getTypeName(), asn1TypeName );
		if( mappedType != null )
			return mappedType;

		TypeFactory factory = context.getAsn1Factory().types();
		CollectionOfType collection = factory.collectionOf( Family.SequenceOf );
		collection.setComponent( TypeUtils.DUMMY, listItemMappedType.getAsnType() );
		DefinedType definedListType = factory.define( asn1TypeName, collection, null );
		SequenceOfMappedType result = new SequenceOfMappedType( type );
		result.setAsnType( definedListType );
		result.setComponentType( listItemMappedType );
		context.putMappedType( result.getTypeName(), definedListType.getName(), result );
		context.putDefinedTypeForClassName( type.getTypeName(), definedListType );
		return result;
	}

	private static MappedType mapGenericArrayType( GenericArrayType type, @Nullable String asn1TypeName )
	{
		throw new UnsupportedOperationException();
	}

	private static MappedType mapTypeVariable( TypeVariable<?> type, @Nullable String asn1TypeName )
	{
		throw new UnsupportedOperationException();
	}

	private static MappedType mapWildcard( WildcardType type, @Nullable String asn1TypeName )
	{
		throw new UnsupportedOperationException();
	}

	private void bindSequenceTypeToAsn1Type( SequenceMappedType type, String asn1TypeName )
	{
		DefinedType definedType = context.getContextModule().getTypeResolver().getType( asn1TypeName );
		if( definedType == null )
			generateAsn1Type( type, asn1TypeName );
		else
			validateAsn1Type( definedType, type );
	}

	private static void validateAsn1Type( DefinedType definedType, MappedType type )
	{
		if( Objects.equals( type.getAsnType(), definedType ) )
			return;

		throw new UnsupportedOperationException( "Not implemented yet." );
	}

	private void generateAsn1Type( SequenceMappedType type, String asn1TypeName )
	{
		TypeFactory factory = context.getAsn1Factory().types();
		CollectionType collection = factory.collection( Family.Sequence );
		for( MappedField field : type.getFields() )
			collection.addComponent( Kind.Primary, field.getPropertyName(), field.getType().getAsnType() ).setOptional( field.isOptional() );

		type.setAsnType( factory.define( asn1TypeName, collection, null ) );
	}

	private void bindSequenceOfTypeToAsn1Type( SequenceOfMappedType type, String asn1TypeName )
	{
		DefinedType definedType = context.getContextModule().getTypeResolver().getType( asn1TypeName );
		if( definedType == null )
			generateSequenceOfAsn1Type( type, asn1TypeName );
		else
			validateAsn1Type( definedType, type );
	}

	private void generateSequenceOfAsn1Type( SequenceOfMappedType type, String asn1TypeName )
	{
		TypeFactory factory = context.getAsn1Factory().types();
		CollectionOfType collection = factory.collectionOf( Family.SequenceOf );
		Ref<org.asn1s.api.type.Type> asnType = type.getComponentType().getAsnType();
		if( asnType == null )
			asnType = scope -> type.getComponentType().getAsnType();

		collection.setComponent( TypeUtils.DUMMY, asnType );
		type.setAsnType( factory.define( asn1TypeName, collection, null ) );
	}

	// *************************************** Builtin Types ******************************************************** //
	private void initBuiltinTypes()
	{
		bindClassToUniversalType( int.class, UniversalType.Integer, true );
		bindClassToUniversalType( Integer.class, UniversalType.Integer, true );
		bindClassToUniversalType( long.class, UniversalType.Integer, true );
		bindClassToUniversalType( Long.class, UniversalType.Integer, true );
		bindClassToUniversalType( String.class, UniversalType.UTF8String, true );
		bindClassToUniversalType( float.class, UniversalType.Real, true );
		bindClassToUniversalType( Float.class, UniversalType.Real, true );
		bindClassToUniversalType( double.class, UniversalType.Real, true );
		bindClassToUniversalType( Double.class, UniversalType.Real, true );
		bindClassToUniversalType( Instant.class, UniversalType.UTCTime, true );
		bindClassToUniversalType( Instant.class, UniversalType.GeneralizedTime, false );
	}

	/**
	 * Bind class for universal type, it will require special adapter to handle them.
	 *
	 * @param type          the type to be bound with universal type
	 * @param universalType the universal type to bound with type
	 * @param isDefault     set to true if binding must be default for type.
	 *                      Example Instant=&gt;UTCTime and Instant=&gt;GeneralizedTime, only one of them
	 *                      should have isDefault flag set to true. When true, the method will add
	 *                      another binding if user don't know how target ASN.1 is called
	 */
	private void bindClassToUniversalType( @NotNull Type type, @NotNull UniversalType universalType, boolean isDefault )
	{
		try
		{
			Ref<org.asn1s.api.type.Type> ref = universalType.ref();
			DefinedType resolve = (DefinedType)ref.resolve( context.getContextModule().createScope() );
			MappedType mappedType = new BuiltinMappedType( type, resolve );
			context.putMappedType( type.getTypeName(), resolve.getName(), mappedType );
			if( isDefault )
			{
				context.putMappedType( type.getTypeName(), "T-Java-Bind-" + type.getTypeName().replace( '.', '-' ), mappedType );
				context.putDefinedTypeForClassName( type.getTypeName(), resolve );
			}
		} catch( ResolutionException e )
		{
			throw new IllegalStateException( "Unable to resolve universal type: " + universalType.name(), e );
		}
	}

	// *************************************** Class Field mapping ************************************************** //

	private MappedField[] mapClassFields( Class<?> type, Collection<String> usedInConstructor )
	{
		AccessKind accessKind = AccessKind.Field;
		TypeAccessKind annotation = type.getAnnotation( TypeAccessKind.class );
		if( annotation != null )
			accessKind = annotation.value();

		List<MappedField> fields = new ArrayList<>();
		boolean requireSort = mapClassFieldsDirect( type, fields, usedInConstructor, accessKind )
				|| mapClassFieldsIndirect( type, fields, usedInConstructor );

		if( requireSort )
			fields.sort( Comparator.comparingInt( MappedField:: getIndex ) );

		assertNoDuplicates( fields );
		return fields.toArray( new MappedField[fields.size()] );
	}

	private static void assertNoDuplicates( Iterable<MappedField> fields )
	{
		Collection<String> collection = new HashSet<>();
		for( MappedField field : fields )
		{
			if( collection.contains( field.getPropertyName() ) )
				throw new IllegalStateException( "Duplicate field detected: " + field );
			collection.add( field.getPropertyName() );
		}
	}

	private boolean mapClassFieldsDirect( Class<?> type, Collection<MappedField> fields, Collection<String> usedInConstructor, AccessKind accessKind )
	{
		boolean requireSort = false;
		for( Field field : type.getDeclaredFields() )
			requireSort = mapClassFieldDirect( type, fields, usedInConstructor, accessKind, field ) || requireSort;
		return requireSort;
	}

	private boolean mapClassFieldDirect(
			Class<?> type,
			Collection<MappedField> fields,
			Collection<String> usedInConstructor,
			AccessKind accessKind,
			Field field )
	{
		Property property = field.getAnnotation( Property.class );
		if( property == null )
			return false;

		MappedType fieldType;

		if( field.getGenericType() == null )
		{
			fieldType = MapperUtils.LABEL_DEFAULT_VALUE.equals( property.typeName() )
					? mapType( field.getType() )
					: mapType( field.getType(), property.typeName() );
		}
		else
		{
			fieldType = MapperUtils.LABEL_DEFAULT_VALUE.equals( property.typeName() )
					? mapType( field.getGenericType() )
					: mapType( field.getGenericType(), property.typeName() );

		}

		String propertyName = field.getName();
		if( !MapperUtils.LABEL_DEFAULT_VALUE.equals( property.name() ) )
			propertyName = property.name();

		if( !field.isAccessible() )
			field.setAccessible( true );

		Method setter = null;
		Method getter = null;

		boolean isUsedInConstructor = usedInConstructor.contains( propertyName );
		if( accessKind == AccessKind.Method )
		{
			String base = Character.toUpperCase( field.getName().charAt( 0 ) ) + field.getName().substring( 1 );
			try
			{
				if( !isUsedInConstructor )
				{
					setter = type.getDeclaredMethod( "set" + base, field.getType() );
					if( !setter.isAccessible() )
						setter.setAccessible( true );
				}
				getter = type.getDeclaredMethod( "get" + base );

				if( !getter.isAccessible() )
					getter.setAccessible( true );
			} catch( NoSuchMethodException e )
			{
				throw new IllegalStateException( "Unable to find setter or getter for " + type.getCanonicalName() + "::" + field.getName(), e );
			}
		}

		long options = ( isUsedInConstructor ? BasicMappedField.OPTION_READONLY : 0L )
				| ( property.optional() ? BasicMappedField.OPTION_OPTIONAL : 0L );
		MappedField mappedField =
				new BasicMappedField( property.index(), propertyName, fieldType, options, field, setter, getter );

		registerField( fields, mappedField );
		return property.index() != -1;
	}

	private boolean mapClassFieldsIndirect( Class<?> type, Collection<MappedField> fields, Collection<String> usedInConstructor )
	{
		boolean requireSort = false;
		for( Method method : type.getDeclaredMethods() )
		{
			if( method.getName().startsWith( "set" ) && method.getParameterCount() == 1 || method.getName().startsWith( "get" ) )
				requireSort = mapClassFieldIndirect( type, fields, usedInConstructor, method ) || requireSort;
			else
			{
				Property property = method.getAnnotation( Property.class );
				if( property != null )
					throw new IllegalStateException( "Only getters and setters must be used for @Property annotation." );
			}
		}
		return requireSort;
	}

	private boolean mapClassFieldIndirect( Class<?> type, Collection<MappedField> fields, Collection<String> usedInConstructor, Method method )
	{
		Property property = method.getAnnotation( Property.class );
		if( property == null )
			return false;

		boolean isGetter = method.getName().startsWith( "get" );

		Class<?> fieldClass;
		Type fieldTypeToMap;
		if( isGetter )
		{
			fieldClass = method.getReturnType();
			fieldTypeToMap = method.getGenericReturnType() == null ? fieldClass : method.getGenericReturnType();
		}
		else
		{
			fieldClass = method.getParameterTypes()[0];
			fieldTypeToMap =
					method.getGenericParameterTypes() == null || method.getGenericParameterTypes()[0] == null
							? fieldClass
							: method.getGenericParameterTypes()[0];
		}

		MappedType fieldType = MapperUtils.LABEL_DEFAULT_VALUE.equals( property.typeName() )
				? mapType( fieldTypeToMap )
				: mapType( fieldTypeToMap, property.typeName() );

		String propertyName =
				MapperUtils.LABEL_DEFAULT_VALUE.equals( property.name() )
						? Character.toLowerCase( method.getName().charAt( 3 ) ) + method.getName().substring( 4 )
						: property.name();

		boolean isUsedInConstructor = usedInConstructor.contains( propertyName );
		Method setter = null;
		Method getter;

		try
		{
			String base = Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
			if( !isUsedInConstructor )
			{
				setter = isGetter ? type.getDeclaredMethod( "set" + base, fieldClass ) : method;
				if( !setter.isAccessible() )
					setter.setAccessible( true );
			}

			getter = isGetter ? method : type.getDeclaredMethod( "get" + base );
			if( !getter.isAccessible() )
				getter.setAccessible( true );
		} catch( NoSuchMethodException e )
		{
			throw new IllegalStateException( "Unable to find method for property " + type.getCanonicalName() + "::" + propertyName, e );
		}

		long options = ( isUsedInConstructor ? BasicMappedField.OPTION_READONLY : 0L )
				| ( property.optional() ? BasicMappedField.OPTION_OPTIONAL : 0L );

		MappedField mappedField =
				new BasicMappedField( property.index(), propertyName, fieldType, options, null, setter, getter );

		registerField( fields, mappedField );

		return property.index() != -1;
	}

	private static void registerField( Collection<MappedField> fields, MappedField mappedField )
	{
		for( MappedField field : fields )
			if( field.getPropertyName().equals( mappedField.getPropertyName() ) )
				throw new IllegalStateException( "Duplicate properties detected for type: " + mappedField.getType().getTypeName() + ". Property: " + mappedField.getPropertyName() );

		fields.add( mappedField );
	}

	private static final Pattern ARRAY_BRACES_REPLACE = Pattern.compile( "\\[]" );
}
