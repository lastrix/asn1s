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
		BOOLEAN( UniversalType.BOOLEAN, new BooleanType() ),
		INTEGER( UniversalType.INTEGER, new IntegerType() ),
		NULL( UniversalType.NULL, new NullType() ),
		REAL( UniversalType.REAL, new RealType() ),

		OID_IRI( UniversalType.OID_IRI, new IriType() ),
		OBJECT_IDENTIFIER( UniversalType.OBJECT_IDENTIFIER, new ObjectIdentifierType() ),
		RELATIVE_OID_IRI( UniversalType.RELATIVE_OID_IRI, new RelativeOidIriType() ),
		RELATIVE_OID( UniversalType.RELATIVE_OID, new RelativeOidType() ),

		BMP_STRING( UniversalType.BMP_STRING ),
		GENERAL_STRING( UniversalType.GENERAL_STRING ),
		GRAPHIC_STRING( UniversalType.GRAPHIC_STRING ),
		IA5_STRING( UniversalType.IA5_STRING ),
		NUMERIC_STRING( UniversalType.NUMERIC_STRING ),
		PRINTABLE_STRING( UniversalType.PRINTABLE_STRING ),
		T61_STRING( UniversalType.T61_STRING ),
		UNIVERSAL_STRING( UniversalType.UNIVERSAL_STRING ),
		UTF8_STRING( UniversalType.UTF8_STRING ),
		VIDEOTEX_STRING( UniversalType.VIDEOTEX_STRING ),
		VISIBLE_STRING( UniversalType.VISIBLE_STRING ),

		TELETEX( UniversalType.TELETEX ),
		ISO_646_STRING( UniversalType.ISO_646_STRING ),

		BIT_STRING( UniversalType.BIT_STRING, new BitStringType() ),
		CHARACTER_STRING( UniversalType.CHARACTER_STRING, new CharacterStringType() ),
		OCTET_STRING( UniversalType.OCTET_STRING, new OctetStringType() ),
		OBJECT_DESCRIPTOR( UniversalType.OBJECT_DESCRIPTOR, new ObjectDescriptorType() ),

		TIME( UniversalType.TIME, new TimeType() ),

		/*
		 * X.680, p 38.4.3
		 * DATE-TIME ::= [UNIVERSAL 33] IMPLICIT TIME (SETTINGS "Basic=Date-Time Date=YMD Year=Basic Time=HMS Local-or-UTC=L"
		 */
		DATE_TIME( UniversalType.DATE_TIME, new SettingsConstraintTemplate( "Basic=Date-Time Date=YMD Year=Basic Time=HMS Local-or-UTC=L" ) ),
		/*
		 * X.680, p 38.4.2
		 * DATE ::= [UNIVERSAL 31] IMPLICIT TIME (SETTINGS "Basic=Date Date=YMD Year=Basic")
		 */
		DATE( UniversalType.DATE, new SettingsConstraintTemplate( "Basic=Date Date=YMD Year=Basic" ) ),
		/*
		 * X.680, p 38.4.4
		 * DURATION ::= [UNIVERSAL 34] IMPLICIT TIME (SETTINGS "Basic=Interval Interval-type=D")
		 */
		DURATION( UniversalType.DURATION, new SettingsConstraintTemplate( "Basic=Interval Interval-type=D" ) ),
		/*
		 * X.680, p 38.4.2
		 * TIME-OF-DAY ::= [UNIVERSAL 32] IMPLICIT TIME (SETTINGS "Basic=Time Time=HMS Local-or-UTC=L")
		 */
		TIME_OF_DAY( UniversalType.TIME_OF_DAY, new SettingsConstraintTemplate( "Basic=Time Time=HMS Local-or-UTC=L" ) ),

		GENERALIZED_TIME( UniversalType.GENERALIZED_TIME, new GeneralizedTimeType() ),
		UTC_TIME( UniversalType.UTC_TIME, new UTCTimeType() ),

		EMBEDDED_PDV( UniversalType.EMBEDDED_PDV, new EmbeddedPdvType() ),
		EXTERNAL( UniversalType.EXTERNAL, new ExternalType() ),
		TYPE_IDENTIFIER( "TYPE-IDENTIFIER", new TypeIdentifierClass() );

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
			TagEncoding encoding = TagEncoding.create( TagMethod.UNKNOWN, TagMethod.IMPLICIT, TagClass.UNIVERSAL, universalType.tagNumber() );
			instanceType = new ConstrainedType( constraintTemplate, new TaggedTypeImpl( encoding, UniversalType.TIME.ref() ) );
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
