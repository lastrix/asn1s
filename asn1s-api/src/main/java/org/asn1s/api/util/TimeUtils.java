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

import org.asn1s.api.UniversalType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.regex.Pattern;

@SuppressWarnings( "NumericCastThatLosesPrecision" )
public final class TimeUtils
{
	public static final String UTC_TIME_FORMAT = "yyMMddHHmmss";
	public static final String GENERALIZED_TIME_FORMAT = "yyyyMMddHHmmss.SSS";
	@SuppressWarnings( "ConstantConditions" )
	@NotNull
	public static final Charset CHARSET = UniversalType.VisibleString.charset();

	private TimeUtils()
	{
	}

	@NotNull
	public static String formatInstant( TemporalAccessor instant, String format, boolean optimize )
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( format )
				.withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( instant );
		if( result.indexOf( '.' ) > 0 )
		{
			if( result.endsWith( ".000" ) )
				result = result.substring( 0, result.length() - 4 );
			else if( result.endsWith( "00" ) )
				result = result.substring( 0, result.length() - 2 );
			else if( result.endsWith( "0" ) )
				result = result.substring( 0, result.length() - 1 );
		}

		if( optimize && result.endsWith( "00" ) )
			result = result.substring( 0, result.length() - 2 );
		// now we have to manually add the 'UTC' timezone identifier
		result += "Z";
		return result;
	}

	public static boolean isUTCTimeValue( CharSequence value )
	{
		return UTCParser.isValid( value );
	}

	public static boolean isGeneralizedTimeValue( CharSequence value )
	{
		return GeneralizedParser.isValid( value );
	}

	public static Instant parseGeneralizedTime( String value )
	{
		if( !GeneralizedParser.isValid( value ) )
			throw new IllegalArgumentException( "Not an GeneralizedTime string: " + value );

		return new GeneralizedParser( value ).asInstant();
	}

	public static Instant parseUTCTime( String value )
	{
		if( !UTCParser.isValid( value ) )
			throw new IllegalArgumentException( "Not an UTCTime string: " + value );
		return new UTCParser( value ).asInstant();
	}

//	private static final String TIME_FORMAT = "HHmmss";
//	private static final String TIME_FORMAT_COLONS = "HH:mm:ss";
//	private static final Pattern TIME_PATTERN = Pattern.compile( "(([0-1][0-9]|2[0-3])([0-5][0-9])([0-5][0-9])|([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))" );
//
//	private static final String DATE_FORMAT = "yyyyMMdd";
//	private static final Pattern DATE_PATTERN = Pattern.compile( "[0-9]{4}(0[0-9]|1[0-2])([0-2][0-9]|3[0-1])" );


	private static final class GeneralizedParser
	{
		private GeneralizedParser( String value )
		{
			this.value = value;
		}

		private final String value;
		private int formatType;
		private double fracture;
		private Instant result;

		private Instant asInstant()
		{
			if( value.indexOf( '.' ) == -1 )
				return asInstantNoFracture( value );

			splitValue();
			addFracture();
			return result;
		}

		@SuppressWarnings( "fallthrough" )
		private void addFracture()
		{
			switch( formatType )
			{
				case VALUE_DEFAULT_FORMAT:
					appendFracture( MINUTES_IN_HOUR, ChronoUnit.MINUTES );
				case VALUE_FORMAT_WITH_MINUTES:
					appendFracture( SECONDS_IN_MINUTE, ChronoUnit.SECONDS );
				case VALUE_FORMAT_WITH_SECONDS:
					appendFracture( MILLIS_IN_SECOND, ChronoUnit.MILLIS );
					break;

				default:
					throw new IllegalArgumentException( "Unable to use format type: " + formatType );
			}
		}

		private void appendFracture( double base, TemporalUnit unit )
		{
			double raw = base * fracture;
			long units = (long)raw;
			if( units != 0 )
				result = result.plus( units, unit );
			fracture = raw % 1;
		}

		private void splitValue()
		{
			int idx = value.indexOf( '.' );
			int end = idx + 1;
			char c = value.charAt( end );
			while( Character.isDigit( c ) )
			{
				end++;
				if( value.length() == end )
					break;
				c = value.charAt( end );
			}

			String baseValue = value.length() == end ? value.substring( 0, idx ) : value.substring( 0, idx ) + value.substring( end );
			formatType = idx;
			result = asInstantNoFracture( baseValue );
			String fractureValue = value.substring( idx + 1, end );
			fracture = Long.parseLong( fractureValue ) / StrictMath.pow( DECIMAL_BASE, fractureValue.length() );
		}

		private static Instant asInstantNoFracture( String value )
		{
			if( value.endsWith( "Z" ) )
			{
				String cleared = value.substring( 0, value.length() - 1 );
				return buildInstant( cleared, cleared.length(), false );
			}

			int idx = value.replace( '-', '+' ).indexOf( '+' );
			if( idx == -1 )
				return buildInstant( value, value.length(), false );
			return buildInstant( value, idx, true );
		}

		private static Instant buildInstant( CharSequence value, int length, boolean withTz )
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern( getValueFormat( length, withTz ) );
			if( !withTz )
				formatter = formatter.withZone( ZoneId.of( "GMT" ) );

			return formatter.parse( value, Instant:: from );
		}

		@NotNull
		private static String getValueFormat( int length, boolean withTz )
		{
			if( length == VALUE_DEFAULT_FORMAT )
				return withTz ? G_FORMAT + 'Z' : G_FORMAT;

			if( length == VALUE_FORMAT_WITH_MINUTES )
				return withTz ? G_FORMAT_M + 'Z' : G_FORMAT_M;

			if( length == VALUE_FORMAT_WITH_SECONDS )
				return withTz ? G_FORMAT_M_S + 'Z' : G_FORMAT_M_S;

			throw new IllegalArgumentException( "Unable to detect format for length: " + length );
		}

		private static boolean isValid( CharSequence value )
		{
			return G_PATTERN.matcher( value ).matches();
		}

		private static final String G_FORMAT = "yyyyMMddHH";
		private static final String G_FORMAT_M = "yyyyMMddHHmm";
		private static final String G_FORMAT_M_S = "yyyyMMddHHmmss";
		private static final double SECONDS_IN_MINUTE = 60.0d;
		private static final double MILLIS_IN_SECOND = 1000.0d;
		private static final double MINUTES_IN_HOUR = 60.0d;
		private static final int VALUE_DEFAULT_FORMAT = 10;
		private static final int VALUE_FORMAT_WITH_MINUTES = 12;
		private static final int VALUE_FORMAT_WITH_SECONDS = 14;
		private static final double DECIMAL_BASE = 10.0d;
		private static final Pattern G_PATTERN = Pattern.compile( "[0-9]{4}(0[0-9]|1[0-2])([0-2][0-9]|3[0-1])([0-1][0-9]|2[0-3])(([0-5][0-9])([0-5][0-9])?)?([.,][0-9]+)?(Z|[+\\-]([0-1][0-9]|2[0-3])([0-5][0-9]))?" );
	}

	private static final class UTCParser
	{
		private UTCParser( String value )
		{
			this.value = value;
		}

		private final String value;

		@NotNull
		private Instant asInstant()
		{
			return isGMT() ? asInstantFromGMT() : asInstantFromCustomTz();
		}

		private boolean isGMT()
		{
			return value.endsWith( "Z" ) || value.indexOf( '-' ) == -1 && value.indexOf( '+' ) == -1;
		}

		private Instant asInstantFromCustomTz()
		{
			int idx = value.replace( '-', '+' ).indexOf( '+' );
			if( idx == 10 )
				return DateTimeFormatter.ofPattern( UTC_FORMAT_TZ ).parse( value, Instant:: from );

			return DateTimeFormatter.ofPattern( UTC_FORMAT_S_TZ ).parse( value, Instant:: from );
		}

		private Instant asInstantFromGMT()
		{
			String valueCleared = value.endsWith( "Z" ) ? value.substring( 0, value.length() - 1 ) : value;
			if( valueCleared.length() == 10 )
				return DateTimeFormatter.ofPattern( UTC_FORMAT ).withZone( ZoneId.of( "GMT" ) ).parse( valueCleared, Instant:: from );

			return DateTimeFormatter.ofPattern( UTC_FORMAT_S ).withZone( ZoneId.of( "GMT" ) ).parse( valueCleared, Instant:: from );
		}

		private static boolean isValid( CharSequence value )
		{
			return UTC_PATTERN.matcher( value ).matches();
		}

		private static final String UTC_FORMAT = "yyMMddHHmm";
		private static final String UTC_FORMAT_TZ = "yyMMddHHmmZ";
		private static final String UTC_FORMAT_S = "yyMMddHHmmss";
		private static final String UTC_FORMAT_S_TZ = "yyMMddHHmmssZ";
		private static final Pattern UTC_PATTERN = Pattern.compile( "[0-9]{2}(0[0-9]|1[0-2])([0-2][0-9]|3[0-1])([0-1][0-9]|2[0-3])([0-5][0-9])([0-5][0-9])?(Z|[+\\-]([0-1][0-9]|2[0-3])([0-5][0-9]))?" );
	}
}
