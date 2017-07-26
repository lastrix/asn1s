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

import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.UniversalType;
import org.asn1s.api.encoding.EncodingInstructions;
import org.asn1s.api.encoding.IEncoding;
import org.asn1s.api.exception.ResolutionException;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.Value;
import org.asn1s.api.value.Value.Kind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines Tag encoding instructions
 */
public abstract class TagEncoding implements IEncoding
{
	public static TagEncoding application( int tagNumber )
	{
		return new ResolvedTagEncoding( TagMethod.UNKNOWN, TagMethod.UNKNOWN, TagClass.APPLICATION, tagNumber );
	}

	public static TagEncoding context( int tagNumber, TagMethod tagMethod )
	{
		return new ResolvedTagEncoding( TagMethod.UNKNOWN, tagMethod, TagClass.CONTEXT_SPECIFIC, tagNumber );
	}

	public static TagEncoding universal( UniversalType type )
	{
		return new ResolvedTagEncoding( TagMethod.UNKNOWN, TagMethod.UNKNOWN, TagClass.UNIVERSAL, type.tagNumber() );
	}

	public static TagEncoding create( @NotNull TagMethod moduleTagMethod, @NotNull TagMethod tagMethod, @Nullable TagClass tagClass, int tagNumber )
	{
		return new ResolvedTagEncoding( moduleTagMethod, tagMethod, tagClass, tagNumber );
	}

	public static TagEncoding create( @NotNull TagMethod moduleTagMethod, @NotNull TagMethod tagMethod, @Nullable TagClass tagClass, Ref<Value> tagNumberRef )
	{
		return new UnresolvedTagEncoding( moduleTagMethod, tagMethod, tagClass, tagNumberRef );
	}

	protected TagEncoding( TagMethod tagMethod, TagClass tagClass )
	{
		this( TagMethod.UNKNOWN, tagMethod, tagClass );
	}

	TagEncoding( @NotNull TagMethod moduleTagMethod, @NotNull TagMethod tagMethod, @Nullable TagClass tagClass )
	{
		this.moduleTagMethod = moduleTagMethod;
		this.tagMethod = tagMethod;
		this.tagClass = tagClass == null ? TagClass.APPLICATION : tagClass;
	}

	private final TagMethod moduleTagMethod;
	private final TagMethod tagMethod;
	private final TagClass tagClass;

	public TagClass getTagClass()
	{
		return tagClass;
	}


	public TagMethod getTagMethod()
	{
		if( tagMethod != TagMethod.UNKNOWN )
			return tagMethod;

		if( moduleTagMethod == TagMethod.EXPLICIT || moduleTagMethod == TagMethod.UNKNOWN )
			return TagMethod.EXPLICIT;
		return TagMethod.IMPLICIT;
	}

	public TagMethod getTagMethodDirect()
	{
		return tagMethod;
	}

	public TagMethod getModuleTagMethod()
	{
		return moduleTagMethod;
	}

	@Override
	public final boolean equals( Object obj )
	{
		return this == obj || obj instanceof TagEncoding && toString().equals( obj.toString() );
	}

	@Override
	public final int hashCode()
	{
		return toString().hashCode();
	}

	public boolean isEqualToTag( @NotNull Tag tag )
	{
		return tag.getTagClass() == getTagClass() && tag.getTagNumber() == getTagNumber();
	}

	@Override
	public String toString()
	{
		if( tagMethod == TagMethod.UNKNOWN )
			return "[ " + tagClass.name().toUpperCase() + ' ' + getTagNumber() + " ]";
		return "[ " + tagClass.name().toUpperCase() + ' ' + getTagNumber() + " ] " + tagMethod.name().toUpperCase();
	}

	@Override
	public EncodingInstructions getEncodingInstructions()
	{
		return EncodingInstructions.TAG;
	}

	public abstract int getTagNumber();

	public Tag toTag( boolean constructed )
	{
		return new Tag( getTagClass(), constructed, getTagNumber() );
	}

	private static final class ResolvedTagEncoding extends TagEncoding
	{
		private ResolvedTagEncoding( @NotNull TagMethod moduleTagMethod, @NotNull TagMethod tagMethod, @Nullable TagClass tagClass, int tagNumber )
		{
			super( moduleTagMethod, tagMethod, tagClass );
			this.tagNumber = tagNumber;
		}

		private final int tagNumber;

		@Override
		public IEncoding resolve( @NotNull Scope scope )
		{
			return this;
		}

		@Override
		public int getTagNumber()
		{
			return tagNumber;
		}
	}

	private static final class UnresolvedTagEncoding extends TagEncoding
	{
		private UnresolvedTagEncoding( @NotNull TagMethod moduleTagMethod, @NotNull TagMethod tagMethod, @Nullable TagClass tagClass, Ref<Value> tagNumberRef )
		{
			super( moduleTagMethod, tagMethod, tagClass );
			this.tagNumberRef = tagNumberRef;
		}

		private final Ref<Value> tagNumberRef;

		@Override
		public IEncoding resolve( @NotNull Scope scope ) throws ResolutionException
		{
			Value value = RefUtils.toBasicValue( scope, tagNumberRef );

			if( value.getKind() != Kind.INTEGER || !value.toIntegerValue().isInt() )
				throw new ResolutionException( "Only integer values supported for tags that may be cast to Integer java type" );

			int tagNumber = value.toIntegerValue().asInt();
			if( tagNumber < 0 )
				throw new ResolutionException( "Tag number must be positive or zero" );

			return new ResolvedTagEncoding( getModuleTagMethod(), getTagMethod(), getTagClass(), tagNumber );
		}

		@Override
		public int getTagNumber()
		{
			throw new UnsupportedOperationException( "Must resolve first" );
		}
	}
}
