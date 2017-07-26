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

package org.asn1s.io.ber.input;

import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.Type.Family;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

final class BerDecoderUtils
{
	private BerDecoderUtils()
	{
	}

	private static boolean findChoiceComponent( Tag tag, Type type )
	{
		List<? extends NamedType> types = type.getNamedTypes();
		for( NamedType namedType : types )
			if( isComponentTag( tag, namedType ) )
				return true;

		return false;
	}

	static boolean isComponentTag( Tag tag, NamedType namedType )
	{
		TagEncoding encoding = (TagEncoding)namedType.getEncoding( EncodingInstructions.TAG );
		if( encoding != null && encoding.isEqualToTag( tag ) )
			return true;

		if( namedType.getFamily() == Family.CHOICE )
		{
			if( findChoiceComponent( tag, namedType ) )
				return true;
		}
		return false;
	}

	static byte[] readString( @NotNull AbstractBerReader reader, int length ) throws IOException
	{
		byte[] content = new byte[length];
		if( reader.read( content ) != length )
			throw new IOException( "Unexpected EOF" );
		return content;
	}
}
