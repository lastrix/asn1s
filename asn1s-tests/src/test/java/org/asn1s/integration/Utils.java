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

package org.asn1s.integration;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class Utils
{
	@SuppressWarnings( "HardcodedFileSeparator" )
	private static final char WINDOWS_SEPARATOR_CHAR = '\\';
	@SuppressWarnings( "HardcodedFileSeparator" )
	private static final char UNIX_SEPARATOR_CHAR = '/';

	private Utils()
	{
	}

	static void createDataFromFolder( Collection<Object[]> list, String packageName, String prefix )
	{
		List<String> items = findFoldersInClassPathFolder( packageName );
		for( String item : items )
			list.add( new Object[]{'[' + prefix + ']' + new File( item ).getName(), item + UNIX_SEPARATOR_CHAR} );
	}

	private static List<String> findFoldersInClassPathFolder( @NotNull String packageName )
	{
		packageName = packageName.replace( '.', File.separatorChar );

		List<String> list = new ArrayList<>();
		File readme = new File( ConstraintSuiteTest.class.getResource( "/asn1s/tests/readme.txt" ).getFile() );
		File folder = readme.getParentFile().getParentFile().getParentFile();
		searchInPath( folder, packageName, list );
		return list;
	}

	static byte[] getResourceAsBytesOrDie( String resourceName )
	{
		try( InputStream is = SuiteTest.class.getResourceAsStream( resourceName ) )
		{
			if( is == null )
				throw new IllegalArgumentException( "Unable to locate resource: " + resourceName );

			return IOUtils.toByteArray( is );
		} catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
	}

	static String getResourceAsStringOrDie( String resourceName )
	{
		try( InputStream is = SuiteTest.class.getResourceAsStream( resourceName ) )
		{
			if( is == null )
				throw new IllegalArgumentException( "Unable to locate resource: " + resourceName );

			return IOUtils.toString( is, "UTF-8" );
		} catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
	}

	private static void searchInPath( File file, String path, Collection<String> list )
	{
		File dir = new File( file, path );
		if( !dir.exists() )
			return;

		File[] files = dir.listFiles();
		if( files == null )
			return;

		String rootPath = file.getAbsolutePath();
		for( File item : files )
			if( item.isDirectory() )
				list.add( createClassResourceName( rootPath, item.getAbsolutePath() ) );
	}

	private static String createClassResourceName( String rootPath, String item )
	{
		if( !item.startsWith( rootPath ) )
			throw new IllegalArgumentException();

		String value = item.substring( rootPath.length() ).replace( WINDOWS_SEPARATOR_CHAR, UNIX_SEPARATOR_CHAR );
		if( value.charAt( 0 ) != UNIX_SEPARATOR_CHAR )
			value = UNIX_SEPARATOR_CHAR + value;
		return value;
	}
}
