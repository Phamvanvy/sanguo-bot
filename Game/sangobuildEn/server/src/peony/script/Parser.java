package peony.script;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Parser {
	protected int peek;
	protected Token token;
	protected Reader reader;
	
	public Parser(Reader reader) throws IOException{
		this.reader = reader;
		peek();
	}
	
	protected void match(int c) throws IOException{
		if(peek==c) peek();
	}
	
	protected void token() throws IOException{
		if(Character.isDigit((char)peek)||peek=='-'){  //数字
			StringBuilder sb = new StringBuilder();
			do{
				sb.append((char)peek);
				peek();
			} while(Character.isDigit((char)peek)||(peek=='x'||peek=='X'));
			String s = sb.toString();
			if(s.startsWith("0x")||s.startsWith("0X")){ //处理16进制
				s = String.valueOf(Integer.parseInt(s.substring(2),16));
			}
			token = new Token(Token.NUM,s);
			return;
		}
		if(Character.isLetter((char)peek)||peek=='_'){  //变量，常量，函数
			StringBuilder sb = new StringBuilder();
			do{
				sb.append((char)peek);
				peek();
			} while(Character.isJavaIdentifierPart((char)peek));
			token = new Token(Token.STRING,sb.toString());
			return;
		}


		if(peek=='\"'){ //字符串常量
			match(peek);
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			while (peek != '\"') {
				if (peek == '\\' && first) {  //处理转义符问题
					sb.append(getEscapeCharacter());
				} else {
					sb.append((char) peek);
				}
				peek(true);
			}
			match('\"');
			token = new Token(Token.STRING_CONSTANT,sb.toString());
			return;
		}
	} 
	protected char getEscapeCharacter() throws IOException{
		peek();
		if(peek=='n')
			return '\n';
		if(peek=='t')
			return '\t';
		if(peek=='b')
			return '\b';
		if(peek=='r')
			return '\r';
		if(peek=='f')
			return '\f';
		if(peek=='\'')
			return '\'';
		if(peek=='\"')
			return '\"';
		if(peek=='\\')
			return '\\';
		throw new IllegalArgumentException();
	}
	
	public ExpressionList expressionList() throws IOException{
		ExpressionList ret = new ExpressionList();
		while (peek!=-1) {
			Expression exp = expression();
			switch (peek) {
			case '=':
			case '<':
			case '>':
			case '!':
				operator();
				int operator = ParseUtil.getOperator(token.value);
//				token();
//				Expression right = ParseUtil.getExpression(token);
				Expression right = expression();
				ret.addExpression(new BinaryExpression(operator,exp,right));
				break;
			default:
				ret.addExpression(exp);
				
			}
			match(',');
		}
		return ret;
	}
	
	public Expression expression() throws IOException{
		if(peek==')'){ 
			match(')');
			return null;
		}
		token();
		switch(peek){
		case '=':
		case '<':
		case '>':
		case '!':
			Expression left = ParseUtil.getExpression(token);
			operator();
			int operator = ParseUtil.getOperator(token.value);
//			token();
//			Expression right = ParseUtil.getExpression(token);
			Expression right = expression();
			return new BinaryExpression(operator,left,right);
		case '(':
			return function();
		case ')':
		case -1:
		case ',':
			return ParseUtil.getExpression(token);
		default:
			throw new IOException();
		}
	}
	
	protected void operator() throws IOException{
		switch(peek){
		case '=':
			match(peek);match('=');token = new Token(Token.OPERATOR,"=="); break;
		case '>':
			match(peek);
			if(peek=='='){
				match(peek);
				token = new Token(Token.OPERATOR,">=");
			}else{
				token = new Token(Token.OPERATOR,">");
			}
			break;
		case '<':
			match(peek);
			if(peek=='='){
				match(peek);
				token = new Token(Token.OPERATOR,"<=");
			}else{
				token = new Token(Token.OPERATOR,"<");
			}
			break;
		case '!':
			match(peek);match('=');token = new Token(Token.OPERATOR,"!="); break;
		default:
			throw new IOException();
		}
	}
	
	protected Expression function() throws IOException {
		match('(');
		if (token.value.equalsIgnoreCase("if")) { //对于if函数进行特殊处理
			Expression one = expression();
			match(',');
			Expression two = expression();
			match(',');
			Expression three = expression();
			match(')');
			return new TripleExpression(one,two,three);
		} else {
			Function ret = new Function(token.value);
			Expression express = expression();
			if (express == null) {  //对没有参数的函数做特殊处理
				return ret;
			} else {
				ret.addExpression(express);
			}
			do {
				if (peek == ')') {
					match(peek);
					break;
				}
				if (peek == ',') {
					match(peek);
					ret.addExpression(expression());
				} else {
					throw new IOException();
				}
			} while (true);
			return ret;
		}
		
	}
	
	public void peek() throws IOException{
		for(;;){
			peek = reader.read();
			if(peek==' '||peek=='\t') continue;
			break;
		}
	}
	
	public void peek(boolean ingore) throws IOException{
		for(;;){
			peek = reader.read();
			break;
		}
	}
	
	public static void main(String[] args) throws IOException{
		String s = "HasItem(1, 2) == 0";
		StringReader reader = new StringReader(s);
		Parser parser = new Parser(reader);
		ExpressionList el = parser.expressionList();
		System.out.println("ok");
	}
}
