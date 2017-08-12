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

package org.asn1s.databind.factory;

import org.asn1s.annotation.Asn1ElementTypes;
import org.asn1s.annotation.Asn1Property;
import org.asn1s.annotation.Asn1Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings( "ALL" )
@Asn1Type( name = "Element" )
public final class Element extends AbstractElement
{
	public Element()
	{
	}

	public Element( String name )
	{
		super( name );
	}

	@Asn1Property( optional = true )
	@Asn1ElementTypes( {Element.class, TextElement.class} )
	private List<TextElement> siblings;

	@Asn1Property( optional = true )
	private List<Attribute> attributes;

	public List<Attribute> getAttributes()
	{
		return Collections.unmodifiableList( attributes );
	}

	public void setAttributes( List<Attribute> attributes )
	{
		this.attributes = new ArrayList<>( attributes );
	}

	public List<TextElement> getSiblings()
	{
		return Collections.unmodifiableList( siblings );
	}

	public void setSiblings( List<TextElement> siblings )
	{
		this.siblings = new ArrayList<>( siblings );
	}
}
