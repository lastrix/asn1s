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

package org.asn1s.core.type.x680.id;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.ConstraintViolationException;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.NullValue;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.value.x680.IntegerValueLong;
import org.asn1s.core.value.x680.NamedValueImpl;
import org.asn1s.core.value.x680.NonOptimizedOIDValueImpl;
import org.asn1s.core.value.x680.OptimizedOIDValueImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * X.680, p 32.1
 *
 * @author lastrix
 * @version 1.0
 */
public final class ObjectIdentifierType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( ObjectIdentifierType.class );

	public ObjectIdentifierType()
	{
		setEncoding( TagEncoding.universal( UniversalType.OBJECT_IDENTIFIER ) );
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind == Kind.OID )
			assertValueFormCollection( scope, value );
		else
			throw new IllegalValueException( "Not an object identifier value: " + valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		Value value = valueRef.resolve( scope );
		if( value.getKind() == Kind.OID )
		{
			if( value instanceof NonOptimizedOIDValueImpl )
				return optimizeValue( scope, (NonOptimizedOIDValueImpl)value );
			return value;
		}
		throw new IllegalValueException( "Unable to optimize value of kind: " + value.getKind() );
	}

	private static Value optimizeValue( Scope scope, NonOptimizedOIDValueImpl nonOptimized ) throws ResolutionException, ValidationException
	{
		List<NamedValue> optimized = new ArrayList<>( nonOptimized.getOidRefs().size() );

		for( Ref<Value> ref : nonOptimized.getOidRefs() )
			optimizeValueImpl( ref.resolve( scope ), optimized );

		return new OptimizedOIDValueImpl( optimized );
	}

	private static void optimizeValueImpl( Value value, List<NamedValue> optimized ) throws IllegalValueException
	{
		Kind kind = value.getKind();
		if( kind == Kind.NAME )
		{
			NamedValue namedValue = value.toNamedValue();
			if( namedValue.getReferenceKind() == Kind.INTEGER )
				optimized.add( namedValue );
			else if( namedValue.getReferenceKind() == Kind.EMPTY )
			{
				long number = resolveName( namedValue.getName(), optimized.size(), optimized );
				optimized.add( new NamedValueImpl( namedValue.getName(), new IntegerValueLong( number ) ) );
			}
			else
				throw new IllegalValueException( "Unable to use value as oid: " + value );
		}
		else if( kind == Kind.INTEGER )
			optimized.add( NamedValueImpl.nameless( value ) );
		else if( kind == Kind.OID )
			optimized.addAll( value.toObjectIdentifierValue().asNamedValueList() );
		else
			throw new IllegalValueException( "Unable to use value as oid: " + value );
	}

	@Override
	public String toString()
	{
		return UniversalType.OBJECT_IDENTIFIER.typeName().toString();
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.OID;
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type!" );
		return new ObjectIdentifierType();
	}

	private static void assertValueFormCollection( Scope scope, Value value ) throws IllegalValueException, ConstraintViolationException
	{
		List<NamedValue> values = new ArrayList<>();
		if( value.getKind() == Kind.NAMED_COLLECTION )
			for( NamedValue namedValue : value.toValueCollection().asNamedValueList() )
				values.add( validateValueReference( scope, namedValue ) );
		else if( value.getKind() == Kind.OID )
			for( NamedValue namedValue : value.toObjectIdentifierValue().asNamedValueList() )
				values.add( validateValueReference( scope, namedValue ) );
		else
			throw new IllegalStateException( "Unable to assert value of kind: " + value.getKind() );

		finalizeForms( values );
	}

	private static void finalizeForms( List<NamedValue> list )
	{
		int idx = -1;
		List<NamedValue> collection = new ArrayList<>();
		for( NamedValue value : list )
		{
			idx++;
			if( value.getKind() == Kind.NULL )
				collection.add( new NamedValueImpl( value.getName(), new IntegerValueLong( resolveName( value.getName(), idx, collection ) ) ) );
			else
				collection.add( value );
		}
	}

	private static long resolveName( String name, int idx, List<NamedValue> collection )
	{
		if( idx == 0 )
		{
			switch( name.toLowerCase() )
			{
				case "ccitt":
				case "itu-t":
				case "itu-r":
					return 0;

				case "iec":
				case "iso":
					return 1;

				case "joint-iso-itu-t":
				case "joint-iso-ccitt":
					return 2;

				default:
					throw new IllegalArgumentException( "Unable to resolve name: " + name );
			}
		}


		if( idx == 1 )
		{
			long prev = collection.get( 0 ).toIntegerValue().asLong();
			if( prev == 0 )
			{
				switch( name.toLowerCase() )
				{
					case "recommendation":
						return 0;

					case "question":
						return 1;

					case "administration":
						return 2;

					case "network-operator":
						return 3;

					case "identified-organization":
						return 4;

					case "r-recommendation":
						return 5;

					case "data":
						return 9;

					default:
						throw new IllegalArgumentException( "Unable to resolve name: " + name );
				}
			}
			else if( prev == 1 )
			{
				switch( name.toLowerCase() )
				{
					case "standard":
						return 0;

					case "registration-authority":
						return 1;

					case "member-body":
						return 2;

					case "identified-organization":
						return 3;

					default:
						throw new IllegalArgumentException( "unable to resolve name: " + name );
				}
			}
			else if( prev == 2 )
			{
				switch( name.toLowerCase() )
				{
					case "module":
						return 1;

					case "document-types":
						return 2;

					case "asn-1":
						return 3;

					case "international-md":
						return 5;

					case "international-organization":
						return 6;

					default:
						throw new IllegalArgumentException( "unable to resolve name: " + name );
				}
			}
		}

		return 0;
	}

	private static NamedValue validateValueReference( Scope scope, NamedValue namedValue ) throws IllegalValueException, ConstraintViolationException
	{
		try
		{
			namedValue = namedValue.resolve( scope ).toNamedValue();
		} catch( ResolutionException ignored )
		{
			return new NamedValueImpl( namedValue.getName(), NullValue.INSTANCE );
		}
		if( namedValue.getReferenceKind() != Kind.INTEGER )
			throw new IllegalValueException( "Only integer values allowed" );

		if( namedValue.toIntegerValue().asLong() < 0L )
			throw new ConstraintViolationException( "All values must be equal or greater then zero!" );

		return namedValue;
	}
}
