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

package org.asn1s.api.encoding.tag;

/**
 * Tag class as specified by X.680-201508 p. 8.1
 */
public enum TagClass
{
	Universal( 0 ),
	Application( 1 << 6 ),
	Private( 3 << 6 ),
	ContextSpecific( 2 << 6 );

	TagClass( int code )
	{
		//noinspection NumericCastThatLosesPrecision
		this.code = (byte)code;
	}

	private final byte code;

	public static TagClass find( String value )
	{
		for( TagClass aClass : values() )
		{
			if( aClass.name().equalsIgnoreCase( value ) )
				return aClass;
		}

		throw new IllegalArgumentException( "Invalid tag class: " + value );
	}

	public static TagClass findByCode( byte code )
	{
		for( TagClass aClass : values() )
			if( code == aClass.getCode() )
				return aClass;
		throw new IllegalArgumentException( "Unknown code: " + code );
	}

	public int getCode()
	{
		return code;
	}
}
