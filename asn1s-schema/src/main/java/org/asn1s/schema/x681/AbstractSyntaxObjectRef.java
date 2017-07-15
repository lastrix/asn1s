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

package org.asn1s.schema.x681;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.type.ClassType;
import org.asn1s.api.type.DefinedType;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.x681.ObjectValue;

import java.util.Map;

public class AbstractSyntaxObjectRef implements Ref<Value>
{
	public AbstractSyntaxObjectRef( String abstractSyntax )
	{
		abstractSyntax = abstractSyntax.trim();
		if( !abstractSyntax.startsWith( "{" ) && !abstractSyntax.endsWith( "}" ) )
			throw new IllegalArgumentException( "Not valid abstract syntax: " + abstractSyntax );
		this.abstractSyntax = abstractSyntax.substring( 1, abstractSyntax.length() - 1 );
	}

	private final String abstractSyntax;

	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		Type type = scope.getTypeOrDie();
		while( type instanceof DefinedType )
			type = type.getSibling();
		assert type instanceof ClassType;
		Module module = scope.getModule();
		ModuleResolver resolver = module.getModuleResolver();
		assert resolver != null;
		ClassType classType = (ClassType)type;
		try
		{
			AbstractSyntaxParser parser = new AbstractSyntaxParser( resolver, resolver.createObjectFactory(), module, classType );
			Map<String, Ref<?>> result = parser.parse( abstractSyntax );
			return new ObjectValue( result );
		} catch( Exception e )
		{
			throw new ResolutionException( "Unable to unwrap abstract syntax: " + abstractSyntax, e );
		}
	}
}
