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

package org.asn1s.io.ber.output;

import org.asn1s.api.Scope;
import org.asn1s.api.encoding.tag.Tag;
import org.asn1s.api.exception.Asn1Exception;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.type.ComponentType;
import org.asn1s.api.type.NamedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x680.NamedValue;
import org.asn1s.io.ber.BerRules;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WriterContext
{
	public WriterContext( AbstractBerWriter writer )
	{
		this.writer = writer;
	}

	public WriterContext( AbstractBerWriter writer, Scope scope, Type type, Value value, boolean writeHeader )
	{
		this.writer = writer;
		this.scope = scope;
		this.type = type;
		this.value = value;
		this.writeHeader = writeHeader;
	}

	private final AbstractBerWriter writer;
	private Scope scope;
	private Type type;
	private Value value;
	private boolean writeHeader;

	public AbstractBerWriter getWriter()
	{
		return writer;
	}

	public Scope getScope()
	{
		return scope;
	}

	public void setScope( Scope scope )
	{
		this.scope = scope;
	}

	public Type getType()
	{
		return type;
	}

	public void setType( Type type )
	{
		this.type = type;
	}

	public Value getValue()
	{
		return value;
	}

	public void setValue( Value value )
	{
		this.value = value;
	}

	public boolean isWriteHeader()
	{
		return writeHeader;
	}

	public void setWriteHeader( boolean writeHeader )
	{
		this.writeHeader = writeHeader;
	}

	public WriterContext narrow() throws Asn1Exception
	{
		if( type instanceof ComponentType && !( (ComponentType)type ).isDummy() )
		{
			if( value.getKind() == Kind.Name )
			{

				if( !value.toNamedValue().getName().equals( ( (NamedType)type ).getName() ) )
					throw new IllegalValueException( "Named value has illegal name: " + value.toNamedValue().getName() + ". Expected: " + ( (NamedType)type ).getName() );

				type = type.getSibling();
				assert type != null;
				scope = type.getScope( scope );
				//noinspection ConstantConditions
				value = value.toNamedValue().getValueRef().resolve( scope );
			}
			else
			{
				type = type.getSibling();
				assert type != null;
				scope = type.getScope( scope );
			}
			return narrow();
		}

		if( type.hasConstraint() )
		{
			type = type.getSibling();
			assert type != null;
			scope = type.getScope( scope );
			return narrow();
		}

		return this;
	}

	public WriterContext toComponentContext( ComponentType componentType, @NotNull NamedValue namedValue ) throws IllegalValueException
	{
		return new WriterContext( writer, componentType.getScope( scope ), componentType, namedValue, true );
	}

	public WriterContext toSiblingContext()
	{
		return toSiblingContext( isWriteHeader() );
	}

	public WriterContext toSiblingContext( boolean writeHeader )
	{
		this.writeHeader = writeHeader;
		type = type.getSibling();
		assert type != null;
		scope = type.getScope( scope );
		return this;
	}

	public WriterContext toSiblingContext( @NotNull Type type, @NotNull Value value, boolean writeHeader )
	{
		this.writeHeader = writeHeader;
		this.type = type;
		scope = type.getScope( scope );
		this.value = value;
		return this;
	}

	public BerRules getRules()
	{
		return writer.getRules();
	}

	public boolean isBufferingAvailable()
	{
		return writer.isBufferingAvailable();
	}

	public void startBuffer( int sizeHint )
	{
		writer.startBuffer( sizeHint );
	}

	public void stopBuffer( @NotNull Tag tag ) throws IOException
	{
		writer.stopBuffer( tag );
	}

	public void write( int aByte ) throws IOException
	{
		writer.write( aByte );
	}

	public void write( byte[] bytes ) throws IOException
	{
		writer.write( bytes );
	}

	public void writeComponent( ComponentType component, NamedValue value ) throws Asn1Exception, IOException
	{
		writer.writeInternal( toComponentContext( component, value ) );
	}

	public void writeHeader( Tag tag, int length ) throws IOException
	{
		writer.writeHeader( tag, length );
	}

	public void writeString( byte[] content, Tag tag ) throws IOException
	{
		if( isWriteHeader() )
			writeHeader( tag, content.length );
		write( content );
	}

	public void writeInternal() throws IOException, Asn1Exception
	{
		writer.writeInternal( this );
	}
}
