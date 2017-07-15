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

package org.asn1s.schema;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenSource;
import org.asn1s.api.ObjectFactory;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.schema.parser.Asn1Lexer;
import org.asn1s.schema.parser.Asn1Parser;

import java.io.*;
import java.util.List;

public final class SchemaUtils
{
	private SchemaUtils()
	{
	}

	public static List<Module> parseModules( String content, ModuleResolver resolver, ObjectFactory objectFactory ) throws IOException
	{
		try( StringReader reader = new StringReader( content ) )
		{
			return parseModules( reader, resolver, objectFactory );
		}
	}

	public static List<Module> parseModules( File file, ModuleResolver resolver, ObjectFactory objectFactory ) throws IOException
	{
		try( FileInputStream is = new FileInputStream( file ) )
		{
			return parseModules( is, resolver, objectFactory );
		}
	}

	public static List<Module> parseModules( Reader reader, ModuleResolver resolver, ObjectFactory objectFactory ) throws IOException
	{
		return parseModules( new ANTLRInputStream( reader ), resolver, objectFactory );
	}

	public static List<Module> parseModules( InputStream is, ModuleResolver resolver, ObjectFactory objectFactory ) throws IOException
	{
		return parseModules( new ANTLRInputStream( is ), resolver, objectFactory );
	}

	public static List<Module> parseModules( ANTLRInputStream inputStream, ModuleResolver resolver, ObjectFactory objectFactory )
	{
		TokenSource lexer = new Asn1Lexer( inputStream );
		CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		Asn1Parser parser = new Asn1Parser( tokenStream, resolver, objectFactory );
		parser.addErrorListener( new Asn1ErrorListener() );
		parser.setBuildParseTree( false );
		return parser.startStmt().result;
	}

	public static Module parsePdu( String content, ModuleResolver resolver, ObjectFactory objectFactory ) throws IOException
	{
		try( Reader reader = new StringReader( content ) )
		{
			return parsePdu( reader, resolver, objectFactory );
		}
	}

	public static Module parsePdu( Reader reader, ModuleResolver resolver, ObjectFactory objectFactory ) throws IOException
	{
		return parsePdu( new ANTLRInputStream( reader ), resolver, objectFactory );
	}

	public static Module parsePdu( InputStream is, ModuleResolver resolver, ObjectFactory objectFactory ) throws IOException
	{
		return parsePdu( new ANTLRInputStream( is ), resolver, objectFactory );
	}

	public static Module parsePdu( ANTLRInputStream inputStream, ModuleResolver resolver, ObjectFactory objectFactory )
	{
		TokenSource lexer = new Asn1Lexer( inputStream );
		CommonTokenStream tokenStream = new CommonTokenStream( lexer );
		Asn1Parser parser = new Asn1Parser( tokenStream, resolver, objectFactory );
		parser.addErrorListener( new Asn1ErrorListener() );
		parser.setBuildParseTree( false );
		return parser.pduStmt().result;
	}
}
