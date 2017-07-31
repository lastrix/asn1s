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

package org.asn1s.api.type.x681;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.type.Type;
import org.asn1s.api.type.x681.ClassFieldType.Kind;
import org.asn1s.api.util.RefUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ScopeClassFieldRef implements Ref<Type>
{
	public ScopeClassFieldRef( String fieldName )
	{
		this.fieldName = fieldName;
	}

	private final String fieldName;

	@Override
	public Type resolve( Scope scope ) throws ResolutionException
	{
		ClassType classType = resolveClassTypeFromScope( scope );
		return new ClassPathResolver( classType, Arrays.asList( fieldName.split( "\\." ) ) ).resolve();
	}

	private static ClassType resolveClassTypeFromScope( Scope scope ) throws ResolutionException
	{
		Type expectedClassFieldType = scope.getTypeOrDie();
		if( !( expectedClassFieldType instanceof ClassFieldType ) )
			throw new ResolutionException( "Scope has no ClassFieldType" );

		ClassType parent = ( (ClassFieldType<?>)expectedClassFieldType ).getParent();
		if( !parent.isValidated() )
			RefUtils.resolutionValidate( scope, parent );
		return parent;
	}

	private static final class ClassPathResolver
	{
		private ClassPathResolver( ClassType rootClass, List<String> pathParts )
		{
			this.rootClass = rootClass;
			this.pathParts = pathParts;
		}

		private final ClassType rootClass;
		private final List<String> pathParts;
		private int position;
		private Type currentType;

		@NotNull
		Type resolve() throws ResolutionException
		{
			position = 0;
			currentType = rootClass;
			while( position < pathParts.size() )
				navigatePathPart();

			if( !isTypeField() )
				throw new ResolutionException( "Unable to resolve FieldType by " + StringUtils.join( pathParts, '.' ) );
			return currentType;
		}

		private void navigatePathPart() throws ResolutionException
		{
			String part = pathParts.get( position );
			currentType = currentType.getNamedType( part );

			if( currentType == null )
				throw new ResolutionException( "Unable to resolve path part: " + part + " from " + StringUtils.join( pathParts, '.' ) );

			position++;
		}

		private boolean isTypeField()
		{
			return currentType instanceof ClassFieldType
					&& ( (ClassFieldType<?>)currentType ).getClassFieldKind() == Kind.TYPE;
		}
	}
}
