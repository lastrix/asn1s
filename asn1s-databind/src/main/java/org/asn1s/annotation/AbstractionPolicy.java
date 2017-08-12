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

package org.asn1s.annotation;

import org.asn1s.api.type.ComponentType.Kind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface AbstractionPolicy
{

	Policy value() default Policy.Copy;

	Kind componentKind() default Kind.PRIMARY;

	int index() default -1;

	Class<?> type();

	enum Policy
	{
		Copy,
		ComponentsFrom
	}

	final class AbstractionPolicyValue
	{
		public AbstractionPolicyValue( Policy policy, int index, Kind componentKind, Type type )
		{
			this.policy = policy;
			this.index = index;
			this.componentKind = componentKind;
			this.type = type;
		}

		private final Policy policy;
		private final int index;
		private final Kind componentKind;
		private final Type type;

		public Policy getPolicy()
		{
			return policy;
		}

		public int getIndex()
		{
			return index;
		}

		public Kind getComponentKind()
		{
			return componentKind;
		}

		public Type getType()
		{
			return type;
		}
	}
}
