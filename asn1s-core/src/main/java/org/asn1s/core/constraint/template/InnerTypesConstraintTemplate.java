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
import org.asn1s.core.constraint.InnerTypesConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InnerTypesConstraintTemplate implements ConstraintTemplate
{
	public InnerTypesConstraintTemplate( List<ConstraintTemplate> components, boolean partial )
	{
		this.components = new ArrayList<>( components );
		this.partial = partial;
	}

	private final List<ConstraintTemplate> components;
	private final boolean partial;

	@Override
	public Constraint build( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		assertType( type );

		List<Constraint> constraints = new ArrayList<>( components.size() );
		for( ConstraintTemplate component : components )
			constraints.add( component.build( scope, type ) );

		return new InnerTypesConstraint( type, constraints, partial );
	}

	public static void assertType( @NotNull Type type ) throws ValidationException
	{
		if( !ALLOWED.contains( type.getFamily() ) )
			throw new ValidationException( "Unable to apply to type: " + type );
	}

	private static final Collection<Family> ALLOWED =
			EnumSet.copyOf(
					Arrays.asList(
							Family.SET,
							Family.SEQUENCE,
							Family.CHOICE,
							//Family.CHARACTER_STRING,
							//Family.REAL,
							Family.EXTERNAL,
							Family.EMBEDDED_PDV
					)
			);
}
