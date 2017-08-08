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

package org.asn1s.obsolete.databind.tests.chat;

import org.asn1s.annotation.Asn1Type;
import org.asn1s.annotation.Constructor;
import org.asn1s.annotation.ConstructorParam;
import org.asn1s.annotation.Property;

import java.time.Instant;

@Asn1Type( name = "Message" )
public class Message
{
	@Property( optional = true )
	private Integer id;

	@Property( typeName = "GeneralizedTime" )
	private Instant stamp;

	private User user;

	@Property
	private String text;

	public Message()
	{
	}

	@Constructor
	public Message(
			@ConstructorParam( value = "manager", global = true ) ObjectManager manager,
			@ConstructorParam( "id" ) Integer id,
			@ConstructorParam( "stamp" ) Instant stamp,
			@ConstructorParam( "userId" ) int userId,
			@ConstructorParam( "text" ) String text )
	{
		this.id = id;
		this.stamp = stamp;
		user = manager.getUser( userId );
		this.text = text;
	}

	public Message( Integer id, User user, String text )
	{
		this.id = id;
		this.user = user;
		this.text = text;
		stamp = Instant.now();
	}

	public Integer getId()
	{
		return id;
	}

	public void setId( int id )
	{
		this.id = id;
	}

	public Instant getStamp()
	{
		return stamp;
	}

	public void setStamp( Instant stamp )
	{
		this.stamp = stamp;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser( User user )
	{
		this.user = user;
	}

	@Property
	public int getUserId()
	{
		return user.getId();
	}

	public String getText()
	{
		return text;
	}

	public void setText( String text )
	{
		this.text = text;
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof Message ) ) return false;

		Message message = (Message)obj;

		if( getId() != null ? !getId().equals( message.getId() ) : message.getId() != null ) return false;
		if( !getStamp().equals( message.getStamp() ) ) return false;
		//noinspection SimplifiableIfStatement
		if( !getUser().equals( message.getUser() ) ) return false;
		return getText().equals( message.getText() );
	}

	@Override
	public int hashCode()
	{
		int result = getId() != null ? getId().hashCode() : 0;
		result = 31 * result + getStamp().hashCode();
		result = 31 * result + getUser().hashCode();
		result = 31 * result + getText().hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		return "Message{" +
				"id=" + id +
				", stamp=" + stamp +
				", userId=" + user.getId() +
				", text='" + text + '\'' +
				'}';
	}
}
