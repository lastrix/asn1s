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

package org.asn1s.core.type.x680.collection;

import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type.Family;

final class InterpolatorUtils
{
	private InterpolatorUtils()
	{
	}

	static void assertTags( NamedType component, Iterable<ComponentType> list ) throws ValidationException
	{
		if( component.getFamily() == Family.Choice && component.getEncoding( EncodingInstructions.Tag ) == null )
		{
			for( NamedType namedType : component.getNamedTypes() )
				assertTags( namedType, list );
		}
		else
		{
			TagEncoding encoding = (TagEncoding)component.getEncoding( EncodingInstructions.Tag );
			assertTagsImpl( component.getName(), encoding.getTagClass(), encoding.getTagNumber(), list );
		}
	}

	private static void assertTagsImpl( String name, TagClass tagClass, int tagNumber, Iterable<? extends NamedType> list ) throws ValidationException
	{
		for( NamedType component : list )
		{
			IEncoding enc = component.getEncoding( EncodingInstructions.Tag );
			if( enc == null )
			{
				if( component.getFamily() == Family.Choice )
					assertTagsImpl( name, tagClass, tagNumber, component.getNamedTypes() );

				throw new IllegalStateException();
			}
			else
			{
				TagEncoding encoding = (TagEncoding)enc;
				if( tagClass == encoding.getTagClass() && tagNumber == encoding.getTagNumber() )
					throw new ValidationException( "Duplicate tag detected for component '" + name + "' and '" + component.getName() + '\'' );
			}
		}
	}
}
