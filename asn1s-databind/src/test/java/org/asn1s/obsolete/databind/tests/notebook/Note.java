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

package org.asn1s.obsolete.databind.tests.notebook;

import org.asn1s.annotation.Asn1Type;
import org.asn1s.annotation.Constructor;
import org.asn1s.annotation.ConstructorParam;
import org.asn1s.annotation.Property;

import java.time.Instant;

@Asn1Type( name = "Note" )
public final class Note
{
	@Constructor
	public Note(
			@ConstructorParam( "stamp" ) Instant stamp,
			@ConstructorParam( "title" ) String title,
			@ConstructorParam( "message" ) String message )
	{
		this.stamp = stamp;
		this.title = title;
		this.message = message;
	}

	@Property( typeName = "GeneralizedTime" )
	private final Instant stamp;

	@Property
	private final String title;

	@Property
	private final String message;

	public Instant getStamp()
	{
		return stamp;
	}

	public String getTitle()
	{
		return title;
	}

	public String getMessage()
	{
		return message;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Note ) ) return false;

		Note note = (Note)obj;

		if( !getStamp().equals( note.getStamp() ) ) return false;
		//noinspection SimplifiableIfStatement
		if( !getTitle().equals( note.getTitle() ) ) return false;
		return getMessage().equals( note.getMessage() );
	}

	@Override
	public int hashCode()
	{
		int result = getStamp().hashCode();
		result = 31 * result + getTitle().hashCode();
		result = 31 * result + getMessage().hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return "Note{" +
				"stamp=" + stamp +
				", title='" + title + '\'' +
				", message='" + message + '\'' +
				'}';
	}
}
