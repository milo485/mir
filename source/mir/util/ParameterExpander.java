/*
 * Copyright (C) 2001, 2002  The Mir-coders group
 *
 * This file is part of Mir.
 *
 * Mir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Mir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * In addition, as a special exception, The Mir-coders gives permission to link
 * the code of this program with the com.oreilly.servlet library, any library
 * licensed under the Apache Software License, The Sun (tm) Java Advanced
 * Imaging library (JAI), The Sun JIMI library (or with modified versions of
 * the above that use the same license as the above), and distribute linked
 * combinations including the two.  You must obey the GNU General Public
 * License in all respects for all of the code used other than the above
 * mentioned libraries.  If you modify this file, you may extend this exception
 * to your version of the file, but you are not obligated to do so.  If you do
 * not wish to do so, delete this exception statement from your version.
 */

package mir.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mir.generator.Generator;
import mir.generator.GeneratorExc;
import multex.Exc;

public class ParameterExpander {
  final static String NODE_SEPARATOR = ".";
  final static char STRING_ESCAPE_CHARACTER = '\\';

  private static Object findNode(String aKey, Map aMap, List aParts, boolean aMakeIfNotPresent) throws Exception {
    Iterator i;
    String location = "";
    Object node = aMap;
    Object newNode;

    i = aParts.iterator();

    while (i.hasNext()) {
      String part = (String) i.next();

      if (!(node instanceof Map)) {
        throw new Exception( "Can't expand key " + aKey + ": " + location + " is not a map" );
      }

      if (location.length()>0) {
        location=location + NODE_SEPARATOR;
      }
      location = location + part;

      newNode = ((Map) node).get(part);

      if (newNode == null)
        if (aMakeIfNotPresent) {
          newNode = new HashMap();
          ((Map) node).put(part, newNode);
        }
        else
          throw new ParameterExpanderExc( "Can't expand key {1}: {2} does not exist", new Object[]{aKey,location} );

      node = newNode;
    }

    return node;
  }

  public static Object findValueForKey(Map aMap, String aKey) throws Exception {
    Object node;
    List parts = StringRoutines.splitString(aKey, NODE_SEPARATOR);

    node = findNode(aKey, aMap, parts, false);

    return node;
  }

  public static String findStringForKey(Map aMap, String aKey) throws Exception {
    Object expandedValue = findValueForKey(aMap, aKey);

    if (!(expandedValue instanceof String))
      throw new ParameterExpanderExc( "Value of key is not a string but a {1}", new Object[]{expandedValue.getClass().getName()} );

    return (String) expandedValue;
  }

  public static void setValueForKey(Map aMap, String aKey, Object aValue) throws Exception {
    List parts = StringRoutines.splitString(aKey, NODE_SEPARATOR);

    String key = (String) parts.get(parts.size()-1);
    parts.remove(parts.size()-1);

    Object node=findNode(aKey, aMap, parts, true);

    if (node instanceof Map) {
      ((Map) node).put(key, aValue);
    }
    else
      throw new ParameterExpanderExc( "Can't set key {1}: not inside a Map", new Object[]{aKey} );
  }

  public static String expandExpression(Map aMap, String anExpression) throws Exception {
    int previousPosition = 0;
    int position;
    int endOfExpressionPosition;
    StringBuffer result = new StringBuffer();

    while ((position=anExpression.indexOf("$", previousPosition))>=0) {
      result.append(anExpression.substring(previousPosition, position));

      if (position>=anExpression.length()-1) {
        result.append(anExpression.substring(position, anExpression.length()));
        previousPosition=anExpression.length();
      }
      else
      {
        if (anExpression.charAt(position+1) == '{') {
          endOfExpressionPosition=position+2;
          while (endOfExpressionPosition<anExpression.length() && anExpression.charAt(endOfExpressionPosition) != '}') {
            if (anExpression.charAt(endOfExpressionPosition)=='\'' || anExpression.charAt(endOfExpressionPosition)=='"') {
              char boundary = anExpression.charAt(endOfExpressionPosition);

              endOfExpressionPosition++;
              while (endOfExpressionPosition<anExpression.length() && anExpression.charAt(endOfExpressionPosition) != boundary) {
                if (anExpression.charAt(endOfExpressionPosition) == STRING_ESCAPE_CHARACTER)
                  endOfExpressionPosition++;
                endOfExpressionPosition++;
              }
              if (endOfExpressionPosition>=anExpression.length()) {
                throw new ParameterExpanderExc("Unterminated string in {1}",new Object[]{anExpression});
              }
            }
            endOfExpressionPosition++;
          }
          if (endOfExpressionPosition<anExpression.length()) {
            result.append(evaluateStringExpression(aMap, anExpression.substring(position+2, endOfExpressionPosition)));
            previousPosition=endOfExpressionPosition+1;
          }
          else {
            throw new ParameterExpanderExc("Missing } in {1}",new Object[]{anExpression});
          }
        }
        else
        {
          previousPosition=position+2;
          result.append(anExpression.charAt(position+1));
        }
      }
    }
    result.append(anExpression.substring(previousPosition, anExpression.length()));

    return result.toString();
  }

  public static boolean evaluateBooleanExpression(Map aMap, String anExpression) throws Exception {
    Parser parser = new Parser(anExpression, aMap);

    return parser.parseBoolean();
  }

  public static String evaluateStringExpression(Map aMap, String anExpression) throws Exception {
    Parser parser = new Parser(anExpression, aMap);

    return parser.parseString();
  }

  public static int evaluateIntegerExpressionWithDefault(Map aMap, String anExpression, int aDefault) throws Exception {
    if (anExpression == null || anExpression.trim().equals(""))
      return aDefault;
    else
      return evaluateIntegerExpression(aMap, anExpression);
  }

  public static int evaluateIntegerExpression(Map aMap, String anExpression) throws Exception {
    Parser parser = new Parser(anExpression, aMap);

    return parser.parseInteger();
  }

  public static Object evaluateExpression(Map aMap, String anExpression) throws Exception {
    Parser parser = new Parser(anExpression, aMap);

    return parser.parseWhole();
  }

  private static class Reader {
    private String data;
    private int position;

    public Reader(String aData) {
      data = aData;
      position=0;
    }

    public Character peek() {
      if (position<data.length()) {
        return (new Character(data.charAt(position)));
      }

      return null;
    }

    public boolean hasNext() {
      return peek()!=null;
    }

    public Character getNext() {
      Character result = peek();

      if (result!=null)
        position++;

      return result;
    }

    public String getPositionString() {
      return data.substring(0, position) + "<__>" + data.substring(position) ;
    }
  }

  private static abstract class Token {
  }

  public static abstract class PunctuationToken extends Token { public PunctuationToken() { }; }
    private static class LeftSquareBraceToken extends PunctuationToken {};
    private static class RightSquareBraceToken extends PunctuationToken {};
    private static class EqualsToken extends PunctuationToken {};
    private static class EqualsNotToken extends PunctuationToken {};
    private static class NOTToken extends PunctuationToken {};
    private static class LeftParenthesisToken extends PunctuationToken {};
    private static class RightParenthesisToken extends PunctuationToken {};
    private static class CommaToken extends PunctuationToken {};
    private static class PeriodToken extends PunctuationToken {};
    private static class PlusToken extends PunctuationToken {};
    private static class TimesToken extends PunctuationToken {};
    private static class DivideToken extends PunctuationToken {};
    private static class MinusToken extends PunctuationToken {};
    private static class ConcatenateToken extends PunctuationToken {};
    private static class LessThanOrEqualsToken extends PunctuationToken {};
    private static class GreaterThanOrEqualsToken extends PunctuationToken {};
    private static class LessThanToken extends PunctuationToken {};
    private static class GreaterThanToken extends PunctuationToken {};


  private static class IdentifierToken extends Token {
    private String name;

    public IdentifierToken(String aName) {
      name = aName;
    }

    public String getName() {
      return name;
    }

  }

  private static class LiteralToken extends Token {
    private Object value;

    public LiteralToken(Object aValue) {
      value = aValue;
    }

    public Object getValue() {
      return value;
    }
  }

  private static class Scanner {
    private Reader reader;
    private Token nextToken;
    private String positionString;

    public Scanner(Reader aReader) {
      reader = aReader;
      skipWhitespace();
      positionString = reader.getPositionString();
    }

    public Token scanStringLiteral() {
      StringBuffer result = new StringBuffer();
      Character delimiter;

      delimiter = reader.getNext();

      while (reader.hasNext() && !reader.peek().equals(delimiter)) {
        if (reader.peek().charValue()==STRING_ESCAPE_CHARACTER) {
          reader.getNext();
          if (reader.hasNext())
            result.append(reader.getNext());
        }
        else {
          result.append(reader.getNext());
        }
      }

      if (!reader.hasNext())
        throw new RuntimeException("unterminated string");
      else
        reader.getNext();

      return new LiteralToken(result.toString());
    }

    public String getPositionString() {
      return positionString;
    }

    private Token scanNumber() {
      StringBuffer result = new StringBuffer();
      result.append(reader.getNext());

      while (reader.hasNext() && isNumberRest(reader.peek().charValue())) {
        result.append(reader.getNext());
      }

      try {
        return new LiteralToken(new Integer(Integer.parseInt(result.toString())));
      }
      catch (NumberFormatException e) {
        throw new RuntimeException("Invalid number: " + e.getMessage());
      }
    }

    private Token scanIdentifierKeyword() {
      StringBuffer result = new StringBuffer();
      result.append(reader.getNext());

      while (reader.hasNext() && isIdentifierRest(reader.peek().charValue())) {
        result.append(reader.getNext());
      }

      return new IdentifierToken(result.toString());
    }

    private Token scanPunctuation() {
      Character c;

      c = reader.getNext();

      switch(c.charValue()) {
        case '[': return new LeftSquareBraceToken();
        case ']': return new RightSquareBraceToken();
        case '=':
          if (reader.hasNext() && reader.peek().charValue() == '=') {
            reader.getNext();
            return new EqualsToken();
          }
          else {
            throw new RuntimeException("Unknown character: '='");
          }

        case '!':
          if (reader.hasNext() && reader.peek().charValue() == '=') {
            reader.getNext();
            return new EqualsNotToken();
          }
          else {
            return new NOTToken();
          }

        case '(': return new LeftParenthesisToken ();

        case ')': return new RightParenthesisToken ();
        case ',': return new CommaToken ();
        case '.': return new PeriodToken ();
        case '+':
          if (reader.hasNext() && reader.peek().charValue() == '+') {
            reader.getNext();
            return new ConcatenateToken();
          }
          else {
            return new PlusToken ();
          }
        case '*': return new TimesToken ();
        case '/': return new DivideToken ();
        case '-': return new MinusToken ();
        case '<':
          if (reader.hasNext() && reader.peek().charValue() == '=') {
            reader.getNext();
            return new LessThanOrEqualsToken();
          }
          else {
            return new LessThanToken();
          }

        case '>':
          if (reader.hasNext() && reader.peek().charValue() == '=') {
            reader.getNext();
            return new GreaterThanOrEqualsToken();
          }
          else {
            return new GreaterThanToken();
          }
        default:
          throw new RuntimeException("Unexpected character: "+c);
      }
    }

    public void skipWhitespace() {
      while (reader.hasNext() && Character.isWhitespace(reader.peek().charValue()))
        reader.getNext();
    };

    private boolean isIdentifierStart(char c) {
      return Character.isLetter(c) || (c == '_');
    }

    private boolean isIdentifierRest(char c) {
      return Character.isLetterOrDigit(c) || (c == '_');
    }

    private boolean isNumberStart(char c) {
      return Character.isDigit(c);
    }

    private boolean isNumberRest(char c) {
      return Character.isDigit(c);
    }

    public Token scanNext() {
      Token result = null;

      skipWhitespace();

      if (reader.hasNext()) {
        Character c = reader.peek();

        switch(c.charValue()) {
          case '\'':
          case '"':
            result = scanStringLiteral();
            break;

          default: {
            if (isIdentifierStart(c.charValue())) {
              result = scanIdentifierKeyword();
            }
            else if (isNumberStart(c.charValue())) {
              result = scanNumber();
            }
            else
              result = scanPunctuation();
          }
        }
      }

      skipWhitespace();

      return result;
    }

    public Token scan() {
      Token result = peek();
      nextToken = null;
      positionString = reader.getPositionString();

      return result;
    }

    public Token peek() {
      if (nextToken==null) {
        nextToken = scanNext();
      }

      return nextToken;
    }

    public boolean hasToken() {
      return peek()!=null;
    }
  }

  private static class Parser {
    private Scanner scanner;
    private Map valueMap;

    public Parser(String anExpression, Map aValueMap) {
      scanner = new Scanner(new Reader(anExpression));
      valueMap = aValueMap;
    }

    public boolean parseBoolean() {
      try {
        return interpretAsBoolean(parseWhole());
      }
      catch (Throwable t) {
        throw new RuntimeException("Parser error at '" + getLocation()+ "': "+t.getMessage());
      }
    }

    public int parseInteger() {
      try {
        return interpretAsInteger(parseWhole());
      }
      catch (Throwable t) {
        throw new RuntimeException("Parser error at '" + getLocation()+ "': "+t.getMessage());
      }
    }

    public String parseString() {
      try {
        return interpretAsString(parseWhole());
      }
      catch (Throwable t) {
        throw new RuntimeException("Parser error at '" + getLocation()+ "': "+t.getMessage());
      }
    }

    private String getLocation() {
      return scanner.getPositionString();
    }

    private Object parseWhole() {
      Object result = parse();

      if (scanner.hasToken()) {
        throw new RuntimeException("Operator expected");
      }

      return result;
    }

    private Object parse() {
      return parseUntil(MAX_OPERATOR_LEVEL);
    }

    private List parseList() {
      Token token;
      Object expression;
      List result = new Vector();

      token = scanner.scan();
      if (!(token instanceof LeftParenthesisToken)) {
        throw new RuntimeException("( expected");
      }

      if (scanner.peek() instanceof RightParenthesisToken) {
        scanner.scan();
        return result;
      }

      do {
        expression = parse();

        if (expression==null) {
          throw new RuntimeException("expression expected");
        }

        result.add(expression);

        token = scanner.scan();
      }
      while (token instanceof CommaToken);

      if (!(token instanceof RightParenthesisToken)) {
        throw new RuntimeException(") or , expected");
      }

      return result;
    }

    private Object parseVariable() {
      boolean done;
      Token token;
      Object currentValue = valueMap;
      Object qualifier;
      List parameters;

      do {
        token = scanner.peek();

        if (token instanceof LeftSquareBraceToken) {
          scanner.scan();
          qualifier = parseUntil(MAX_OPERATOR_LEVEL);
          token = scanner.scan();
          if (!(token instanceof RightSquareBraceToken))
            throw new RuntimeException("] expected");

          if (currentValue instanceof Map) {
            currentValue = ((Map) currentValue).get(qualifier);
          }
          else {
            throw new RuntimeException("cannot reference into anything other than a map ('"+qualifier+"')");
          }
        }
        else if (token instanceof IdentifierToken) {
          scanner.scan();
          qualifier = ((IdentifierToken) token).getName();

          if (currentValue instanceof Map) {
            currentValue = ((Map) currentValue).get(qualifier);
          }
          else {
            throw new RuntimeException("cannot reference into anything other than a map ('"+qualifier+"')");
          }
        }
        else if (token instanceof LeftParenthesisToken) {
          if (currentValue instanceof Generator.GeneratorFunction) {
            parameters = parseList();
            try {
              currentValue = ((Generator.GeneratorFunction) currentValue).perform(parameters);
            }
            catch (GeneratorExc t) {
              throw new RuntimeException(t.getMessage());
            }
          }
          else
            throw new RuntimeException("not a function");
        }
        else
          throw new RuntimeException("fieldname or [ expected");

        if (scanner.peek() instanceof PeriodToken ||
            scanner.peek() instanceof LeftSquareBraceToken ||
            scanner.peek() instanceof LeftParenthesisToken) {
          done = false;

          if (scanner.peek() instanceof PeriodToken)
            scanner.scan();
        }
        else
          done = true;
      } while (!done);

      return currentValue;
    }


    private Object parseUntil(int aMaxOperatorLevel) {
      Token token = scanner.peek();
      Object value;

      if (token instanceof LeftParenthesisToken) {
        scanner.scan();
        value = parse();
        token = scanner.peek();
        if (!(token instanceof RightParenthesisToken))
          throw new RuntimeException(") expected");
        scanner.scan();
      }
      else if (isUnaryOperator(token)) {
        scanner.scan();
        value = parseUntil(unaryOperatorLevel(token));
        value = expandOperatorExpression(token, value);
      }
      else if (token instanceof IdentifierToken || token instanceof LeftSquareBraceToken) {
        value = parseVariable();
      }
      else if (token instanceof LiteralToken) {
        scanner.scan();
        value = ((LiteralToken) token).getValue();
      }
      else
        throw new RuntimeException("Expression expected");

      token = scanner.peek();

      while (isBinaryOperator(token) && binaryOperatorLevel(token)<aMaxOperatorLevel) {
        Object value2;
        scanner.scan();

        if (isINOperator(token)) {
          value2 = parseList();
        }
        else {
          value2 = parseUntil(binaryOperatorLevel(token));
        }

        value = expandOperatorExpression(token, value, value2);

        token = scanner.peek();
      }

      return value;
    }

    private static final int MAX_OPERATOR_LEVEL = 1000;                //
    private static final int LOGICAL_OPERATOR_LEVEL = 5;               // && || !
    private static final int COMPARISON_OPERATOR_LEVEL = 4;            // == <= >= in < >
    private static final int ADDITION_OPERATOR_LEVEL = 3;              // + - &
    private static final int MULTIPLICATION_OPERATOR_LEVEL = 2;        // * /

    private int unaryOperatorLevel(Token aToken) {
      if (aToken instanceof NOTToken)
        return LOGICAL_OPERATOR_LEVEL;
      else if (aToken instanceof MinusToken)
        return ADDITION_OPERATOR_LEVEL;

      throw new RuntimeException("Internal error: unknown unary operator: " + aToken.getClass().getName());
    }

    private boolean isUnaryOperator(Token aToken) {
      return
          ((aToken instanceof NOTToken) ||
           (aToken instanceof MinusToken));
    }

    private int binaryOperatorLevel(Token aToken) {
      if (isANDOperator(aToken) ||
          isOROperator(aToken))
        return LOGICAL_OPERATOR_LEVEL;

      if ((aToken instanceof EqualsToken) ||
          (aToken instanceof EqualsNotToken) ||
          (aToken instanceof LessThanOrEqualsToken) ||
          (aToken instanceof LessThanToken) ||
          (aToken instanceof GreaterThanOrEqualsToken) ||
          (aToken instanceof GreaterThanToken) ||
          isINOperator(aToken))
        return COMPARISON_OPERATOR_LEVEL;

      if ((aToken instanceof PlusToken) ||
          (aToken instanceof ConcatenateToken) ||
          (aToken instanceof MinusToken))
        return ADDITION_OPERATOR_LEVEL;

      if ((aToken instanceof TimesToken) ||
          (aToken instanceof DivideToken))
        return MULTIPLICATION_OPERATOR_LEVEL;

      throw new RuntimeException("Internal error: unknown binary operator: " + aToken.getClass().getName());
    }

    private boolean isINOperator(Token aToken) {
      return (aToken instanceof IdentifierToken && ((IdentifierToken) aToken).getName().equals("in"));
    }

    private boolean isANDOperator(Token aToken) {
      return (aToken instanceof IdentifierToken && ((IdentifierToken) aToken).getName().equals("and"));
    }

    private boolean isOROperator(Token aToken) {
      return (aToken instanceof IdentifierToken && ((IdentifierToken) aToken).getName().equals("or"));
    }

    private boolean isBinaryOperator(Token aToken) {
      return
           (aToken instanceof EqualsToken) ||
           (aToken instanceof EqualsNotToken) ||
           (aToken instanceof PlusToken) ||
           (aToken instanceof TimesToken) ||
           (aToken instanceof DivideToken) ||
           (aToken instanceof MinusToken) ||
           (aToken instanceof ConcatenateToken) ||
           (aToken instanceof LessThanOrEqualsToken) ||
           (aToken instanceof LessThanToken) ||
           (aToken instanceof GreaterThanOrEqualsToken) ||
           (aToken instanceof GreaterThanToken) ||
           isINOperator(aToken) ||
           isOROperator(aToken) ||
           isANDOperator(aToken);
    }

    private boolean interpretAsBoolean(Object aValue) {
      if (aValue instanceof Boolean)
        return ((Boolean) aValue).booleanValue();

      return aValue!=null;
    }

    private int interpretAsInteger(Object aValue) {
      if (aValue instanceof Integer)
        return ((Integer) aValue).intValue();

      if (aValue instanceof String) {
        try {
          return Integer.parseInt((String) aValue);
        }
        catch (Throwable t) {
        }
      }

      throw new RuntimeException("Not an integer");
    }

    private String interpretAsString(Object aValue) {
      if (aValue instanceof String)
        return (String) aValue;
      if (aValue instanceof Integer)
        return ((Integer) aValue).toString();

      throw new RuntimeException("Not a string");
    }

    private Object expandOperatorExpression(Token aToken, Object aValue) {
      if (aToken instanceof NOTToken)
        return new Boolean(!interpretAsBoolean(aValue));
      else if (aToken instanceof MinusToken)
        return new Integer(-interpretAsInteger(aValue));

      throw new RuntimeException("Internal error: unknown unary operator: " + aToken.getClass().getName());
    }

    private boolean areEqual(Object aValue1, Object aValue2) {
      if (aValue1==null || aValue2==null)
        return (aValue1==null) && (aValue2==null);
      else
        return aValue1.equals(aValue2);
    }

    private Object expandOperatorExpression(Token aToken, Object aValue1, Object aValue2) {
      if (isINOperator(aToken)) {
        if (!(aValue2 instanceof List)) {
          throw new RuntimeException("Internal error: List expected");
        }

        Iterator i = ((List) aValue2).iterator();

        while (i.hasNext()) {
          if (areEqual(aValue1, i.next()))
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
      }

      if (isANDOperator(aToken))
        return new Boolean(interpretAsBoolean(aValue1) && interpretAsBoolean(aValue2));
      if (isOROperator(aToken))
        return new Boolean(interpretAsBoolean(aValue1) || interpretAsBoolean(aValue2));
      if (aToken instanceof EqualsToken) {
        return new Boolean(areEqual(aValue1, aValue2));
      }
      if (aToken instanceof EqualsNotToken)
        return new Boolean(!areEqual(aValue1, aValue2));
      if (aToken instanceof PlusToken)
        return new Integer(interpretAsInteger(aValue1) + interpretAsInteger(aValue2));
      if (aToken instanceof TimesToken)
        return new Integer(interpretAsInteger(aValue1) * interpretAsInteger(aValue2));
      if (aToken instanceof DivideToken)
        return new Integer(interpretAsInteger(aValue1) / interpretAsInteger(aValue2));
      if (aToken instanceof MinusToken)
        return new Integer(interpretAsInteger(aValue1) - interpretAsInteger(aValue2));

      if (aToken instanceof ConcatenateToken)
        return interpretAsString(aValue1) + interpretAsString(aValue2);

      if (aToken instanceof LessThanOrEqualsToken)
        return new Boolean(interpretAsInteger(aValue1) <= interpretAsInteger(aValue2));
      if (aToken instanceof LessThanToken)
        return new Boolean(interpretAsInteger(aValue1) < interpretAsInteger(aValue2));
      if (aToken instanceof GreaterThanOrEqualsToken)
        return new Boolean(interpretAsInteger(aValue1) >= interpretAsInteger(aValue2));
      if (aToken instanceof GreaterThanToken)
        return new Boolean(interpretAsInteger(aValue1) > interpretAsInteger(aValue2));

      throw new RuntimeException("Internal error: unknown binary operator: " + aToken.getClass().getName());
    }
  }

  public static class ParameterExpanderExc extends Exc {
    public ParameterExpanderExc(String msg, Object[] objects) {
      super(msg, objects);
    }
  }
}