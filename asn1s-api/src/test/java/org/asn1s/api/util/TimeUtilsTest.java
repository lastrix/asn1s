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

import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtilsTest
{
	@Test
	public void testUTCParse() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withNano( 0 ).toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyMMddHHmmss" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + 'Z';
		Instant instant = TimeUtils.parseUTCTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testUTCParseMinutes() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withSecond( 0 ).withNano( 0 ).toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyMMddHHmm" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + 'Z';
		Instant instant = TimeUtils.parseUTCTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testUTCParseFails() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withNano( 0 ).toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyMMddHHmmss" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + "+01Z";
		Assert.assertFalse( "Value is not valid", TimeUtils.isUTCTimeValue( result ) );
		Instant instant = TimeUtils.parseUTCTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testUTCParseCustomTz() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withNano( 0 ).toInstant( ZoneOffset.ofHours( 4 ) );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyMMddHHmmssZ" ).withZone( ZoneId.systemDefault() );
		String result = formatter.format( now );
		Instant instant = TimeUtils.parseUTCTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testUTCParseMinutesCustomTz() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withSecond( 0 ).withNano( 0 ).toInstant( ZoneOffset.ofHours( 4 ) );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyMMddHHmmZ" ).withZone( ZoneId.systemDefault() );
		String result = formatter.format( now );
		Instant instant = TimeUtils.parseUTCTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testGeneralizedParse() throws Exception
	{
		Instant now = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHHmmss.SSS" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + 'Z';
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testGeneralizedParseNoZ() throws Exception
	{
		Instant now = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHHmmss.SSS" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now );
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testGeneralizedParseNoFracture() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withNano( 0 ).toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHHmmss" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + 'Z';
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testGeneralizedParseNoFractureNoSeconds() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withSecond( 0 ).withNano( 0 ).toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHHmm" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + 'Z';
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testGeneralizedParseNoFractureNoZ() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withNano( 0 ).toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHHmmss" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now );
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testGeneralizedParseNoMinutesWithFracture() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		ldt = ldt.withMinute( 59 ).withSecond( 52 ).withNano( 800000000 );
		now = ldt.toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHH" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + ".998Z";
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testGeneralizedParseCustomTz() throws Exception
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		now = ldt.withNano( 0 ).toInstant( ZoneOffset.UTC );
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHHmmssZ" ).withZone( ZoneId.systemDefault() );
		String result = formatter.format( now );
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test( expected = IllegalArgumentException.class )
	public void testGeneralizedParseFails() throws Exception
	{
		Instant now = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyyMMddHHmmss" ).withZone( ZoneId.of( "GMT" ) );
		String result = formatter.format( now ) + "+01Z";
		Assert.assertFalse( "Value is not valid", TimeUtils.isGeneralizedTimeValue( result ) );
		Instant instant = TimeUtils.parseGeneralizedTime( result );
		Assert.assertEquals( "Values are not equal", now, instant );
	}

	@Test
	public void testFormatInstant000()
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		ldt = ldt.withYear( 2000 ).withMonth( 1 ).withDayOfMonth( 1 ).withHour( 1 ).withMinute( 0 ).withSecond( 0 ).withNano( 0 );
		now = ldt.toInstant( ZoneOffset.UTC );
		String result = TimeUtils.formatInstant( now, TimeUtils.GENERALIZED_TIME_FORMAT, true );
		Assert.assertEquals( "Values are not equal", "200001010100Z", result );
	}

	@Test
	public void testFormatInstant00()
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		ldt = ldt.withYear( 2000 ).withMonth( 1 ).withDayOfMonth( 1 ).withHour( 1 ).withMinute( 0 ).withSecond( 0 )
				.withNano( 880000000 );
		now = ldt.toInstant( ZoneOffset.UTC );
		String result = TimeUtils.formatInstant( now, TimeUtils.GENERALIZED_TIME_FORMAT, true );
		Assert.assertEquals( "Values are not equal", "20000101010000.88Z", result );
	}

	@Test
	public void testFormatInstant0()
	{
		Instant now = Instant.now();
		LocalDateTime ldt = LocalDateTime.ofInstant( now, ZoneId.of( "GMT" ) );
		ldt = ldt.withYear( 2000 ).withMonth( 1 ).withDayOfMonth( 1 ).withHour( 1 ).withMinute( 0 ).withSecond( 0 )
				.withNano( 800000000 );
		now = ldt.toInstant( ZoneOffset.UTC );
		String result = TimeUtils.formatInstant( now, TimeUtils.GENERALIZED_TIME_FORMAT, true );
		Assert.assertEquals( "Values are not equal", "20000101010000.8Z", result );
	}
}
