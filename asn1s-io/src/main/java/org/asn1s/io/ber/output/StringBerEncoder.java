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

package org.asn1s.io.ber.output;

import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.StringType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.StringValue;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;

public class StringBerEncoder implements BerEncoder
{
	@Override
	public void encode( @NotNull BerWriter os, @NotNull Scope scope, @NotNull Type type, @NotNull Value value, boolean writeHeader ) throws IOException, Asn1Exception
	{
		if( !( type instanceof StringType ) )
			throw new IllegalStateException();

		Tag tag = ( (TagEncoding)type.getEncoding( EncodingInstructions.Tag ) ).toTag( false );

		if( value.getKind() == Kind.CString )
			writeString( os, ( (StringType)type ).getCharset(), tag, value.toStringValue().asString(), writeHeader );
		else if( value.getKind() == Kind.Collection )
		{
			StringValue stringValue = ( (StringType)type ).tryBuildStringValue( scope, value.toValueCollection() );
			if( stringValue != null )
				writeString( os, ( (StringType)type ).getCharset(), tag, stringValue.asString(), writeHeader );
			else
				throw new IllegalValueException( "Unable to write value as string: " + value );
		}
		else
			throw new IllegalValueException( "Unable to write value as string: " + value );
	}

	static void writeString( BerWriter os, Charset charset, Tag tag, String value, boolean writeHeader ) throws IOException
	{
		byte[] content = value.getBytes( charset );

		if( writeHeader )
			os.writeHeader( tag, content.length );

		os.write( content );
	}
}
