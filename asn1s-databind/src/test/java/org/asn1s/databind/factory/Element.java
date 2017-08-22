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

import org.apache.commons.lang3.StringUtils;
import org.asn1s.annotation.*;

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

	@Constructor
	public Element( @ConstructorParam( "name" ) String name )
	{
		super( name );
	}

	@Asn1Property( optional = true )
	@CollectionSettings( {Element.class, TextElement.class} )
	private List<AbstractElement> siblings;

	@Asn1Property( optional = true )
	private List<Attribute> attributes;

	public List<Attribute> getAttributes()
	{
		return attributes == null ? Collections.emptyList() : Collections.unmodifiableList( attributes );
	}

	public void setAttributes( List<Attribute> attributes )
	{
		this.attributes = new ArrayList<>( attributes );
	}

	public List<AbstractElement> getSiblings()
	{
		return siblings == null ? Collections.emptyList() : Collections.unmodifiableList( siblings );
	}

	public void setSiblings( List<AbstractElement> siblings )
	{
		this.siblings = new ArrayList<>( siblings );
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		appendToStringBuilder( sb, "" );
		return sb.toString();
	}

	@Override
	public void appendToStringBuilder( StringBuilder sb, String prefix )
	{
		sb.append( prefix ).append( '<' ).append( getName() );
		List<Attribute> attributes = getAttributes();
		if( !attributes.isEmpty() )
			sb.append( ' ' ).append( StringUtils.join( attributes, ' ' ) );
		sb.append( '>' ).append( System.lineSeparator() );
		String siblingPrefix = prefix + '\t';
		for( AbstractElement sibling : siblings )
			sibling.appendToStringBuilder( sb, siblingPrefix );
		sb.append( prefix ).append( "</" ).append( getName() ).append( '>' ).append( System.lineSeparator() );
	}
}
