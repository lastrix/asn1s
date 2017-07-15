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
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.core.constraint.template.ComponentConstraintTemplate;
import org.asn1s.core.constraint.template.InnerTypesConstraintTemplate;
import org.asn1s.core.type.BuiltinType;
import org.asn1s.core.type.ConstrainedType;
import org.asn1s.core.type.x680.collection.ChoiceType;
import org.asn1s.core.type.x680.collection.SequenceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * X.680, p 36.1
 */
public final class EmbeddedPdvType extends BuiltinType
{
	private static final Log log = LogFactory.getLog( EmbeddedPdvType.class );

	public EmbeddedPdvType()
	{
		setEncoding( TagEncoding.universal( UniversalType.EmbeddedPdv ) );
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
		return Family.EmbeddedPdv;
	}

	@Override
	public String toString()
	{
		return UniversalType.EmbeddedPdv.typeName();
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type!" );
		return new EmbeddedPdvType();
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		type.validate( scope );
	}

	@Override
	protected void onDispose()
	{
		type.dispose();
		type = null;
	}

	private static Type createSubType()
	{
		ConstraintTemplate template = new InnerTypesConstraintTemplate(
				Collections.singletonList( new ComponentConstraintTemplate( "data-value-descriptor", null, Presence.Absent ) ),
				true
		);
		return new ConstrainedType( template, createSequenceType() );
	}

	static Type createSequenceType()
	{
		/*
		 SEQUENCE {
			 identification CHOICE {
				 syntaxes SEQUENCE { abstract OBJECT IDENTIFIER, transfer OBJECT IDENTIFIER },
				 syntax OBJECT IDENTIFIER,
				 presentation-context-id INTEGER,
				 context-negotiation SEQUENCE { presentation-context-id INTEGER, transfer-syntax OBJECT IDENTIFIER },
				 transfer-syntax OBJECT IDENTIFIER,
				 fixed NULL
			 },
			 data-value-descriptor ObjectDescriptor OPTIONAL,
			 string-value OCTET STRING
		 } ( WITH COMPONENTS { ..., data-value-descriptor ABSENT } )
		 */

		SequenceType pdvType = new SequenceType( true );
		ChoiceType identification = new ChoiceType( true );

		Ref<Type> oidRef = UniversalType.ObjectIdentifier.ref();
		SequenceType identification_syntaxes = new SequenceType( true );
		identification_syntaxes.addComponent( Kind.Primary, "abstract", oidRef, false, null );
		identification_syntaxes.addComponent( Kind.Primary, "transfer", oidRef, false, null );

		identification.addComponent( Kind.Primary, "syntaxes", identification_syntaxes, false, null );
		identification.addComponent( Kind.Primary, "syntax", oidRef, false, null );
		identification.addComponent( Kind.Primary, "presentation-context-id", UniversalType.Integer.ref(), false, null );

		SequenceType contextNegotiation = new SequenceType( true );
		contextNegotiation.addComponent( Kind.Primary, "presentation-context-id", UniversalType.Integer.ref(), false, null );
		contextNegotiation.addComponent( Kind.Primary, "transfer-syntax", oidRef, false, null );

		identification.addComponent( Kind.Primary, "context-negotiation", contextNegotiation, false, null );
		identification.addComponent( Kind.Primary, "transfer-syntax", oidRef, false, null );
		identification.addComponent( Kind.Primary, "fixed", UniversalType.Null.ref(), false, null );

		pdvType.addComponent( Kind.Primary, "identification", identification, false, null );
		pdvType.addComponent( Kind.Primary, "data-value-descriptor", UniversalType.ObjectDescriptor.ref(), true, null );
		pdvType.addComponent( Kind.Primary, "string-value", UniversalType.OctetString.ref(), false, null );
		return pdvType;
	}
}
