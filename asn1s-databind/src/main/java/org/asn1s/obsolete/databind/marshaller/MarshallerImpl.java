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

package org.asn1s.obsolete.databind.marshaller;

import org.asn1s.api.type.DefinedType;
import org.asn1s.api.value.Value;
import org.asn1s.io.Asn1Writer;
import org.asn1s.io.ber.BerRules;
import org.asn1s.io.ber.output.DefaultBerWriter;
import org.asn1s.obsolete.databind.Asn1Context;
import org.asn1s.obsolete.databind.binder.Asn1ValueBinder;
import org.asn1s.obsolete.databind.binder.Asn1ValueBinderImpl;
import org.asn1s.obsolete.databind.mapper.MappedType;

import java.io.IOException;
import java.io.OutputStream;

public class MarshallerImpl implements Marshaller
{
	public MarshallerImpl( Asn1Context context )
	{
		this.context = context;
	}

	private final Asn1Context context;

	@Override
	public void marshall( Object o, OutputStream os ) throws IOException
	{
		MappedType type = context.getMappedTypeByClass( o.getClass() );
		if( type == null )
			throw new IllegalStateException( "No mapping for: " + o.getClass().getCanonicalName() );

		Asn1ValueBinder binder = new Asn1ValueBinderImpl( context );
		Value value = binder.toAsn1( o, type );
		if( value == null )
			throw new IllegalStateException( "Binder returned no value to write" );

		DefinedType asnType = type.getAsnType();
		try( Asn1Writer writer = new DefaultBerWriter( BerRules.DER, os ) )
		{
			writer.write( asnType.createScope(), asnType, value );
		} catch( Exception e )
		{
			throw new IOException( "Unable to write value: " + o, e );
		}
	}
}
