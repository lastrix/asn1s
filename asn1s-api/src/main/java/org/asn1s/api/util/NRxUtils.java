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

import java.math.BigInteger;
import java.util.regex.Pattern;

public final class NRxUtils
{
	private static final int MAX_LONG_DECIMALS = 16;
	private static final BigInteger BIG_ZERO = new BigInteger( "0" );
	private static final Pattern FORMAT_PATTERN = Pattern.compile( "^[+\\-]?(0|[1-9]\\d*)(\\.\\d*([eE][+\\-]?\\d+)?)?$" );

	private NRxUtils()
	{
	}

	/**
	 * Converts value to canonical NR3 ISO 6093 form
	 *
	 * @param value the value to convert, may be NR1, NR2, NR3 ISO 6093 form
	 * @return string
	 */
	public static String toCanonicalNR3( String value )
	{
		if( "-Infinity".equals( value ) || "Infinity".equals( value ) || "NaN".equals( value ) )
			return value;

		if( !FORMAT_PATTERN.matcher( value ).matches() )
			throw new IllegalArgumentException();

		value = value.toUpperCase();
		String mantisStr;
		String exponentStr;

		int expIndex = value.indexOf( 'E' );
		if( expIndex == -1 )
		{
			mantisStr = value;
			// no exponent in source
			exponentStr = "0";
		}
		else
		{
			mantisStr = value.substring( 0, expIndex ).trim();
			exponentStr = value.substring( expIndex + 1 ).trim();
		}

		int scale;
		String actualMantis;
		int dotIndex = mantisStr.indexOf( '.' );
		if( dotIndex == -1 )
		{
			scale = 0;
			actualMantis = mantisStr;
		}
		else
		{
			scale = mantisStr.length() - dotIndex - 1;
			String fracture = mantisStr.substring( dotIndex + 1 );
			if( isAllZeros( fracture ) )
			{
				scale -= fracture.length();
				fracture = "";
			}
			actualMantis = mantisStr.substring( 0, dotIndex ) + fracture;
		}

		return formatInt( actualMantis ) + ".E" + formatExponent( scale, exponentStr );
	}

	private static boolean isAllZeros( CharSequence fracture )
	{
		for( int i = 0; i < fracture.length(); i++ )
			if( fracture.charAt( i ) != '0' )
				return false;
		return true;
	}

	private static String formatExponent( int scale, String exponent )
	{
		if( scale == 0 )
			return formatInt( exponent );

		if( exponent.length() > MAX_LONG_DECIMALS )
		{
			BigInteger integer = new BigInteger( exponent );
			BigInteger scaleInt = new BigInteger( Integer.toString( scale ) );
			integer = integer.subtract( scaleInt );
			if( BIG_ZERO.equals( integer ) )
				return "+0";

			return integer.toString();
		}
		else
		{
			long value = Long.parseLong( exponent ) - scale;
			if( value == 0L )
				return "+0";

			return Long.toString( value );
		}
	}

	private static String formatInt( String mantis )
	{
		if( mantis.length() > MAX_LONG_DECIMALS )
		{
			BigInteger integer = new BigInteger( mantis );
			if( BIG_ZERO.equals( integer ) )
				return "+0";

			return integer.toString();
		}
		else
		{
			long value = Long.parseLong( mantis );
			if( value == 0L )
				return "+0";

			return Long.toString( value );
		}
	}
}
