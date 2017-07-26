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
import org.asn1s.api.UniversalType;
import org.asn1s.api.constraint.ConstraintTemplate;
import org.asn1s.api.constraint.Presence;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.type.ComponentType.Kind;
import org.asn1s.api.type.Type;
import org.asn1s.core.constraint.template.ComponentConstraintTemplate;
import org.asn1s.core.constraint.template.InnerTypesConstraintTemplate;
import org.asn1s.core.type.AbstractNestingBuiltinType;
import org.asn1s.core.type.ConstrainedType;
import org.asn1s.core.type.x680.collection.ChoiceType;
import org.asn1s.core.type.x680.collection.SequenceType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * X.680, p 36.1
 */
public final class EmbeddedPdvType extends AbstractNestingBuiltinType
{
	private static final Log log = LogFactory.getLog( EmbeddedPdvType.class );

	public EmbeddedPdvType()
	{
		super( createSubType() );
		setEncoding( TagEncoding.universal( UniversalType.EMBEDDED_PDV ) );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.EMBEDDED_PDV;
	}

	@Override
	public String toString()
	{
		return UniversalType.EMBEDDED_PDV.typeName().toString();
	}

	@NotNull
	@Override
	public Type copy()
	{
		log.warn( "Copying builtin type!" );
		return new EmbeddedPdvType();
	}

	private static Ref<Type> createSubType()
	{
		ConstraintTemplate template = new InnerTypesConstraintTemplate(
				Collections.singletonList( new ComponentConstraintTemplate( "data-value-descriptor", null, Presence.ABSENT ) ),
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

		Ref<Type> oidRef = UniversalType.OBJECT_IDENTIFIER.ref();
		SequenceType identificationSyntaxes = new SequenceType( true );
		identificationSyntaxes.addComponent( Kind.PRIMARY, "abstract", oidRef );
		identificationSyntaxes.addComponent( Kind.PRIMARY, "transfer", oidRef );

		identification.addComponent( Kind.PRIMARY, "syntaxes", identificationSyntaxes );
		identification.addComponent( Kind.PRIMARY, "syntax", oidRef );
		identification.addComponent( Kind.PRIMARY, "presentation-context-id", UniversalType.INTEGER.ref() );

		SequenceType contextNegotiation = new SequenceType( true );
		contextNegotiation.addComponent( Kind.PRIMARY, "presentation-context-id", UniversalType.INTEGER.ref() );
		contextNegotiation.addComponent( Kind.PRIMARY, "transfer-syntax", oidRef );

		identification.addComponent( Kind.PRIMARY, "context-negotiation", contextNegotiation );
		identification.addComponent( Kind.PRIMARY, "transfer-syntax", oidRef );
		identification.addComponent( Kind.PRIMARY, "fixed", UniversalType.NULL.ref() );

		pdvType.addComponent( Kind.PRIMARY, "identification", identification );
		pdvType.addComponent( Kind.PRIMARY, "data-value-descriptor", UniversalType.OBJECT_DESCRIPTOR.ref() ).setOptional( true );
		pdvType.addComponent( Kind.PRIMARY, "string-value", UniversalType.OCTET_STRING.ref() );
		return pdvType;
	}
}
