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

package org.asn1s.core;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Scope;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.Validation;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.ByteArrayValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.core.type.DefinedTypeTemplate;
import org.asn1s.core.value.x680.ByteArrayValueImpl;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@SuppressWarnings( {"UtilityClassCanBeEnum", "UtilityClass"} )
public final class CoreUtils
{
	private static final int BYTE_MASK = 0xFF;
	private static final Pattern CLEAR_HEX_PATTERN = Pattern.compile( "[^A-Za-z0-9]" );
	private static final Pattern CLEAR_BIN_PATTERN = Pattern.compile( "[^0-1]" );
	private static final byte[] EMPTY_ARRAY = new byte[0];
	private static final int HEX_RADIX = 16;
	private static final int BIN_RADIX = 2;
	public static final String CORE_MODULE_NAME = "ASN14J-CORE-MODULE";

	private CoreUtils()
	{
	}

	private static final Pattern BIN_HEX_PATTERN = Pattern.compile( "^'([A-Za-z0-9]'H|[01]'B)$" );

	public static boolean isConvertibleToByteArrayValue( CharSequence content )
	{
		return BIN_HEX_PATTERN.matcher( content ).matches();
	}

	public static int compareNumberToNamed( Value lhs, NamedValue rhs )
	{
		assert lhs.getKind() == Kind.Real || lhs.getKind() == Kind.Integer;
		Kind kind = rhs.getReferenceKind();
		if( kind == Kind.Real || kind == Kind.Integer )
		{
			assert rhs.getValueRef() instanceof Value;
			return lhs.compareTo( (Value)rhs.getValueRef() );
		}
		throw new UnsupportedOperationException( "Unable to compare number value to " + kind );
	}

	public static void assertParameterMap( Scope scope, Map<String, TemplateParameter> parameterMap ) throws ValidationException, ResolutionException
	{
		if( parameterMap.isEmpty() )
			throw new ValidationException( "No parameters for template type" );

		for( TemplateParameter parameter : parameterMap.values() )
			assertParameter( scope, parameter );
	}

	private static void assertParameter( Scope scope, TemplateParameter parameter ) throws ValidationException, ResolutionException
	{
		if( parameter.isValueRef() )
		{
			if( parameter.getGovernor() == null )
				throw new ValidationException( "Governor type must be present for value parameter" );
		}
		else if( !parameter.isTypeRef() )
			throw new ValidationException( "Unable to determine reference type: " + parameter );

		if( parameter.getGovernor() != null )
		{
			Type type = parameter.getGovernor().resolve( scope );
			if( type instanceof DefinedTypeTemplate )
				throw new ValidationException( "Unable to use Type template as governor" );
		}
	}

	@NotNull
	public static ByteArrayValue byteArrayFromBitString( @NotNull String content )
	{
		if( !content.startsWith( "'" ) && content.endsWith( "'B" ) )
			throw new IllegalArgumentException( "Not BString: " + content );
		content = CLEAR_BIN_PATTERN.matcher( content.substring( 1, content.length() - 2 ) ).replaceAll( "" );
		try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
		{
			return convertBitString( content, os );
		} catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
	}

	@NotNull
	private static ByteArrayValue convertBitString( @NotNull String content, ByteArrayOutputStream os )
	{
		for( int i = 0; i * 8 < content.length(); i++ )
		{
			String value = content.substring( i * 8, Math.min( i * 8 + 8, content.length() ) );
			int size = value.length();
			if( size < 8 )
			{
				StringBuilder sb = new StringBuilder();
				sb.append( value );
				for( int k = size; k < 8; k++ )
					sb.append( '0' );
				value = sb.toString();
			}
			os.write( Integer.parseInt( value, BIN_RADIX ) & BYTE_MASK );
		}
		byte[] bytes = os.toByteArray();
		int usedBits = content.length();
		return usedBits > 0
				? new ByteArrayValueImpl( usedBits, bytes )
				: new ByteArrayValueImpl( 0, EMPTY_ARRAY );
	}

	@NotNull
	public static ByteArrayValue byteArrayFromHexString( @NotNull String content )
	{
		if( !content.startsWith( "'" ) && content.endsWith( "'H" ) )
			throw new IllegalArgumentException( "Not HString: " + content );
		content = CLEAR_HEX_PATTERN.matcher( content.substring( 1, content.length() - 2 ) ).replaceAll( "" );

		try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
		{
			return convertHexString( content, os );
		} catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
	}

	@NotNull
	private static ByteArrayValue convertHexString( @NotNull String content, ByteArrayOutputStream os )
	{
		for( int i = 0; i * 2 < content.length(); i++ )
		{
			int size = Math.min( i * 2 + 2, content.length() );
			//noinspection NonConstantStringShouldBeStringBuffer
			String value = content.substring( i * 2, size );
			if( value.length() == 1 )
				value += "0";
			os.write( Integer.parseInt( value, HEX_RADIX ) & BYTE_MASK );
		}

		byte[] bytes = os.toByteArray();
		int usedBits = content.length() * 4;
		return usedBits > 0
				? new ByteArrayValueImpl( usedBits, bytes )
				: new ByteArrayValueImpl( 0, EMPTY_ARRAY );
	}

	public static String paramMapToString( Map<String, TemplateParameter> parameterMap )
	{
		List<TemplateParameter> parameters = new ArrayList<>( parameterMap.values() );
		parameters.sort( Comparator.comparingInt( TemplateParameter:: getIndex ) );
		return StringUtils.join( parameters, ", " );
	}

	public static void resolutionValidate( Scope scope, Validation copy ) throws ResolutionException
	{
		try
		{
			copy.validate( scope );
		} catch( ValidationException e )
		{
			throw new ResolutionException( "Unable to create new template type instance", e );
		}
	}
}
