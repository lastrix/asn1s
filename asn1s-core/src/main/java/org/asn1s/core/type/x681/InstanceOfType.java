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

package org.asn1s.core.type.x681;

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.tag.TagEncoding;
import org.asn1s.api.encoding.tag.TagMethod;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.*;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.core.type.TaggedTypeImpl;
import org.asn1s.core.type.x680.collection.SequenceType;
import org.jetbrains.annotations.NotNull;

public class InstanceOfType extends SequenceType
{
	public InstanceOfType( Ref<Type> classTypeRef )
	{
		super( true );
		this.classTypeRef = classTypeRef;
		createComponents( classTypeRef );
		setEncoding( TagEncoding.universal( UniversalType.INSTANCE_OF ) );
	}

	private void createComponents( Ref<Type> classTypeRef )
	{
		addComponent( ComponentType.Kind.PRIMARY, "type-id", new ClassFieldRef( classTypeRef, "&id" ) );
		addComponent( ComponentType.Kind.PRIMARY, "value", new TaggedTypeImpl( TagEncoding.context( 0, TagMethod.EXPLICIT ), new ClassFieldRef( classTypeRef, "&Type" ) ) );
	}

	private final Ref<Type> classTypeRef;
	private ClassType classType;

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Kind.OBJECT )
			classType.accept( scope, valueRef );
		else
			super.accept( scope, valueRef );
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ValidationException, ResolutionException
	{
		super.onValidate( scope );
		Type type = classTypeRef.resolve( scope );
		if( type.getFamily() != Family.OBJECT_CLASS )
			throw new ValidationException( "Is not ObjectClass: " + type );

		while( type instanceof DefinedType )
			type = type.getSibling();

		if( !( type instanceof ClassType ) )
			throw new ValidationException( "Unable to find class type" );

		classType = (ClassType)type;
	}

	@Override
	public boolean isInstanceOf()
	{
		return true;
	}
}
