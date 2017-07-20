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

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asn1s.api.ObjectFactory;
import org.asn1s.api.Ref;
import org.asn1s.api.UniversalType;
import org.asn1s.api.module.Module;
import org.asn1s.api.module.ModuleResolver;
import org.asn1s.api.type.ClassFieldType;
import org.asn1s.api.type.ClassType;
import org.asn1s.api.type.Type.Family;
import org.asn1s.api.type.TypeNameRef;
import org.asn1s.api.util.RefUtils;
import org.asn1s.api.value.ValueNameRef;
import org.asn1s.schema.Asn1ErrorListener;
import org.asn1s.schema.parser.Asn1Lexer;
import org.asn1s.schema.parser.Asn1Parser;
import org.asn1s.schema.x681.SyntaxObject.Kind;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class AbstractSyntaxParser
{
	private static final Log log = LogFactory.getLog( AbstractSyntaxParser.class );

	private final ModuleResolver resolver;
	private final ObjectFactory factory;
	private final Module module;
	private final ClassType classType;
	private final GroupSyntaxObject root;

	public AbstractSyntaxParser( ModuleResolver resolver, ObjectFactory factory, Module module, ClassType classType )
	{
		this.resolver = resolver;
		this.factory = factory;
		this.module = module;
		this.classType = classType;

		root = new GroupSyntaxObject();
		parseGroupItems( root, new LinkedList<>( classType.getSyntaxList() ), false );
	}

	public Map<String, Ref<?>> parse( String value ) throws Exception
	{
		try( Reader r = new StringReader( value ) )
		{
			TokenSource lexer = new Asn1Lexer( new ANTLRInputStream( r ) );
			CommonTokenStream tokenStream = new CommonTokenStream( lexer );
			return parseImpl( tokenStream );
		}
	}

	private Map<String, Ref<?>> parseImpl( CommonTokenStream tokenStream )
	{
		Map<String, Ref<?>> map = new HashMap<>();
		parseTokens( map, tokenStream, root );
		Token token = tokenStream.LT( 1 );
		if( token != null && token.getType() != Recognizer.EOF )
			throw new IllegalArgumentException( "Unable to parse abstract syntax, token left: " + token );
		return map;
	}

	private void parseTokens( Map<String, Ref<?>> map, CommonTokenStream tokenStream, GroupSyntaxObject root )
	{
		boolean start = true;
		List<SyntaxObject> objects = root.getObjects();
		for( SyntaxObject object : objects )
		{
			if( object.getKind() == Kind.Group )
				parseTokens( map, tokenStream, (GroupSyntaxObject)object );
			else if( object.getKind() == Kind.Keyword )
			{
				Token token = tokenStream.LT( 1 );
				if( !token.getText().equals( object.getText() ) )
				{
					if( !start )
						throw new IllegalStateException( "Expected token does not found: " + object.getText() + ", got: " + token.getText() );
					break;
				}
				tokenStream.consume();
			}
			else if( object.getKind() == Kind.TypeField )
			{
				if( !consumeTypeField( map, tokenStream, object ) )
				{
					if( !start )
						throw new IllegalStateException( "Expected token does not found: " + object.getText() );
					break;
				}
			}
			else if( object.getKind() == Kind.ValueField )
			{
				if( !consumeValueField( map, tokenStream, object ) )
				{
					if( !start )
						throw new IllegalStateException( "Expected token does not found: " + object.getText() );
					break;
				}
			}
			start = false;
		}
	}

	private boolean consumeValueField( Map<String, Ref<?>> map, CommonTokenStream tokenStream, SyntaxObject object )
	{
		Token token = tokenStream.LT( 1 );
		switch( token.getType() )
		{
			case Asn1Parser.Identifier:
				return tryParseValueReference( map, object, tokenStream, token );

			case Asn1Parser.OPEN_BRACE:
				return tryParseBraceValue( map, object, tokenStream );

			case Asn1Parser.NumberLiteral:
				registerFieldRef( map, object.getText(), factory.integer( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.RealLiteral:
				registerFieldRef( map, object.getText(), factory.real( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.HString:
				registerFieldRef( map, object.getText(), factory.hString( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.BString:
				registerFieldRef( map, object.getText(), factory.bString( token.getText() ) );
				tokenStream.consume();
				return true;

			case Asn1Parser.CString:
				registerFieldRef( map, object.getText(), factory.cString( token.getText() ) );
				tokenStream.consume();
				return true;

			default:
				return false;
		}
	}

	private boolean tryParseBraceValue( Map<String, Ref<?>> map, SyntaxObject object, CommonTokenStream tokenStream )
	{
		// consume all tokens up to }
		int level = 0;
		int position = 2;
		StringBuilder sb = new StringBuilder( "{ " );
		Token token = tokenStream.LT( position );
		while( token != null )
		{
			if( token.getType() == Asn1Parser.CLOSE_BRACE )
			{
				if( level == 0 )
				{
					sb.append( '}' );
					if( parseBracedValue( sb.toString(), object, map ) )
					{
						while( position > 0 )
						{
							tokenStream.consume();
							position--;
						}
						return true;
					}
					return false;
				}
				level--;
			}
			else if( token.getType() == Asn1Parser.OPEN_BRACE )
				level++;

			sb.append( token.getText() ).append( ' ' );
			position++;
			token = tokenStream.LT( position );
		}

		return false;
	}

	private boolean parseBracedValue( String value, SyntaxObject object, Map<String, Ref<?>> map )
	{
		ClassFieldType field = classType.getField( object.getText() );
		assert field != null;
		boolean isOid = field.getFamily() == Family.Oid;

		try( Reader r = new StringReader( value ) )
		{
			TokenSource lexer = new Asn1Lexer( new ANTLRInputStream( r ) );
			CommonTokenStream tokenStream = new CommonTokenStream( lexer );
			Asn1Parser parser = new Asn1Parser( tokenStream, resolver, factory );
			parser.addErrorListener( new Asn1ErrorListener() );
			parser.setBuildParseTree( false );
			parser.setModule( module );
			parser.getInterpreter().setPredictionMode( PredictionMode.SLL );

			if( isOid )
				registerFieldRef( map, object.getText(), parser.objectIdentifierValue().result );
			else
				registerFieldRef( map, object.getText(), parser.value().result );

		} catch( Exception e )
		{
			if( log.isDebugEnabled() )
				log.debug( "Unable to parse value: " + value, e );
			return false;
		}

		return true;
	}

	private static boolean tryParseValueReference( Map<String, Ref<?>> map, SyntaxObject object, CommonTokenStream tokenStream, Token token )
	{
		if( !RefUtils.isValueRef( token.getText() ) )
			return false;

		Token nextToken = tokenStream.LT( 2 );
		if( nextToken != null && nextToken.getType() == Asn1Parser.DOT )
		{
			nextToken = tokenStream.LT( 3 );
			if( nextToken != null && nextToken.getType() == Asn1Parser.Identifier )
			{
				if( !RefUtils.isValueRef( token.getText() ) )
					return false;

				tokenStream.consume();
				tokenStream.consume();
				tokenStream.consume();
				registerFieldRef( map, object.getText(), new ValueNameRef( nextToken.getText(), token.getText() ) );
				return true;
			}
			else
				return false;
		}
		else
		{
			registerFieldRef( map, object.getText(), new ValueNameRef( token.getText(), null ) );
			tokenStream.consume();
			return true;
		}
	}

	private static boolean consumeTypeField( Map<String, Ref<?>> map, CommonTokenStream tokenStream, SyntaxObject object )
	{
		Token token = tokenStream.LT( 1 );
		switch( token.getType() )
		{
			case Asn1Parser.EMBEDDED:
				return tryParseEmbeddedPdv( map, tokenStream, object );


			case Asn1Parser.OCTET:
			case Asn1Parser.BIT:
			case Asn1Parser.CHARACTER:
				return tryParseOBCString( map, tokenStream, object, token );


			case Asn1Parser.OBJECT:
				return tryParseObjectIdentifier( map, tokenStream, object );

			case Asn1Parser.RestrictedString:
			case Asn1Parser.BOOLEAN:
			case Asn1Parser.DATE:
			case Asn1Parser.DATE_TIME:
			case Asn1Parser.DURATION:
			case Asn1Parser.EXTERNAL:
			case Asn1Parser.OID_IRI:
			case Asn1Parser.NULL:
			case Asn1Parser.REAL:
			case Asn1Parser.RELATIVE_OID_IRI:
			case Asn1Parser.RELATIVE_OID:
			case Asn1Parser.TIME:
			case Asn1Parser.TIME_OF_DAY:
			case Asn1Parser.INTEGER:
				registerFieldRef( map, object.getText(), UniversalType.forTypeName( token.getText() ).ref() );
				return true;

			default:
				return tryParseReference( map, object, tokenStream );
		}
	}

	private static boolean tryParseObjectIdentifier( Map<String, Ref<?>> map, CommonTokenStream tokenStream, SyntaxObject object )
	{
		Token nextToken = tokenStream.LT( 2 );
		if( nextToken.getType() != Asn1Parser.IDENTIFIER )
			throw new IllegalStateException( "Illegal type name: OBJECT" );
		tokenStream.consume();
		tokenStream.consume();
		registerFieldRef( map, object.getText(), UniversalType.ObjectIdentifier.ref() );
		return true;
	}

	private static boolean tryParseEmbeddedPdv( Map<String, Ref<?>> map, CommonTokenStream tokenStream, SyntaxObject object )
	{
		Token token = tokenStream.LT( 2 );
		if( token.getType() != Asn1Parser.PDV )
			throw new IllegalStateException( "Illegal type name: EMBEDDED" );
		tokenStream.consume();
		tokenStream.consume();
		registerFieldRef( map, object.getText(), UniversalType.EmbeddedPdv.ref() );
		return true;
	}

	private static boolean tryParseOBCString( Map<String, Ref<?>> map, CommonTokenStream tokenStream, SyntaxObject object, Token token )
	{
		Token nextToken = tokenStream.LT( 2 );
		if( nextToken.getType() != Asn1Parser.STRING )
			throw new IllegalStateException( "Illegal type name: " + token.getText() );

		tokenStream.consume();
		tokenStream.consume();

		if( token.getType() == Asn1Parser.OCTET )
			registerFieldRef( map, object.getText(), UniversalType.OctetString.ref() );
		else if( token.getType() == Asn1Parser.BIT )
			registerFieldRef( map, object.getText(), UniversalType.BitString.ref() );
		else
			registerFieldRef( map, object.getText(), UniversalType.CharacterString.ref() );
		return true;
	}

	private static boolean tryParseReference( Map<String, Ref<?>> map, SyntaxObject object, CommonTokenStream tokenStream )
	{
		Token token = tokenStream.LT( 1 );
		if( token.getType() != Asn1Parser.Identifier )
			return false;

		if( !RefUtils.isTypeRef( token.getText() ) )
			return false;

		Token nextToken = tokenStream.LT( 2 );
		if( nextToken != null && nextToken.getType() == Asn1Parser.DOT )
			return tryParseExternalReference( map, object, tokenStream, token );

		registerFieldRef( map, object.getText(), new TypeNameRef( token.getText(), null ) );
		tokenStream.consume();
		return true;
	}

	private static boolean tryParseExternalReference( Map<String, Ref<?>> map, SyntaxObject object, CommonTokenStream tokenStream, Token token )
	{
		Token nextToken = tokenStream.LT( 3 );
		if( nextToken == null || nextToken.getType() != Asn1Parser.Identifier || !RefUtils.isTypeRef( nextToken.getText() ) )
			return false;

		tokenStream.consume();
		tokenStream.consume();
		tokenStream.consume();
		registerFieldRef( map, object.getText(), new TypeNameRef( nextToken.getText(), token.getText() ) );
		return true;
	}

	private static void registerFieldRef( Map<String, Ref<?>> map, String name, Ref<?> ref )
	{
		if( map.containsKey( name ) )
			throw new IllegalStateException( "Trying to redefine value for field: " + name );

		map.put( name, ref );
	}

	private static void parseGroupItems( GroupSyntaxObject root, Queue<String> list, boolean expectCloseBracket )
	{
		while( !list.isEmpty() )
		{
			String item = list.poll();
			if( item.startsWith( "&" ) )
			{
				root.addObject( new SimpleSyntaxObject( Character.isUpperCase( item.charAt( 1 ) ) ? Kind.TypeField : Kind.ValueField, item ) );
			}
			else if( item.equals( "[" ) )
			{
				GroupSyntaxObject object = new GroupSyntaxObject();
				root.addObject( object );
				parseGroupItems( object, list, true );
			}
			else if( item.equals( "]" ) )
			{
				if( !expectCloseBracket )
					throw new IllegalStateException( "Unexpected close bracket!" );
				break;
			}
			else
			{
//				if( !RefUtils.isTypeRef( item ) )
//					throw new IllegalStateException( "Not valid keyword: " + item );
				root.addObject( new SimpleSyntaxObject( Kind.Keyword, item ) );
			}
		}
	}
}
