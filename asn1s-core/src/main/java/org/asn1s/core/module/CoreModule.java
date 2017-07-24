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

package org.asn1s.core.module;

import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.module.EmptyModuleResolver;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleReference;
import org.asn1s.api.type.Type;
import org.asn1s.core.CoreUtils;
import org.asn1s.core.constraint.template.SettingsConstraintTemplate;
import org.asn1s.core.type.ConstrainedType;
import org.asn1s.core.type.DefinedTypeImpl;
import org.asn1s.core.type.TaggedTypeImpl;
import org.asn1s.core.type.x680.*;
import org.asn1s.core.type.x680.id.IriType;
import org.asn1s.core.type.x680.id.ObjectIdentifierType;
import org.asn1s.core.type.x680.id.RelativeOidIriType;
import org.asn1s.core.type.x680.id.RelativeOidType;
import org.asn1s.core.type.x680.string.*;
import org.asn1s.core.type.x680.time.GeneralizedTimeType;
import org.asn1s.core.type.x680.time.TimeType;
import org.asn1s.core.type.x680.time.UTCTimeType;
import org.asn1s.core.type.x681.TypeIdentifierClass;

public final class CoreModule extends AbstractModule
{
	private CoreModule()
	{
		super( new ModuleReference( CoreUtils.CORE_MODULE_NAME ), new EmptyModuleResolver() );
		registerType( UniversalType.Boolean, new BooleanType() );
		registerType( UniversalType.Integer, new IntegerType() );
		registerType( UniversalType.Null, new NullType() );
		registerType( UniversalType.Real, new RealType() );

		registerType( UniversalType.OidIri, new IriType() );
		registerType( UniversalType.ObjectIdentifier, new ObjectIdentifierType() );
		registerType( UniversalType.RelativeOidIri, new RelativeOidIriType() );
		registerType( UniversalType.RelativeOid, new RelativeOidType() );

		registerStringType( UniversalType.BMPString );
		registerStringType( UniversalType.GeneralString );
		registerStringType( UniversalType.GraphicString );
		registerStringType( UniversalType.IA5String );
		registerStringType( UniversalType.NumericString );
		registerStringType( UniversalType.PrintableString );
		registerStringType( UniversalType.T61String );
		registerStringType( UniversalType.UniversalString );
		registerStringType( UniversalType.UTF8String );
		registerStringType( UniversalType.VideotexString );
		registerStringType( UniversalType.VisibleString );

		registerStringType( UniversalType.Teletex );
		registerStringType( UniversalType.ISO646String );

		registerType( UniversalType.BitString, new BitStringType() );
		registerType( UniversalType.CharacterString, new CharacterStringType() );
		registerType( UniversalType.OctetString, new OctetStringType() );
		registerType( UniversalType.ObjectDescriptor, new ObjectDescriptorType() );

		registerType( UniversalType.Time, new TimeType() );
		/*
		 * X.680, p 38.4.3
		 * DATE-TIME ::= [UNIVERSAL 33] IMPLICIT TIME (SETTINGS "Basic=Date-Time Date=YMD Year=Basic Time=HMS Local-or-UTC=L"
		 */
		registerTimeType( UniversalType.DateTime, new SettingsConstraintTemplate( "Basic=Date-Time Date=YMD Year=Basic Time=HMS Local-or-UTC=L" ) );
		/*
		 * X.680, p 38.4.2
		 * DATE ::= [UNIVERSAL 31] IMPLICIT TIME (SETTINGS "Basic=Date Date=YMD Year=Basic")
		 */
		registerTimeType( UniversalType.Date, new SettingsConstraintTemplate( "Basic=Date Date=YMD Year=Basic" ) );
		/*
		 * X.680, p 38.4.4
		 * DURATION ::= [UNIVERSAL 34] IMPLICIT TIME (SETTINGS "Basic=Interval Interval-type=D")
		 */
		registerTimeType( UniversalType.Duration, new SettingsConstraintTemplate( "Basic=Interval Interval-type=D" ) );
		/*
		 * X.680, p 38.4.2
		 * TIME-OF-DAY ::= [UNIVERSAL 32] IMPLICIT TIME (SETTINGS "Basic=Time Time=HMS Local-or-UTC=L")
		 */
		registerTimeType( UniversalType.TimeOfDay, new SettingsConstraintTemplate( "Basic=Time Time=HMS Local-or-UTC=L" ) );

		registerType( UniversalType.GeneralizedTime, new GeneralizedTimeType() );
		registerType( UniversalType.UTCTime, new UTCTimeType() );

		registerType( UniversalType.EmbeddedPdv, new EmbeddedPdvType() );
		registerType( UniversalType.External, new ExternalType() );

		registerType( "TYPE-IDENTIFIER", new TypeIdentifierClass() );

		try
		{
			validate();
		} catch( ValidationException | ResolutionException e )
		{
			throw new IllegalStateException( "Unable to validate Core module: " + e.getMessage(), e );
		}
	}

	private void registerTimeType( UniversalType typeKind, SettingsConstraintTemplate constraintTemplate )
	{
		TagEncoding encoding = TagEncoding.create( TagMethod.Unknown, TagMethod.Implicit, TagClass.Universal, typeKind.tagNumber() );
		Type type = new ConstrainedType( constraintTemplate, new TaggedTypeImpl( encoding, UniversalType.Time.ref() ) );
		registerType( typeKind, new DefinedTypeImpl( this, typeKind.name(), type ) );
	}

	private void registerStringType( UniversalType universalType )
	{
		registerType( universalType, new StringTypeImpl( universalType ) );
	}

	private void registerType( UniversalType universalType, Type type )
	{
		registerType( universalType.typeName().toString(), type );
	}

	private void registerType( String name, Type type )
	{
		DefinedTypeImpl definedType = new DefinedTypeImpl( this, name, type );
		getTypeResolver().add( definedType );

		try
		{
			definedType.validate( createScope() );
		} catch( ValidationException | ResolutionException e )
		{
			throw new IllegalStateException( e );
		}
	}

	@Override
	public Module getCoreModule()
	{
		return this;
	}

	@Override
	protected void onValidate()
	{
		// nothing to do, core types has nothing to validate
	}

	private static final Module INSTANCE = new CoreModule();

	public static Module getInstance()
	{
		return INSTANCE;
	}

	@Override
	public boolean isAllTypesExtensible()
	{
		return false;
	}

	@Override
	public boolean hasExports()
	{
		return false;
	}

	@Override
	public boolean isExportAll()
	{
		return true;
	}
}
