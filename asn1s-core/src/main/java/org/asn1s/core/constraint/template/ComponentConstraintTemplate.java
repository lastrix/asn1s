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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.util.RefUtils;
import org.asn1s.core.constraint.ComponentConstraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

public class ComponentConstraintTemplate implements ConstraintTemplate
{
	private static final Log log = LogFactory.getLog( ComponentConstraintTemplate.class );

	public ComponentConstraintTemplate( @NotNull String name, @Nullable ConstraintTemplate template, @Nullable Presence presence )
	{
		if( presence == null )
		{
			log.warn( "'presence' is null, probable bug detected. The value will be replaced with 'None'." );
			presence = Presence.NONE;
		}

		RefUtils.assertValueRef( name );
		this.name = name;
		this.template = template;
		this.presence = presence;
	}

	private final String name;
	private final ConstraintTemplate template;
	private final Presence presence;

	@Override
	public Constraint build( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		assertType( type );
		NamedType namedType = type.getNamedType( name );
		if( namedType == null )
			throw new ValidationException( "There is no field '" + name + "' in type: " + type );

		Constraint constraint = template == null ? null : template.build( scope, namedType );

		return new ComponentConstraint( name, constraint, presence );
	}

	public static void assertType( @NotNull Type type ) throws ValidationException
	{
		if( !ALLOWED.contains( type.getFamily() ) )
			throw new ValidationException( "Unable to apply to type: " + type );
	}

	private static final Collection<Family> ALLOWED =
			EnumSet.copyOf(
					Arrays.asList(
							Family.CHOICE,
							Family.SEQUENCE,
							Family.SET
					)
			);
}
