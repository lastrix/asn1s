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

package org.asn1s.core.value;

import org.apache.commons.lang3.StringUtils;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.exception.ValidationException;
import org.asn1s.api.value.DefinedValue;
import org.asn1s.api.value.Value;
import org.asn1s.core.AbstractTemplateInstantiator;
import org.asn1s.core.CoreUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TemplateValueInstance implements Value
{
	public TemplateValueInstance( @NotNull Ref<Value> valueRef, @NotNull List<Ref<?>> arguments )
	{
		this.valueRef = valueRef;
		this.arguments = new ArrayList<>( arguments );
	}

	private final Ref<Value> valueRef;
	private final List<Ref<?>> arguments;

	private Ref<Value> getValueRef()
	{
		return valueRef;
	}

	private List<Ref<?>> getArguments()
	{
		return arguments;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Value resolve( Scope scope ) throws ResolutionException
	{
		return new TemplateInstantiator( scope ).newInstance();
	}

	@NotNull
	@Override
	public Kind getKind()
	{
		return Kind.TEMPLATE_INSTANCE;
	}

	@Override
	public int compareTo( @NotNull Value o )
	{
		return getKind().compareTo( o.getKind() );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) return true;
		if( !( obj instanceof TemplateValueInstance ) ) return false;

		TemplateValueInstance instance = (TemplateValueInstance)obj;

		//noinspection SimplifiableIfStatement
		if( !getValueRef().equals( instance.getValueRef() ) ) return false;
		return getArguments().equals( instance.getArguments() );
	}

	@Override
	public int hashCode()
	{
		int result = valueRef.hashCode();
		result = 31 * result + arguments.hashCode();
		return result;
	}

	@Override
	public void prettyFormat( StringBuilder sb, String prefix )
	{
		sb.append( valueRef ).append( "{ " ).append( StringUtils.join( arguments, ", " ) ).append( " }" );
	}

	@Override
	public String toString()
	{
		return valueRef + "{" + StringUtils.join( arguments, ", " ) + '}';
	}

	private final class TemplateInstantiator extends AbstractTemplateInstantiator
	{
		private final Scope scope;

		private TemplateInstantiator( Scope scope )
		{
			this.scope = scope;
		}

		Value newInstance() throws ResolutionException
		{
			try
			{
				DefinedValue templateValue = resolveValueTemplateOrDie();
				setTemplateScope( templateValue.createScope() );
				//noinspection ConstantConditions already checked non-null
				setTemplateInstanceScope( scope.templateInstanceScope( templateValue.getTemplate(), arguments ) );
				return createInstanceOf( templateValue );
			} catch( ValidationException e )
			{
				throw new ResolutionException( "Unable to create instance of value: " + getValueRef(), e );
			}
		}

		private Value createInstanceOf( DefinedValue templateValue ) throws ResolutionException, ValidationException
		{
			DefinedValueImpl instance = (DefinedValueImpl)templateValue.copy();
			//noinspection ConstantConditions already checked non-null
			List<Ref<?>> list = resolveTemplateInstance( templateValue.getTemplate(), arguments );
			arguments.clear();
			arguments.addAll( list );
			instance.setTemplate( getNewTemplate() );
			instance.validate( scope.templateInstanceScope( getNewTemplate(), arguments ) );
			return instance;
		}

		@NotNull
		private DefinedValue resolveValueTemplateOrDie() throws ResolutionException, ValidationException
		{
			Value resolved = getValueRef().resolve( scope );
			if( !isTemplateValue( resolved ) )
				throw new ValidationException( "ValueRef must point to value non-instance template" );
			//noinspection ConstantConditions
			CoreUtils.assertParameterMap( scope, ( (DefinedValue)resolved ).getTemplate() );
			return (DefinedValue)resolved;
		}

		private boolean isTemplateValue( Value resolved )
		{
			if( !( resolved instanceof DefinedValue ) )
				return false;
			DefinedValue type = (DefinedValue)resolved;
			return type.getTemplate() != null && !type.getTemplate().isInstance();
		}
	}
}
