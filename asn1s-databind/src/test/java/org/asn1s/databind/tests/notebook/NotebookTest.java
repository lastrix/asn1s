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

package org.asn1s.databind.tests.notebook;


import org.asn1s.api.ModuleReference;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.core.DefaultObjectFactory;
import org.asn1s.core.module.ModuleImpl;
import org.asn1s.core.module.ModuleSet;
import org.asn1s.databind.Asn1Context;
import org.asn1s.databind.mapper.MappedType;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;

public class NotebookTest
{
	@Test
	public void test() throws Exception
	{
		ModuleSet moduleSet = new ModuleSet();
		ModuleImpl module = new ModuleImpl( new ModuleReference( "My-Module" ), moduleSet );
		module.setTagMethod( TagMethod.Automatic );
		Asn1Context context = new Asn1Context( module, new DefaultObjectFactory( moduleSet ) );
		context.mapType( Note.class );
		MappedType mappedType = context.mapType( Book.class );
		module.validate();

		Book book = new Book( "lastrix" );
		book.addNote( new Note( Instant.now(), "Hello", "I'm new here!" ) );
		book.addNote( new Note( Instant.now(), "Whats going on?", "Testing you." ) );

		byte[] bytes;
		try( ByteArrayOutputStream os = new ByteArrayOutputStream() )
		{
			context.createMarshaller().marshall( book, os );
			bytes = os.toByteArray();
		}

		Book result;
		try( ByteArrayInputStream is = new ByteArrayInputStream( bytes ) )
		{
			result = context.createUnmarshaller().unmarshal( Book.class, is );
		}

		Assert.assertEquals( "Objects are not equal", book, result );
	}
}
