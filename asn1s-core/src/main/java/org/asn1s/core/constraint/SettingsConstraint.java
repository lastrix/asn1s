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

package org.asn1s.core.constraint;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintType;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.constraint.template.SettingsConstraintTemplate;
import org.asn1s.core.constraint.template.SettingsConstraintTemplate.BasicType;
import org.asn1s.core.constraint.template.SettingsConstraintTemplate.Setting;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public class SettingsConstraint implements Constraint
{
	public SettingsConstraint( @NotNull Type type, @NotNull BasicType basicType, @NotNull Map<Setting, String> settingsMap, @NotNull String settingString )
	{
		this.type = type;
		this.basicType = basicType;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.settingsMap = settingsMap;
		this.settingString = settingString;
	}

	private final Type type;
	private final BasicType basicType;
	private final Map<Setting, String> settingsMap;
	private final String settingString;

	@Override
	public void check( Scope scope, Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = valueRef.resolve( scope );
		type.accept( scope, value );

		// TODO: not implemented
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public Constraint copyForType( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		SettingsConstraintTemplate.assertType( type );
		return new SettingsConstraint( type, basicType, settingsMap, settingString );
	}

	@Override
	public void assertConstraintTypes( Collection<ConstraintType> allowedTypes ) throws ValidationException
	{
		if( !allowedTypes.contains( ConstraintType.Settings ) )
			throw new ValidationException( "'Settings' constraint is not allowed" );
	}

	@Override
	public String toString()
	{
		return settingString;
	}
}
