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

package org.asn1s.core.scope;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.Module;
import org.asn1s.api.Ref;
import org.asn1s.api.Scope;
import org.asn1s.api.Template;
import org.asn1s.api.type.Type;
import org.asn1s.api.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractScope implements Scope
{
	private static final Log log = LogFactory.getLog( AbstractScope.class );

	AbstractScope( @Nullable Scope parent )
	{
		this.parent = parent;
	}

	private final Scope parent;
	private final Map<String, Object> options = new HashMap<>();
	private Value value;

	@Override
	public Type getTypeOrDie()
	{
		throw new IllegalStateException( "No Type available" );
	}

	@NotNull
	@Override
	public final Scope getParentScope()
	{
		if( parent == null )
			throw new IllegalStateException();
		return parent;
	}

	@Override
	public boolean hasParentScope()
	{
		return parent != null;
	}

	@NotNull
	@Override
	public Module getModule()
	{
		return getParentScope().getModule();
	}

	@Override
	public final Scope templateInstanceScope( @NotNull Template<?> template, @NotNull List<Ref<?>> arguments )
	{
		return new TemplateInstanceScope( this, template, arguments );
	}

	@Override
	public final Scope templateScope( @NotNull Template<?> template )
	{
		return new TemplateScope( this, template );
	}

	@Override
	public final Scope typedScope( Type type )
	{
		return new TypedScope( this, type );
	}

	@SuppressWarnings( "unchecked" )
	@Nullable
	@Override
	public <T> T getScopeOption( String key )
	{
		T t = (T)options.get( key );
		if( t == null && parent != null )
			return parent.getScopeOption( key );
		return t;
	}

	@Override
	public void setScopeOption( String key, Object value )
	{
		options.put( key, value );
	}

	@Override
	public void setValueLevel( Value value )
	{
		if( this.value != null && log.isDebugEnabled() )
			log.debug( "Overwriting ValueLevel: " + this.value + " changing to " + value );
//			throw new IllegalStateException();
		this.value = value;
	}

	@Override
	public Value getValueLevel()
	{
		return value;
	}

	@Override
	public Pair<Type[], Value[]> getValueLevels()
	{
		int depth = getValueLevelDepth();
		Type[] types = new Type[depth];
		Value[] values = new Value[depth];
		fillValueLevels( types, values, depth );
		return ImmutablePair.of( types, values );
	}

	void fillValueLevels( Type[] types, Value[] values, int depth )
	{
		if( parent instanceof AbstractScope )
			( (AbstractScope)parent ).fillValueLevels( types, values, depth );
	}

	int getValueLevelDepth()
	{
		if( !( parent instanceof AbstractScope ) )
			return 0;
		return ( (AbstractScope)parent ).getValueLevelDepth();
	}
}
