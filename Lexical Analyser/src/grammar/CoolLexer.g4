lexer grammar CoolLexer;

tokens{
	ERROR,
	TYPEID,
	OBJECTID,
	BOOL_CONST,
	INT_CONST,
	STR_CONST,
	LPAREN,
	RPAREN,
	COLON,
	ATSYM,
	SEMICOLON,
	COMMA,
	PLUS,
	MINUS,
	STAR,
	SLASH,
	TILDE,
	LT,
	EQUALS,
	LBRACE,
	RBRACE,
	DOT,
	DARROW,
	LE,
	ASSIGN,
	CLASS,
	ELSE,
	FI,
	IF,
	IN,
	INHERITS,
	LET,
	LOOP,
	POOL,
	THEN,
	WHILE,
	CASE,
	ESAC,
	OF,
	NEW,
	ISVOID,
	NOT
}

/*
  DO NOT EDIT CODE ABOVE THIS LINE
*/

@members{

	/*
		YOU CAN ADD YOUR MEMBER VARIABLES AND METHODS HERE
	*/

	/**
	* Function to report errors.
	* Use this function whenever your lexer encounters any erroneous input
	* DO NOT EDIT THIS FUNCTION
	*/
	
	
	
	//This function is use to report the error sent by the regex with the type of error.
	public void reportError(String errorString){
		setText(errorString);
		setType(ERROR);
	}
	
	
	/* the below function is use to give error and also the token associated with*/
	
	public void notFound(){
		Token t = _factory.create(_tokenFactorySourcePair, _type, _text, _channel, _tokenStartCharIndex, getCharIndex()-1, _tokenStartLine, _tokenStartCharPositionInLine);
		String text = t.getText();
		reportError(text);
	}

	/* The below function process the string sent by string constant function*/

	public void processString() {
		Token t = _factory.create(_tokenFactorySourcePair, _type, _text, _channel, _tokenStartCharIndex, getCharIndex()-1, _tokenStartLine, _tokenStartCharPositionInLine);
		String text = t.getText();
		//System.out.println(text);
		String str = "";
		int flag=0;
		
		//write your code to test strings here
		
		String newStr = text.substring(1, text.length() -1);	/*Because we have to remove quotes from both end of string which is tokenize 																	by lexer*/
		if(newStr.length()>1024){
			reportError("String constant too long");
		}
		else{
			//System.out.println(newStr);
			//out_string(newStr);
			int i = 0;
			for(;i<newStr.length();){			//process whole string
				if(newStr.charAt(i) == '\n'){
					reportError("Unterminated string constant");
				}
				else if(newStr.charAt(i) == '\0')
				{
					 reportError("String contains null character");
				}
				else if(newStr.charAt(i) == '\\')
				{
                	char next = newStr.charAt(i + 1);
                    
					if (next == 'n') 
						str = str + "\n";
					else if (next == '\n')
						str = str +"\n";
					else if (next == '0'){
						str = str + "0";
						flag=1;
					}
					else if (next == 'b')
						str = str + "\b";
					else if (next == 'f')
						str = str + "\f";
					else if (next == 't')
						str = str + "\t";
					else
						str = str + (String.valueOf(text.charAt(i +1)));
 					i=i+2;
 					continue;
			}
			else{
				str = str + (String.valueOf(newStr.charAt(i)));
			}
			i+=1;
		}
		
		setText(str);						
		}
		if(flag ==1)
			reportError("String contains null character");
		

	}
}

/*
	WRITE ALL LEXER RULES BELOW
*/

//All the lexer rules.

SPACE		: [ \t\r\n\u ]+ -> skip;
TYPEID		: [A-Z][_a-zA-Z0-9]*;
OBJECTID	: [a-z][_a-zA-Z0-9]*;
BOOL_CONST	: [t][r][u][e] | [f][a][l][s][e];
INT_CONST	: [0-9]+;
LPAREN		: '(';
RPAREN		: ')';
COLON		: ':';
ATSYM		: '@';
SEMICOLON   : ';';
COMMA		: ',';
PLUS		: '+';
MINUS		: '-';
STAR		: '*';
SLASH		: '/';
TILDE		: '~';
LT			: '<';
EQUALS		: '=';
LBRACE		: '{';
RBRACE		: '}';
DOT			: '.';
DARROW      : '=>'; 
LE			: '<=';
ASSIGN		: '<-';
CLASS		: [c|C][l|L][a|A][s|S][s|S];
ELSE		: [e|E][l|L][s|S][e|E];
FI			: [f|F][i|I];
IF			: [i|I][f|F];
IN			: [i|I][n|N];
INHERITS	: [i|I][n|N][h|H][e|E][r|R][i|I][t|T][s|S];
LET			: [l|L][e|E][t|T];
LOOP		: [l|L][o|O][o|O][p|P];
POOL		: [p|P][o|O][o|O][l|L];
THEN		: [t|T][h|H][e|E][n|N];
WHILE		: [w|W][h|H][i|I][l|L][e|E];
CASE		: [c|C][a|A][s|S][e|E];
ESAC		: [e|E][s|S][a|A][c|C];
OF			: [o|O][f|F];
NEW			: [n|N][e|E][w|W];
ISVOID		: [i|I][s|S][v|V][o|O][i|I][d|D];		
NOT			: [n|N][o|O][t|T];

/*this if fragment is used anywhere but it doesn't match any token but it is used in grammar as sometimes we have to use same type of rules in many place and this will be helpful*/
fragment UT	:  ~('\\'|'"'|'\n');

//This checks Inline comments
SL_COMMENT	: '--'(.)*? ('\n'|'\r') -> skip;


//This check white spaces in the code.
WHITESPACE 	: [ \r\t\n\f]+ -> skip ;

SINGLE_BACKSLASH: '"'UT*'\n' {reportError("Unterminated string constant");};
EOF_STRING	: ('"' ( '\\' | [ \r\t\n ]+ |~('\\'|'"'))* )(EOF){reportError("EOF found in string");};
STR_CONST 	: '"' ( '\\\n' | ~('"'| '\n') |  [ \r\t]+)* '"' {processString();};

//OTHER	: . {reportError("EOF found in string");}; 
NOTFOUND	: . { notFound(); } ;

//This matches '*)' if there w/o '(*'
CLOSE_COMMENT	: '*)' {reportError("UnMatched comment identifier.");};


//This matches all the multi line comment scenario using stack implementation
COMMENT		: '(*'-> pushMode(INCOM), skip;
mode INCOM;
ER     	: .(EOF) { reportError("EOF in comment"); } ;
E_COMMENT	: '*)' -> popMode, skip ;
IN_COMMENT : . -> skip ;

