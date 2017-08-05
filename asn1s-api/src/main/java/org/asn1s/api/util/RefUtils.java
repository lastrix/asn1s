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

package org.asn1s.api.util;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Validation;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.api.value.x680.NamedValue;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;


public final class RefUtils
{
	//private static final Pattern TIME_VALUE_PATTERN = PATTERN.compile( "^\"[0-9+\\-:.,/CDHMRPSTWZ]+\"$" );

	private RefUtils()
	{
	}

	/////////////////////////////////////// Validation /////////////////////////////////////////////////////////////////
	private static final Pattern TYPE_REF_PATTERN = Pattern.compile( "^([A-Z][A-Za-z0-9]*([\\-][A-Za-z0-9]+)*|(BIT|OCTET|CHARACTER)\\s+STRING|OBJECT\\s+IDENTIFIER|EMBEDDED\\s+PDV|INSTANCE\\s+OF)$" );

	/**
	 * Asserts name to be valid type name
	 *
	 * @param name type name
	 * @throws IllegalArgumentException if name is not type name
	 */
	public static void assertTypeRef( String name )
	{
		if( !isTypeRef( name ) )
			throw new IllegalArgumentException( "Not a type reference: " + name );
	}

	public static boolean isSameAsDefaultValue( Scope scope, ComponentType component, Value resolve ) throws ResolutionException
	{
		if( component.getDefaultValue() == null )
			return false;

		resolve = toBasicValue( scope, resolve );
		Value value = toBasicValue( scope, component.getDefaultValue() );
		return resolve.isEqualTo( value );
	}

	/**
	 * Check if name is type name
	 *
	 * @param name type name
	 * @return true if argument is type name, false otherwise
	 */
	public static boolean isTypeRef( CharSequence name )
	{
		return name != null && TYPE_REF_PATTERN.matcher( name ).matches();
	}

	private static final Pattern VALUE_REF_PATTERN = Pattern.compile( "^[a-z][A-Za-z0-9]*([\\-][A-Za-z0-9]+)*$" );

	public static void assertValueRef( String name )
	{
		if( !isValueRef( name ) )
			throw new IllegalArgumentException( "Not a value reference: " + name );
	}

	public static boolean isValueRef( CharSequence name )
	{
		return VALUE_REF_PATTERN.matcher( name ).matches();
	}

	////////////////////////////////////// Validation //////////////////////////////////////////////////////////////////

	public static void resolutionValidate( Scope scope, Validation validation ) throws ResolutionException
	{
		try
		{
			validation.validate( scope );
		} catch( ValidationException e )
		{
			throw new ResolutionException( "Unable to validate object: " + validation, e );
		}
	}

	public static Value toBasicValue( Scope scope, Ref<Value> ref ) throws ResolutionException
	{
		Value value = ref.resolve( scope );
		if( value.getKind() == Kind.NAME )
		{
			NamedValue namedValue = value.toNamedValue();
			if( namedValue.getValueRef() == null )
				return namedValue;

			return toBasicValue( scope, namedValue.getValueRef() );
		}
		return value;
	}

	public static boolean isValueRef( @Nullable Ref<?> ref )
	{
		return ref instanceof Value || ref instanceof ValueNameRef;
	}

	public static boolean isTypeRef( @Nullable Ref<?> ref )
	{
		return ref instanceof Type || ref instanceof TypeNameRef;
	}
}
