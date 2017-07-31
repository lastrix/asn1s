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
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.TemplateParameter;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.ByteArrayValue;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.api.value.x680.ValueCollection;
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
		assert lhs.getKind() == Kind.REAL || lhs.getKind() == Kind.INTEGER;
		Kind kind = rhs.getReferenceKind();
		if( kind == Kind.REAL || kind == Kind.INTEGER )
		{
			assert rhs.getValueRef() instanceof Value;
			return lhs.compareTo( (Value)rhs.getValueRef() );
		}
		throw new UnsupportedOperationException( "Unable to compare number value to " + kind );
	}

	public static void assertParameterMap( Scope scope, Template template ) throws ValidationException, ResolutionException
	{
		if( template.getParameterCount() == 0 )
			throw new ValidationException( "No parameters for template type" );

		for( int i = 0; i < template.getParameterCount(); i++ )
		{
			TemplateParameter parameter = template.getParameter( i );
			assertReference( parameter );
			assertGovernor( scope, parameter );
		}
	}

	private static void assertReference( TemplateParameter parameter ) throws ValidationException
	{
		if( parameter.isTypeRef() )
			return;

		if( parameter.isValueRef() && parameter.getGovernor() == null )
			throw new ValidationException( "Governor type must be present for value parameter" );
	}

	private static void assertGovernor( Scope scope, TemplateParameter parameter ) throws ResolutionException, ValidationException
	{
		if( parameter.getGovernor() == null )
			return;

		Type type = parameter.getGovernor().resolve( scope );
		//noinspection ConstantConditions isAbstract is a check for template != null
		if( type instanceof DefinedType && ( (DefinedType)type ).isAbstract() && !( (DefinedType)type ).getTemplate().isInstance() )
			throw new ValidationException( "Unable to use Type template as governor" );
	}

	@NotNull
	public static ByteArrayValue byteArrayFromBitString( @NotNull String content )
	{
		return new BinConverter( content ).convert();
	}

	@NotNull
	public static ByteArrayValue byteArrayFromHexString( @NotNull String content )
	{
		return new HexConverter( content ).convert();
	}

	public static String paramMapToString( Map<String, TemplateParameter> parameterMap )
	{
		List<TemplateParameter> parameters = new ArrayList<>( parameterMap.values() );
		parameters.sort( Comparator.comparingInt( TemplateParameter:: getIndex ) );
		return StringUtils.join( parameters, ", " );
	}

	@NotNull
	public static ValueCollection toValueCollectionOrDie( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, IllegalValueException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() != Kind.NAMED_COLLECTION && value.getKind() != Kind.COLLECTION )
			throw new IllegalValueException( "Illegal Sequence value: " + value );
		return value.toValueCollection();
	}

	private abstract static class AbstractByteArrayConverter
	{
		AbstractByteArrayConverter( String content )
		{
			originalLength = content.length();
			this.content = alignToStride( content );
		}

		private final String content;
		private final int originalLength;

		private String alignToStride( String content )
		{
			int stride = getStride();
			int count = content.length() % stride;
			if( count == 0 )
				return content;
			count = stride - count;
			StringBuilder sb = new StringBuilder();
			sb.append( content );
			while( count > 0 )
			{
				sb.append( '0' );
				count--;
			}
			return sb.toString();
		}


		ByteArrayValueImpl convert()
		{
			try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
			{
				return writeToStream( os );
			} catch( IOException e )
			{
				throw new IllegalStateException( e );
			}
		}

		private ByteArrayValueImpl writeToStream( ByteArrayOutputStream os )
		{
			for( int i = 0; i * getStride() < content.length(); i++ )
				writeOctet( os, i );

			byte[] bytes = os.toByteArray();
			int usedBits = originalLength * getMultiplier();
			return usedBits > 0
					? new ByteArrayValueImpl( usedBits, bytes )
					: new ByteArrayValueImpl( 0, EMPTY_ARRAY );
		}

		private void writeOctet( ByteArrayOutputStream os, int i )
		{
			int offset = i * getStride();
			String value = content.substring( offset, offset + getStride() );
			os.write( Integer.parseInt( value, getRadix() ) & BYTE_MASK );
		}

		protected abstract int getStride();

		protected abstract int getRadix();

		protected abstract int getMultiplier();
	}

	private static final class BinConverter extends AbstractByteArrayConverter
	{
		private BinConverter( String content )
		{
			super( normalize( content ) );
		}

		private static String normalize( @NotNull String content )
		{
			if( !content.startsWith( "'" ) && content.endsWith( "'B" ) )
				throw new IllegalArgumentException( "Not BString: " + content );
			return CLEAR_BIN_PATTERN.matcher( content.substring( 1, content.length() - 2 ) ).replaceAll( "" );
		}

		@Override
		protected int getStride()
		{
			return 8;
		}

		@Override
		protected int getRadix()
		{
			return BIN_RADIX;
		}

		@Override
		protected int getMultiplier()
		{
			return 1;
		}
	}

	private static final class HexConverter extends AbstractByteArrayConverter
	{
		private HexConverter( String content )
		{
			super( normalize( content ) );
		}

		private static String normalize( @NotNull String content )
		{
			if( !content.startsWith( "'" ) && content.endsWith( "'H" ) )
				throw new IllegalArgumentException( "Not HString: " + content );
			return CLEAR_HEX_PATTERN.matcher( content.substring( 1, content.length() - 2 ) ).replaceAll( "" );
		}

		@Override
		protected int getStride()
		{
			return 2;
		}

		@Override
		protected int getRadix()
		{
			return HEX_RADIX;
		}

		@Override
		protected int getMultiplier()
		{
			return 4;
		}
	}
}
