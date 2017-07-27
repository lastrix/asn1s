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

import org.asn1s.api.Scope;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.ValueFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class ReaderContext
{
	public ReaderContext( AbstractBerReader reader )
	{
		this.reader = reader;
	}

	public ReaderContext( AbstractBerReader reader, @NotNull Scope scope, @NotNull Type type, @Nullable Tag tag, int length, boolean implicit )
	{
		this.reader = reader;
		this.scope = scope;
		this.type = type;
		this.tag = tag;
		this.length = length;
		this.implicit = implicit;
	}

	private final AbstractBerReader reader;
	private Scope scope;
	private Type type;
	private Tag tag;
	private int length;
	private boolean implicit;

	public AbstractBerReader getReader()
	{
		return reader;
	}

	public ValueFactory getValueFactory()
	{
		return reader.getValueFactory();
	}

	public Scope getScope()
	{
		return scope;
	}

	boolean hasTag()
	{
		return tag != null;
	}

	boolean isSameTagEncoding( TagEncoding encoding )
	{
		return encoding.getTagClass() == tag.getTagClass() && encoding.getTagNumber() == tag.getTagNumber();
	}

	public void setScope( @NotNull Scope scope )
	{
		this.scope = scope;
	}

	public Type getType()
	{
		return type;
	}

	public void setType( @NotNull Type type )
	{
		this.type = type;
	}

	public Tag getTag()
	{
		return tag;
	}

	public int getLength()
	{
		return length;
	}

	boolean isImplicit()
	{
		return implicit;
	}

	void setImplicit( boolean implicit )
	{
		this.implicit = implicit;
	}

	void resetTagInfo( boolean implicit )
	{
		tag = null;
		length = -1;
		this.implicit = implicit;
	}

	void readTagInfo( boolean assertTagValue ) throws IOException
	{
		tag = reader.readTag();
		length = reader.readLength();
		if( assertTagValue )
			assertTag( type, tag );
	}

	boolean readTagInfoEocPossible( boolean definite ) throws IOException
	{
		tag = reader.readTag();
		length = reader.readLength();
		return !definite && tag.isEoc() && length == 0;

	}

	private static void assertTag( @NotNull Type type, @Nullable Tag tag ) throws IOException
	{
		if( tag == null )
			throw new IllegalStateException();

		TagEncoding encoding = (TagEncoding)type.getEncoding( EncodingInstructions.TAG );
		if( encoding == null )
			throw new IllegalStateException( "No encoding for type: " + type );

		if( tag.getTagClass() != encoding.getTagClass() || tag.getTagNumber() != encoding.getTagNumber() )
			throw new IOException( "Invalid tag: " + tag );
	}

	ReaderContext toSiblingContext()
	{
		type = type.getSibling();
		assert type != null;
		scope = type.getScope( scope );
		return this;
	}

	ReaderContext toSiblingContext( @NotNull Type componentType )
	{
		return new ReaderContext( reader, componentType.getScope( scope ), componentType, tag, length, implicit );
	}

	private ReaderContext toSiblingContext( @NotNull Type componentType, Tag tag, int length )
	{
		return new ReaderContext( reader, componentType.getScope( scope ), componentType, tag, length, false );
	}

	Value readComponentType( @NotNull Type componentType, Tag tag, int length ) throws IOException, Asn1Exception
	{
		return reader.readInternal( toSiblingContext( componentType, tag, length ) );
	}

	public byte read() throws IOException
	{
		return reader.read();
	}

	public int read( byte[] buffer ) throws IOException
	{
		return reader.read( buffer );
	}

	int position()
	{
		return reader.position();
	}

	Value readInternal( @NotNull ReaderContext context ) throws IOException, Asn1Exception
	{
		return reader.readInternal( context );
	}

	void ensureConstructedRead( int start, int length, @Nullable Tag tag ) throws IOException
	{
		reader.ensureConstructedRead( start, length, tag );
	}

	void skipToEoc() throws IOException
	{
		reader.skipToEoc();
	}

	void skip( int amount ) throws IOException
	{
		reader.skip( amount );
	}

	public ReaderContext copy()
	{
		return new ReaderContext( reader, scope, type, tag, length, implicit );
	}
}
