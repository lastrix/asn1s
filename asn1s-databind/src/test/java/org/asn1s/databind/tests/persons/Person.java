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

package org.asn1s.databind.tests.persons;

import org.apache.commons.lang3.ArrayUtils;
import org.asn1s.annotation.Constructor;
import org.asn1s.annotation.ConstructorParam;
import org.asn1s.annotation.Property;
import org.asn1s.annotation.Sequence;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Sequence( name = "Person" )
public final class Person
{
	public Person()
	{
	}

	public Person( String name, String surname, int age )
	{
		this( name, surname, age, null );
	}

	@Constructor
	public Person(
			@ConstructorParam( "name" ) String name,
			@ConstructorParam( "surname" ) String surname,
			@ConstructorParam( "age" ) int age,
			@ConstructorParam( "family" ) @Nullable Person[] family )
	{
		this.name = name;
		this.surname = surname;
		this.age = age;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.family = family;
	}

	@Property
	private String name;

	@Property
	private String surname;

	@Property
	private int age;

	@Property( optional = true, typeName = "Person-Array" )
	private Person[] family;

	public String getName()
	{
		return name;
	}

	public String getSurname()
	{
		return surname;
	}

	public int getAge()
	{
		return age;
	}

	public Person[] getFamily()
	{
		return family;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setSurname( String surname )
	{
		this.surname = surname;
	}

	public void setAge( int age )
	{
		this.age = age;
	}

	public void setFamily( Person[] family )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		this.family = family;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Person ) ) return false;

		Person person = (Person)obj;

		if( getAge() != person.getAge() ) return false;
		if( !getName().equals( person.getName() ) ) return false;
		if( !getSurname().equals( person.getSurname() ) ) return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals( getFamily(), person.getFamily() );
	}

	@Override
	public int hashCode()
	{
		int result = getName().hashCode();
		result = 31 * result + getSurname().hashCode();
		result = 31 * result + getAge();
		result = 31 * result + Arrays.hashCode( getFamily() );
		return result;
	}

	public void toPrintableString( StringBuilder sb, String prefix )
	{
		sb.append( prefix ).append( '{' ).append( System.lineSeparator() );
		String sPrefix = prefix + '\t';
		sb
				.append( sPrefix ).append( "name: " ).append( getName() ).append( ',' ).append( System.lineSeparator() )
				.append( sPrefix ).append( "surname: " ).append( getSurname() ).append( ',' ).append( System.lineSeparator() )
				.append( sPrefix ).append( "age: " ).append( getAge() ).append( ',' ).append( System.lineSeparator() );
		if( ArrayUtils.isEmpty( family ) )
			sb.append( sPrefix ).append( "family: []" ).append( System.lineSeparator() );
		else
		{
			sb.append( sPrefix ).append( "family: [" ).append( System.lineSeparator() );
			String ssPrefix = sPrefix + '\t';
			boolean first = true;
			for( Person person : family )
			{
				if( first )
					first = false;
				else
					sb.append( ',' ).append( System.lineSeparator() );
				person.toPrintableString( sb, ssPrefix );
			}
			sb.append( System.lineSeparator() ).append( sPrefix ).append( ']' ).append( System.lineSeparator() );
		}
		sb.append( prefix ).append( '}' );
	}

	@Override
	public String toString()
	{
		return "Person{" +
				"name='" + name + '\'' +
				", surname='" + surname + '\'' +
				", age=" + age +
				'}';
	}
}
