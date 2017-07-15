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

package org.asn1s.databind.unmarshaller;

import org.asn1s.api.type.DefinedType;
import org.asn1s.api.value.Value;
import org.asn1s.databind.Asn1Context;
import org.asn1s.databind.binder.JavaValueBinder;
import org.asn1s.databind.binder.JavaValueBinderImpl;
import org.asn1s.databind.mapper.MappedType;
import org.asn1s.io.Asn1Reader;
import org.asn1s.io.ber.input.DefaultBerReader;

import java.io.IOException;
import java.io.InputStream;

public class UnmarshallerImpl implements Unmarshaller
{
	public UnmarshallerImpl( Asn1Context context )
	{
		this.context = context;
	}

	private final Asn1Context context;

	@Override
	public <T> T unmarshal( Class<T> aClass, InputStream is ) throws IOException
	{
		MappedType mappedType = context.getMappedTypeByClass( aClass );
		if( mappedType == null )
			throw new IllegalStateException( "No mapping for class: " + aClass );

		DefinedType asnType = mappedType.getAsnType();
		Value value;
		try( Asn1Reader reader = new DefaultBerReader( is, context.getObjectFactory() ) )
		{
			value = reader.read( asnType.createScope(), asnType );
		} catch( Exception e )
		{
			throw new IOException( "Unable to read value of class: " + aClass.getCanonicalName(), e );
		}
		JavaValueBinder binder = new JavaValueBinderImpl( context );
		return binder.toJava( value, mappedType );
	}
}
