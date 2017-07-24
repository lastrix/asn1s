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
import org.asn1s.api.module.TypeResolver;
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
	private enum CoreType
	{
		Boolean( UniversalType.Boolean, new BooleanType() ),
		Integer( UniversalType.Integer, new IntegerType() ),
		Null( UniversalType.Null, new NullType() ),
		Real( UniversalType.Real, new RealType() ),

		OidIri( UniversalType.OidIri, new IriType() ),
		ObjectIdentifier( UniversalType.ObjectIdentifier, new ObjectIdentifierType() ),
		RelativeOidIri( UniversalType.RelativeOidIri, new RelativeOidIriType() ),
		RelativeOid( UniversalType.RelativeOid, new RelativeOidType() ),

		BMPString( UniversalType.BMPString ),
		GeneralString( UniversalType.GeneralString ),
		GraphicString( UniversalType.GraphicString ),
		IA5String( UniversalType.IA5String ),
		NumericString( UniversalType.NumericString ),
		PrintableString( UniversalType.PrintableString ),
		T61String( UniversalType.T61String ),
		UniversalString( UniversalType.UniversalString ),
		UTF8String( UniversalType.UTF8String ),
		VideotexString( UniversalType.VideotexString ),
		VisibleString( UniversalType.VisibleString ),

		Teletex( UniversalType.Teletex ),
		ISO646String( UniversalType.ISO646String ),

		BitString( UniversalType.BitString, new BitStringType() ),
		CharacterString( UniversalType.CharacterString, new CharacterStringType() ),
		OctetString( UniversalType.OctetString, new OctetStringType() ),
		ObjectDescriptor( UniversalType.ObjectDescriptor, new ObjectDescriptorType() ),

		Time( UniversalType.Time, new TimeType() ),

		/*
		 * X.680, p 38.4.3
		 * DATE-TIME ::= [UNIVERSAL 33] IMPLICIT TIME (SETTINGS "Basic=Date-Time Date=YMD Year=Basic Time=HMS Local-or-UTC=L"
		 */
		DateTime( UniversalType.DateTime, new SettingsConstraintTemplate( "Basic=Date-Time Date=YMD Year=Basic Time=HMS Local-or-UTC=L" ) ),
		/*
		 * X.680, p 38.4.2
		 * DATE ::= [UNIVERSAL 31] IMPLICIT TIME (SETTINGS "Basic=Date Date=YMD Year=Basic")
		 */
		Date( UniversalType.Date, new SettingsConstraintTemplate( "Basic=Date Date=YMD Year=Basic" ) ),
		/*
		 * X.680, p 38.4.4
		 * DURATION ::= [UNIVERSAL 34] IMPLICIT TIME (SETTINGS "Basic=Interval Interval-type=D")
		 */
		Duration( UniversalType.Duration, new SettingsConstraintTemplate( "Basic=Interval Interval-type=D" ) ),
		/*
		 * X.680, p 38.4.2
		 * TIME-OF-DAY ::= [UNIVERSAL 32] IMPLICIT TIME (SETTINGS "Basic=Time Time=HMS Local-or-UTC=L")
		 */
		TimeOfDay( UniversalType.TimeOfDay, new SettingsConstraintTemplate( "Basic=Time Time=HMS Local-or-UTC=L" ) ),

		GeneralizedTime( UniversalType.GeneralizedTime, new GeneralizedTimeType() ),
		UTCTime( UniversalType.UTCTime, new UTCTimeType() ),

		EmbeddedPdv( UniversalType.EmbeddedPdv, new EmbeddedPdvType() ),
		External( UniversalType.External, new ExternalType() ),
		TypeIdentifier( "TYPE-IDENTIFIER", new TypeIdentifierClass() );

		private final String typeName;
		private final Type instanceType;

		CoreType( UniversalType universalType, Type instanceType )
		{
			this( universalType.typeName().getName(), instanceType );
		}

		CoreType( UniversalType universalType )
		{
			this( universalType.typeName().getName(), new StringTypeImpl( universalType ) );
		}

		CoreType( String typeName, Type instanceType )
		{
			this.typeName = typeName;
			this.instanceType = instanceType;
		}

		CoreType( UniversalType universalType, SettingsConstraintTemplate constraintTemplate )
		{
			typeName = universalType.typeName().getName();
			TagEncoding encoding = TagEncoding.create( TagMethod.Unknown, TagMethod.Implicit, TagClass.Universal, universalType.tagNumber() );
			instanceType = new ConstrainedType( constraintTemplate, new TaggedTypeImpl( encoding, UniversalType.Time.ref() ) );
		}

		public String getName()
		{
			return typeName;
		}

		public Type getInstanceType()
		{
			return instanceType;
		}
	}

	private CoreModule()
	{
		super( new ModuleReference( CoreUtils.CORE_MODULE_NAME ), new EmptyModuleResolver() );
		TypeResolver typeResolver = getTypeResolver();
		for( CoreType type : CoreType.values() )
			typeResolver.add( new DefinedTypeImpl( this, type.getName(), type.getInstanceType() ) );

		onConstructionComplete();
	}

	private void onConstructionComplete()
	{
		try
		{
			validate();
		} catch( ValidationException | ResolutionException e )
		{
			throw new IllegalStateException( "Unable to validate Core module: " + e.getMessage(), e );
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
