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

package org.asn1s.core.type.x680;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.constraint.template.ComponentConstraintTemplate;
import org.asn1s.core.constraint.template.InnerTypesConstraintTemplate;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.type.ConstrainedType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * X.680, p 37.1
 */
public final class ExternalType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( ExternalType.class );

	public ExternalType()
	{
		setEncoding( TagEncoding.universal( UniversalType.External ) );
		type = createSubType();
	}

	private Type type;

	@Nullable
	@Override
	public Type getSibling()
	{
		return type;
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		type.accept( scope, valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		return type.optimize( scope, valueRef );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.External;
	}

	@Override
	public String toString()
	{
		return UniversalType.External.typeName();
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type!" );
		return new ExternalType();
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		type.validate( scope );
	}

	@Override
	protected void onDispose()
	{
		if( type != null )
			type.dispose();
		type = null;
	}

	private static Type createSubType()
	{
		List<ConstraintTemplate> templates = Arrays.asList(
				new ComponentConstraintTemplate( "syntaxes", null, Presence.Absent ),
				new ComponentConstraintTemplate( "transfer-syntax", null, Presence.Absent ),
				new ComponentConstraintTemplate( "fixed", null, Presence.Absent ) );

		InnerTypesConstraintTemplate identificationTypeConstraintsTemplate = new InnerTypesConstraintTemplate( templates, true );
		ConstraintTemplate identificationConstraintTemplate = new ComponentConstraintTemplate( "identification", identificationTypeConstraintsTemplate, Presence.Present );
		ConstraintTemplate template = new InnerTypesConstraintTemplate( Collections.singletonList( identificationConstraintTemplate ), true );
		return new ConstrainedType( template, EmbeddedPdvType.createSequenceType() );
	}
}
