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

package org.asn1s.api;

import org.asn1s.api.type.Type;
import org.asn1s.api.type.TypeName;
import org.asn1s.api.type.TypeNameRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

public enum UniversalType
{
	//Reserved0( "Reserved0", 0 ),
	Boolean( "BOOLEAN", 1 ),
	Integer( "INTEGER", 2 ),
	BitString( "BIT STRING", 3 ),
	OctetString( "OCTET STRING", 4 ),
	Null( "NULL", 5 ),
	ObjectIdentifier( "OBJECT IDENTIFIER", 6 ),
	ObjectDescriptor( "ObjectDescriptor", 7 ),
	External( "EXTERNAL", 8 ),
	InstanceOf( "INSTANCE OF", 8 ),
	Real( "REAL", 9 ),
	Enumerated( "ENUMERATED", 10 ),
	EmbeddedPdv( "EMBEDDED PDV", 11 ),
	UTF8String( "UTF8String", 12, "UTF-8", null ),
	RelativeOid( "RELATIVE-OID", 13 ),
	Time( "TIME", 14 ),
	//Reserved1( "Reserved1", 15 ),
	Sequence( "Sequence", 16 ),
	Set( "Set", 17 ),
	NumericString( "NumericString", 18, "US-ASCII", Pattern.compile( "^[0-9 ]+$" ) ),
	PrintableString( "PrintableString", 19, "US-ASCII", Pattern.compile( "^[A-Za-z0-9 '()+,\\-/:=?]*$" ) ),
	T61String( "T61String", 20, "ISO-8859-1", null ),
	Teletex( "Teletex", 20, "ISO-8859-1", null ),//alias to T61String
	VideotexString( "VideotexString", 21, "ISO-8859-1", null ),
	IA5String( "IA5String", 22, "ISO-8859-1", null ),
	UTCTime( "UTCTime", 23 ),
	GeneralizedTime( "GeneralizedTime", 24 ),
	GraphicString( "GraphicString", 25, "ISO-8859-1", null ),
	VisibleString( "VisibleString", 26, "ISO-8859-1", null ),
	ISO646String( "ISO646String", 26, "ISO-8859-1", null ),// alias to VisibleString
	GeneralString( "GeneralString", 27, "ISO-8859-1", null ),
	UniversalString( "UniversalString", 28, "UTF-32BE", null ),
	CharacterString( "CHARACTER STRING", 29 ),
	BMPString( "BMPString", 30, "UTF-16BE", null ),
	Date( "DATE", 31 ),
	TimeOfDay( "TIME-OF-DAY", 32 ),
	DateTime( "DATE-TIME", 33 ),
	Duration( "DURATION", 34 ),
	OidIri( "OID-IRI", 35 ),
	RelativeOidIri( "RELATIVE-OID-IRI", 36 );

	private final TypeName typeName;
	private final int tagNumber;
	private final Ref<Type> ref;
	private final Charset charset;
	private final Pattern pattern;

	UniversalType( String typeName, int tagNumber )
	{
		this( typeName, tagNumber, (Charset)null, null );
	}

	UniversalType( String typeName, int tagNumber, @Nullable String charset, @Nullable Pattern pattern )
	{
		this( typeName, tagNumber, charset == null ? null : Charset.forName( charset ), pattern );
	}

	UniversalType( String typeName, int tagNumber, @Nullable Charset charset, @Nullable Pattern pattern )
	{
		this.typeName = new TypeName( typeName, null );
		this.tagNumber = tagNumber;
		this.charset = charset;
		this.pattern = pattern;
		ref = new TypeNameRef( this.typeName );
	}

	@Nullable
	public Charset charset()
	{
		return charset;
	}

	@Nullable
	public Pattern pattern()
	{
		return pattern;
	}

	@NotNull
	public TypeName typeName()
	{
		return typeName;
	}

	public int tagNumber()
	{
		return tagNumber;
	}

	public Ref<Type> ref()
	{
		return ref;
	}

	private boolean isTypeNameEqualTo( @NotNull String name )
	{
		if( name.equals( typeName() ) )
			return true;

		switch( this )
		{
			case BitString:
				return "BITSTRING".equals( name );

			case OctetString:
				return "OCTETSTRING".equals( name );

			case ObjectIdentifier:
				return "OBJECTIDENTIFIER".equals( name );

			case EmbeddedPdv:
				return "EMBEDDEDPDV".equals( name );

			case CharacterString:
				return "CHARACTERSTRING".equals( name );

			default:
				return false;
		}
	}

	public static UniversalType forTypeName( String typeName )
	{
		for( UniversalType type : values() )
			if( type.isTypeNameEqualTo( typeName ) )
				return type;

		throw new IllegalArgumentException( "Unable to find builtin type for name: " + typeName );
	}
}
