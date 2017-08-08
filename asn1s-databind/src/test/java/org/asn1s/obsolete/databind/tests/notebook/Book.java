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

import java.util.ArrayList;
import java.util.List;

@Asn1Type( name = "Book" )
public class Book
{
	@Constructor
	public Book( @ConstructorParam( "author" ) String author )
	{
		this.author = author;
		notes = new ArrayList<>();
	}

	@Property
	private final String author;

	@Property( typeName = "NoteList" )
	private List<Note> notes;


	public String getAuthor()
	{
		return author;
	}

	public void setNotes( List<Note> notes )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.notes = notes;
	}

	public List<Note> getNotes()
	{
		return notes;
	}

	public void addNote( Note note )
	{
		notes.add( note );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Book ) ) return false;

		Book book = (Book)obj;

		//noinspection SimplifiableIfStatement
		if( !getAuthor().equals( book.getAuthor() ) ) return false;
		return getNotes() != null ? getNotes().equals( book.getNotes() ) : book.getNotes() == null;
	}

	@Override
	public int hashCode()
	{
		int result = getAuthor().hashCode();
		result = 31 * result + ( getNotes() != null ? getNotes().hashCode() : 0 );
		return result;
	}

	@Override
	public String toString()
	{
		return "Book{" +
				"author='" + author + '\'' +
				'}';
	}
}
