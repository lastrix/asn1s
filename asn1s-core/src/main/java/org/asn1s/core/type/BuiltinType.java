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

package org.asn1s.core.type;

import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;

import java.util.EnumMap;
import java.util.Map;

public abstract class BuiltinType extends AbstractType
{
	protected BuiltinType()
	{
	}

	private final Map<EncodingInstructions, IEncoding> encodingMap = new EnumMap<>( EncodingInstructions.class );

	@Override
	public IEncoding getEncoding( EncodingInstructions instructions )
	{
		return encodingMap.get( instructions );
	}

	protected void setEncoding( IEncoding encoding )
	{
		encodingMap.put( encoding.getEncodingInstructions(), encoding );
	}

	@Override
	public final boolean equals( Object obj )
	{
		return obj == this || obj instanceof BuiltinType && toString().equals( obj.toString() );
	}

	@Override
	public final int hashCode()
	{
		return toString().hashCode();
	}
}
