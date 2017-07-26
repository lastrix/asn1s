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

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.IllegalValueException;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.type.AbstractType;
import org.asn1s.api.type.ClassFieldType;
import org.asn1s.api.type.ClassType;
import org.asn1s.api.type.Type;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.asn1s.api.value.x681.ObjectValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public class ClassTypeImpl extends AbstractType implements ClassType
{
	private final List<String> syntaxList = new ArrayList<>();
	private final List<ClassFieldType> fields = new ArrayList<>();

	@Override
	public void add( @NotNull ClassFieldType field )
	{
		fields.add( field );
	}

	@NotNull
	@Override
	public Family getFamily()
	{
		return Family.OBJECT_CLASS;
	}

	@Nullable
	@Override
	public ClassFieldType getField( @NotNull String name )
	{
		for( ClassFieldType field : fields )
			if( field.getName().equals( name ) )
				return field;

		return null;
	}

	@NotNull
	@Override
	public List<ClassFieldType> getFields()
	{
		return Collections.unmodifiableList( fields );
	}

	@NotNull
	@Override
	public Type copy()
	{
		ClassType result = new ClassTypeImpl();
		for( ClassFieldType field : fields )
			result.add( (ClassFieldType)field.copy() );

		result.setSyntaxList( syntaxList );
		return result;
	}

	@Override
	public void setSyntaxList( @NotNull List<String> syntaxList )
	{
		this.syntaxList.clear();
		this.syntaxList.addAll( syntaxList );
	}

	@Override
	public boolean hasSyntaxList()
	{
		return !syntaxList.isEmpty();
	}

	@Override
	public List<String> getSyntaxList()
	{
		return Collections.unmodifiableList( syntaxList );
	}

	@Override
	public String toString()
	{
		return "CLASS {" + StringUtils.join( fields, ',' + System.lineSeparator() ) + '}';
	}

	@Override
	public boolean isAllFieldsOptional()
	{
		for( ClassFieldType field : fields )
			if( field.isRequired() )
				return false;

		return true;
	}

	@Override
	protected void onValidate( @NotNull Scope scope ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		for( ClassFieldType field : fields )
		{
			field.setNamespace( getNamespace() );
			field.validate( scope );
		}

		// TODO: syntax list validation
	}

	@Override
	public void accept( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ValidationException, ResolutionException
	{
		scope = getScope( scope );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		Kind kind = value.getKind();
		if( kind == Kind.OBJECT )
			assertObjectFields( scope, value.toObjectValue().getFields() );
		else
			throw new IllegalValueException( "Unable to accept value: " + valueRef );
	}

	@NotNull
	@Override
	public Value optimize( @NotNull Scope scope, @NotNull Ref<Value> valueRef ) throws ResolutionException, ValidationException
	{
		scope = getScope( scope );
		Value value = RefUtils.toBasicValue( scope, valueRef );
		if( value.getKind() == Kind.OBJECT )
			return optimizeObject( scope, value.toObjectValue() );

		throw new IllegalValueException( "Unable to optimize value: " + valueRef );
	}

	private Value optimizeObject( Scope scope, ObjectValue objectValue ) throws ValidationException, ResolutionException
	{
		Collection<String> visited = new HashSet<>();
		Map<String, Ref<?>> result = new HashMap<>();
		for( Entry<String, Ref<?>> entry : objectValue.getFields().entrySet() )
		{
			ClassFieldType fieldType = getField( entry.getKey() );
			if( fieldType == null )
				throw new IllegalValueException( "There is no field with name: " + entry.getKey() );

			result.put( fieldType.getName(), fieldType.optimizeRef( scope, entry.getValue() ) );
			visited.add( fieldType.getName() );
		}

		for( ClassFieldType field : fields )
			if( !visited.contains( field.getName() ) && field.isRequired() )
				throw new IllegalValueException( "Required component is not met: " + field.getName() );

		return new ObjectValue( result );
	}

	private void assertObjectFields( Scope scope, Map<String, Ref<?>> map ) throws ValidationException, ResolutionException
	{
		for( ClassFieldType field : fields )
		{
			Ref<?> ref = map.get( field.getName() );
			if( ref == null )
			{
				if( field.isRequired() )
					throw new IllegalValueException( "Unable to find required field: " + field.getName() );

				continue;
			}

			field.acceptRef( scope, ref );
		}
	}
}
