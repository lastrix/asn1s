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

package org.asn1s.io.ber.output;

import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.GenericTimeType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.TimeUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Instant;

public class TimeBerEncoder implements BerEncoder
{
	private static final Tag G_TAG = new Tag( TagClass.Universal, false, UniversalType.GeneralizedTime.tagNumber() );
	private static final Tag UTC_TAG = new Tag( TagClass.Universal, false, UniversalType.UTCTime.tagNumber() );

	@SuppressWarnings( "ConstantConditions" )
	@Override
	public void encode( @NotNull BerWriter os, @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		if( !( type instanceof GenericTimeType ) )
			throw new IOException( "Unable to write type: " + type.getFamily() + ". Type does not implement GenericTimeType" );
		GenericTimeType.Kind kind = ( (GenericTimeType)type ).getKind();
		boolean isDer = os.getRules() == BerRules.Der;
		if( kind == GenericTimeType.Kind.Generalized )
		{
			String content = convertValueToString( value, TimeUtils.GENERALIZED_TIME_FORMAT, isDer, true );
			StringBerEncoder.writeString( os, UniversalType.VisibleString.charset(), G_TAG, content, writeHeader );
		}
		else if( kind == GenericTimeType.Kind.UTC )
		{
			String content = convertValueToString( value, TimeUtils.UTC_TIME_FORMAT, isDer, false );
			StringBerEncoder.writeString( os, UniversalType.VisibleString.charset(), UTC_TAG, content, writeHeader );
		}
	}

	private static String convertValueToString( Value value, String format, boolean isDer, boolean isGeneralized )
	{
		Kind kind = value.getKind();
		if( kind == Kind.Time )
			return TimeUtils.formatInstant( value.toDateValue().asInstant(), format, !isDer );
		else if( kind == Kind.CString )
		{
			Instant instant;
			if( isGeneralized )
				instant = TimeUtils.parseGeneralizedTime( value.toStringValue().asString() );
			else
				instant = TimeUtils.parseUTCTime( value.toStringValue().asString() );

			return TimeUtils.formatInstant( instant, format, !isDer );
		}
		else
			throw new IllegalStateException( "Unable to convert value to Time string: " + value );
	}
}
