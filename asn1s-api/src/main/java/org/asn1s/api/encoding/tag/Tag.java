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

package org.asn1s.api.encoding.tag;


@SuppressWarnings( "ClassNamingConvention" )
public final class Tag
{
	public Tag( TagEncoding encoding, boolean constructed )
	{
		this( encoding.getTagClass(), constructed, encoding.getTagNumber() );
	}

	public Tag( TagClass tagClass, boolean constructed, int tagNumber )
	{
		this.tagClass = tagClass;
		this.constructed = constructed;
		this.tagNumber = tagNumber;
	}

	private final TagClass tagClass;
	private final boolean constructed;
	private final int tagNumber;

	public TagClass getTagClass()
	{
		return tagClass;
	}

	public boolean isConstructed()
	{
		return constructed;
	}

	public int getTagNumber()
	{
		return tagNumber;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Tag ) ) return false;

		Tag tag = (Tag)obj;

		if( isConstructed() != tag.isConstructed() ) return false;
		//noinspection SimplifiableIfStatement
		if( getTagNumber() != tag.getTagNumber() ) return false;
		return getTagClass() == tag.getTagClass();
	}

	@Override
	public int hashCode()
	{
		int result = getTagClass().hashCode();
		result = 31 * result + ( isConstructed() ? 1 : 0 );
		result = 31 * result + getTagNumber();
		return result;
	}

	public void assertEoc()
	{
		if( !isEoc() )
			throw new IllegalStateException( "EOC required" );
	}

	public boolean isEoc()
	{
		return tagClass == TagClass.Universal && !constructed && tagNumber == 0;
	}

	@Override
	public String toString()
	{
		return "Tag{" +
				"tagClass=" + tagClass +
				", constructed=" + constructed +
				", tagNumber=" + tagNumber +
				'}';
	}
}
