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
import org.asn1s.api.util.EncodingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

public enum UniversalType
{
	//Reserved0( "Reserved0", 0 ),
	BOOLEAN( "BOOLEAN", 1 ),
	INTEGER( "INTEGER", 2 ),
	BIT_STRING( "BIT STRING", 3 ),
	OCTET_STRING( "OCTET STRING", 4 ),
	NULL( "NULL", 5 ),
	OBJECT_IDENTIFIER( "OBJECT IDENTIFIER", 6 ),
	OBJECT_DESCRIPTOR( "ObjectDescriptor", 7 ),
	EXTERNAL( "EXTERNAL", 8 ),
	INSTANCE_OF( "INSTANCE OF", 8 ),
	REAL( "REAL", 9 ),
	ENUMERATED( "ENUMERATED", 10 ),
	EMBEDDED_PDV( "EMBEDDED PDV", 11 ),
	UTF8_STRING( "UTF8String", 12, "UTF-8", null ),
	RELATIVE_OID( "RELATIVE-OID", 13 ),
	TIME( "TIME", 14 ),
	//Reserved1( "Reserved1", 15 ),
	SEQUENCE( "Sequence", 16 ),
	SET( "Set", 17 ),
	NUMERIC_STRING( "NumericString", 18, "US-ASCII", Pattern.compile( "^[0-9 ]+$" ) ),
	PRINTABLE_STRING( "PrintableString", 19, "US-ASCII", Pattern.compile( "^[A-Za-z0-9 '()+,\\-/:=?]*$" ) ),
	T61_STRING( "T61String", 20, EncodingUtils.ISO_8859_1, null ),
	TELETEX( "Teletex", 20, EncodingUtils.ISO_8859_1, null ),//alias to T61String
	VIDEOTEX_STRING( "VideotexString", 21, EncodingUtils.ISO_8859_1, null ),
	IA5_STRING( "IA5String", 22, EncodingUtils.ISO_8859_1, null ),
	UTC_TIME( "UTCTime", 23 ),
	GENERALIZED_TIME( "GeneralizedTime", 24 ),
	GRAPHIC_STRING( "GraphicString", 25, EncodingUtils.ISO_8859_1, null ),
	VISIBLE_STRING( "VisibleString", 26, EncodingUtils.ISO_8859_1, null ),
	ISO_646_STRING( "ISO646String", 26, EncodingUtils.ISO_8859_1, null ),// alias to VisibleString
	GENERAL_STRING( "GeneralString", 27, EncodingUtils.ISO_8859_1, null ),
	UNIVERSAL_STRING( "UniversalString", 28, "UTF-32BE", null ),
	CHARACTER_STRING( "CHARACTER STRING", 29 ),
	BMP_STRING( "BMPString", 30, "UTF-16BE", null ),
	DATE( "DATE", 31 ),
	TIME_OF_DAY( "TIME-OF-DAY", 32 ),
	DATE_TIME( "DATE-TIME", 33 ),
	DURATION( "DURATION", 34 ),
	OID_IRI( "OID-IRI", 35 ),
	RELATIVE_OID_IRI( "RELATIVE-OID-IRI", 36 );

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
		if( name.equals( typeName().getName() ) )
			return true;

		switch( this )
		{
			case BIT_STRING:
				return "BITSTRING".equals( name );

			case OCTET_STRING:
				return "OCTETSTRING".equals( name );

			case OBJECT_IDENTIFIER:
				return "OBJECTIDENTIFIER".equals( name );

			case EMBEDDED_PDV:
				return "EMBEDDEDPDV".equals( name );

			case CHARACTER_STRING:
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
