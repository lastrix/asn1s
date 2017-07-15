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

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.constraint.Constraint;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.ElementSetSpecs;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ClassFieldType;
import org.asn1s.api.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class TypeConstraintTemplate implements ConstraintTemplate
{
	public TypeConstraintTemplate( Ref<Type> typeRef )
	{
		this.typeRef = typeRef;
		tableConstraintTemplate = new TableConstraintTemplate( typeRef, Collections.emptyList() );
	}

	private final Ref<Type> typeRef;
	private final TableConstraintTemplate tableConstraintTemplate;

	@Override
	public Constraint build( @NotNull Scope scope, @NotNull Type type ) throws ResolutionException, ValidationException
	{
		Type resolve = typeRef.resolve( scope );
		resolve.validate( scope );
		if( !resolve.hasElementSetSpecs() )
			throw new ValidationException( "Is not elementSetSpecs: " + typeRef );

		if( type instanceof ClassFieldType )
			return tableConstraintTemplate.build( scope, type );

		ElementSetSpecs specs = resolve.asElementSetSpecs();
		return specs.copyForType( scope, type );
	}
}
