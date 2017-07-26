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

import org.asn1s.api.encoding.tag.TagClass;
import org.asn1s.api.encoding.tag.TagMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for components
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( {ElementType.METHOD, ElementType.FIELD} )
public @interface Property
{
	/**
	 * Component name, must be valid ASN.1 component name
	 *
	 * @return string
	 */
	String name() default "#default";

	/**
	 * Component order, two components with same index will be sorted alphabetically
	 *
	 * @return int
	 */
	int index() default -1;

	/**
	 * Type for this component. Values from this component must be acceptable by TYPE.
	 *
	 * @return string
	 */
	String typeName() default "#default";

	/**
	 * @return true if property is optional and may be null
	 */
	boolean optional() default false;

	/**
	 * For manual component tagging
	 *
	 * @return int
	 */
	int tagNumber() default -1;

	/**
	 * For manual component tagging
	 *
	 * @return TagClass
	 */
	TagClass tagClass() default TagClass.CONTEXT_SPECIFIC;

	/**
	 * Default tagging method
	 *
	 * @return TagMethod
	 */
	TagMethod tagMethod() default TagMethod.AUTOMATIC;
}
