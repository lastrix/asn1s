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

package org.asn1s.core.constraint.template;

import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.core.constraint.SettingsConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class SettingsConstraintTemplate implements ConstraintTemplate
{
	private static final Pattern SETTINGS_PATTERN =
			Pattern.compile( "^([A-Z][A-Za-z0-9\\-]+=[A-Z][A-Za-z0-9\\-]*)(\\s+[A-Z][A-Za-z0-9\\-]+=[A-Z][A-Za-z0-9\\-]*)*\\s*$" );

	public enum Setting
	{
		Time,
		Local_or_UTC,
		Midnight,
		Interval_type,
		SE_point,
		Recurrence,
		Date,
		Year
	}

	public enum BasicType
	{
		Date( Setting.Time, Setting.Local_or_UTC, Setting.Midnight, Setting.Interval_type, Setting.SE_point, Setting.Recurrence ),
		Time( Setting.Date, Setting.Year, Setting.Interval_type, Setting.SE_point, Setting.Recurrence ),
		Date_Time( Setting.Interval_type, Setting.SE_point, Setting.Recurrence ),
		Interval( Setting.Recurrence ),
		Rec_Interval;

		private final Set<Setting> prohibited;

		BasicType( Setting... prohibited )
		{
			this.prohibited = prohibited.length == 0
					? EnumSet.noneOf( Setting.class )
					: EnumSet.copyOf( Arrays.asList( prohibited ) );
		}

		public boolean isProhibited( Setting setting )
		{
			return prohibited.contains( setting );
		}
	}

	public SettingsConstraintTemplate( @NotNull String settings )
	{
		this.settings = settings;
	}

	private final String settings;

	@Override
	public Constraint build( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		assertType( type );
		if( !SETTINGS_PATTERN.matcher( settings ).matches() )
			throw new IllegalArgumentException( "Not an settings string: " + settings );

		BasicType basicType = null;
		Map<Setting, String> settingsMap = new EnumMap<>( Setting.class );
		String[] parts = settings.split( " " );
		for( String part : parts )
		{
			int idx = part.indexOf( '=' );
			String property = part.substring( 0, idx );
			String value = part.substring( idx + 1 );

			if( property.equals( "Basic" ) )
			{
				if( basicType != null )
					throw new IllegalArgumentException( "Basic redefinition" );

				basicType = BasicType.valueOf( value.replace( '-', '_' ) );
			}
			else
			{
				// basic type should be declared as first name=value pair, so no actual checks is required.
				assert basicType != null;
				Setting setting = Setting.valueOf( property.replace( '-', '_' ) );
				if( basicType.isProhibited( setting ) )
					throw new IllegalArgumentException( "Illegal property '" + settings + "' for basic: " + basicType );

				settingsMap.put( setting, value );
			}
		}
		assert basicType != null;

		return new SettingsConstraint( type, basicType, settingsMap, settings );
	}

	public static void assertType( @NotNull Type type ) throws ValidationException
	{
		if( type.getFamily() != Family.Time )
			throw new ValidationException( "Unable to apply to type: " + type );
	}
}
