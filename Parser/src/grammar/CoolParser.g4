parser grammar CoolParser;

options {
	tokenVocab = CoolLexer;
}

@header{
	import cool.AST;
	import java.util.List;
	import static cool.AST.*;
}

@members{
	String filename;
	public void setFilename(String f){
		filename = f;
	}

/*
	DO NOT EDIT THE FILE ABOVE THIS LINE
	Add member functions, variables below.
*/

}

/*
	Add Grammar rules and appropriate actions for building AST below.
*/

program returns [AST.program value]	: 
						cl=class_list EOF 
							{
								$value = new AST.program($cl.value, $cl.value.get(0).lineNo);
							};

/*
Define the rule for the class list
it return the list of class defined in the file
*/
class_list returns [List<class_> value] 
@init{$value = new ArrayList<class_>();} 
		: classdef+;



/*
Rule for to class with semi colon

*/
classdef 	: classs SEMICOLON;



/*
This rule define the structure of the class.
It shows the inheritnace syntax along with the features list.
*/
classs returns [List<feature> features]  @init{$features = new ArrayList<feature>();} 
		: CLASS classname=TYPEID (INHERITS parent=TYPEID)?  LBRACE featuredef* RBRACE  
		{ class_ classInstance = new class_($classname.text, filename,$parent.text, $features, $CLASS.line);
		  $class_list::value.add(classInstance);
		};


featuredef 	: feature SEMICOLON;

/*
Features can be method/function or a member variable
Method         ==== feature::=  ID( [formal[[,formal]]∗] ) : TYPE{expr}
member variable==== feature::= ID : TYPE [<-expr] 
*/
feature returns[List<formal> formals]  @init{$formals = new ArrayList<formal>();} 
		: OBJECTID LPAREN (formal (COMMA formal)*)? RPAREN  COLON TYPEID LBRACE e=expr RBRACE 
	          { feature meth =  new method($OBJECTID.text,$formals,$TYPEID.text,$e.exprReturn,$OBJECTID.line);
		    $classs::features.add(meth);
		  } 

		| OBJECTID COLON TYPEID (ASSIGN e=expr)? 
		  {  expression e2 = $ASSIGN !=null ? $e.exprReturn: new no_expr($OBJECTID.line);
		    feature attribute =  new attr($OBJECTID.text,$TYPEID.text,e2,$OBJECTID.line);
	            $classs::features.add(attribute);	
 		  };

/*
It provide the grammer rule for the formal paramters of an method definition
formal ::=  ID : TYPE
*/
formal 		: OBJECTID COLON TYPEID 
		  { formal formalType = new formal($OBJECTID.text,$TYPEID.text,$OBJECTID.line);
		    $feature::formals.add(formalType);
		  };
/*
This rule evaluate the expression for the various match.
*/
expr returns[expression exprReturn,List<expression> commaExpr,List<branch> branches,List<attr> letList] 
@init {$commaExpr = new ArrayList<expression>();$branches = new ArrayList<branch>();$letList  = new ArrayList<attr>();}

		/* Define the below rule
		   Assign the value to an identifer
		expr::=  ID <-expr
		*/
		: OBJECTID ASSIGN e=expr 
		{$exprReturn = new assign($OBJECTID.text,$e.exprReturn,$OBJECTID.line);}

		/* Grammer rule for the static method call
		 expr::= expr[@TYPE].ID( [expr[[,expr]]∗] )
		*/
		| expr1=expr ATSYM TYPEID DOT OBJECTID LPAREN (expr2=expr { $expr1.commaExpr.add($expr2.exprReturn);} (COMMA expr3=expr 		  { $expr1.commaExpr.add($expr3.exprReturn);} )*)? RPAREN 
		{$exprReturn = new static_dispatch($expr1.exprReturn,$TYPEID.text,$OBJECTID.text,$expr1.commaExpr,$expr1.exprReturn.lineNo);} 
		
		/* Rule for method call through some expression result evaluation
		  expr::= expr.ID( [expr[[,expr]]∗] )
		*/
	        |  expr1=expr DOT OBJECTID LPAREN (expr2=expr { $expr1.commaExpr.add($expr2.exprReturn);} (COMMA expr3=expr 			   { $expr1.commaExpr.add($expr3.exprReturn);} )*)? RPAREN 
		{  $exprReturn = new dispatch($expr1.exprReturn,$OBJECTID.text,$expr1.commaExpr,$expr1.exprReturn.lineNo); }

		/* Rule for method call which is implicit to the class
		  expr::=ID( [expr[[,expr]]∗] )
		*/
		| OBJECTID LPAREN (expr2=expr { $commaExpr.add($expr2.exprReturn);} 
		  (COMMA expr3=expr { $commaExpr.add($expr3.exprReturn);} )*)? RPAREN 
		{ $exprReturn = new dispatch(new object("self" , $OBJECTID.line),$OBJECTID.text,$commaExpr,$OBJECTID.line);}
		
		/* Rule for the if else block
		 expr::= if expr then expr else expr fi
		*/
	  	| IF expr1=expr THEN expr2=expr ELSE expr3=expr FI 
		  { $exprReturn = new cond($expr1.exprReturn,$expr2.exprReturn,$expr3.exprReturn,$IF.line);}

		/* Rule  for the loop as deined below :-
		  expr::= while expr loop expr pool
		*/
		| WHILE expr1=expr LOOP expr2=expr POOL 
		  { $exprReturn = new loop($expr1.exprReturn, $expr2.exprReturn,$WHILE.line); }
		
		/* Rule for list of expression in a block seperated by semi-colon(;)
       		   expr ::= { [[expr; ]]+ }
		*/
		| LBRACE (e=expr SEMICOLON { $commaExpr.add($e.exprReturn);})+ RBRACE 
		  { $exprReturn = new block($commaExpr,$LBRACE.line); } 
		
		/* Rule for the Let as define below
		 let ID : TYPE [<- expr] [[,ID : TYPE [<- expr]]]∗ in expr
		*/
		| LET obj1=OBJECTID COLON type1=TYPEID (ASSIGN expr1=expr)? 
				{
				expression e = $ASSIGN !=null ? $expr1.exprReturn: new no_expr($LET.line);
				attr a1 = new attr($obj1.text,$type1.text,e,$LET.line);
				$letList.add(a1);
				} 
				(COMMA obj2=OBJECTID COLON type2=TYPEID (assign=ASSIGN expr2=expr)? 
				{
				expression e2 = $assign !=null ? $expr2.exprReturn: new no_expr($LET.line);
				attr a2 =  new attr($obj2.text,$type2.text,e2,$COMMA.line);
				$letList.add(a2);
				} )* IN expr3=expr 
				{
				$exprReturn = $expr3.exprReturn; 
		   attr this_attr;
		   for(int i = $letList.size() - 1; i >= 0; --i) 
			{
			this_attr = $letList.get(i);
			$exprReturn = new let(this_attr.name, this_attr.typeid, this_attr.value, $exprReturn, $LET.line);
			}
		}

		/* Rule for the case defination
		expr::= case expr of [[ID : TYPE =>expr; ]]+ esac
		*/
		| CASE expr1=expr OF (OBJECTID COLON TYPEID DARROW expr2=expr SEMICOLON {
				branch br = new branch($OBJECTID.text,$TYPEID.text,$expr2.exprReturn,$OBJECTID.line);
				$expr1.branches.add(br);})+ ESAC 
		{ $exprReturn = new typcase($expr1.exprReturn,$expr1.branches,$CASE.line);}

		/* Rule for the defining an object as below defination
		 new TYPE
		*/
		| NEW TYPEID { $exprReturn = new new_($TYPEID.text,$NEW.line);}

		/* Rule for define expression as void
		 expr::= ISVOID expr
		*/
		| ISVOID e=expr {$exprReturn = new isvoid($e.exprReturn,$ISVOID.line);}
		
		/* Rule for multiplication  of values evaluated by these two expression
		   expr::= expr * expr
		*/
		| expr1=expr STAR expr2=expr {$exprReturn = new mul($expr1.exprReturn,$expr2.exprReturn,$expr1.exprReturn.lineNo);}

		/* Rule for division of values evaluated by these two expression
		   expr::= expr / expr
		*/		
		| expr1=expr SLASH expr2=expr {$exprReturn = new divide($expr1.exprReturn,$expr2.exprReturn,$expr1.exprReturn.lineNo);}


		/* Rule for addition  of values evaluated by these two expression
       		   expr::= expr + expr
		*/
		| expr1=expr PLUS expr2=expr {$exprReturn = new plus($expr1.exprReturn,$expr2.exprReturn,$expr1.exprReturn.lineNo);}

		/* Rule for substraction  of values evaluated by these two expression
		  expr::= expr - expr
		*/
		| expr1=expr MINUS expr2=expr  {$exprReturn = new sub($expr1.exprReturn,$expr2.exprReturn,$expr1.exprReturn.lineNo);}
		
		/* Rule for Complement the value evaluated by the expression
		   expr::= ~expr
		*/
		| TILDE e=expr { $exprReturn = new comp($e.exprReturn,$TILDE.line);}

		/* Rule to compare the values evaluated by these two expressions with less than (<) operation
		   expr::= expr < expr
		*/
		| expr1=expr LT expr2=expr {$exprReturn = new lt($expr1.exprReturn, $expr2.exprReturn,$expr1.exprReturn.lineNo);}

		/* Rule to compare the values evaluated by these two expressions with less than equal (<=) operation
		   expr::= expr <= expr
		*/	
		| expr1=expr LE expr2=expr {$exprReturn = new leq($expr1.exprReturn, $expr2.exprReturn,$expr1.exprReturn.lineNo);}

		/* Rule to compare the values evaluated by these two expressions with equal(=) operation
		   expr::= expr = expr
		*/	
		| expr1=expr EQUALS expr2=expr {$exprReturn = new eq($expr1.exprReturn, $expr2.exprReturn,$expr1.exprReturn.lineNo);}

		/* Rule to find the negative off the value evaluated by the expression
		   expr::= not expr
		*/	
		| NOT e=expr {  $exprReturn = new neg($e.exprReturn,$NOT.line); }

		/* Rule to express the expression inside the ()
		   expr::=  ( expr )
		*/	
		| LPAREN e=expr RPAREN { $exprReturn = $e.exprReturn; }

		/* Rule to parse the object identifier
		   expr::=  ID
		*/	
		| OBJECTID   { $exprReturn = new object($OBJECTID.text,$OBJECTID.line);}

		/* Rule to parse the Integer Literal
		   expr::=  integer
		*/	
		| INT_CONST  { $exprReturn = new int_const(Integer.parseInt($INT_CONST.text),$INT_CONST.line); }

		/* Rule to parse the STRING Literal
		   expr::=  string
		*/
		| STR_CONST  { $exprReturn = new string_const($STR_CONST.text,$STR_CONST.line);}
		
		/* Rule to parse the Boolean Literal
		   expr::=  true | false
		*/
		| BOOL_CONST { boolean flag = false;
			if($BOOL_CONST.text.equalsIgnoreCase("true")){
			flag = true;
			 }
			$exprReturn = new bool_const(flag,$BOOL_CONST.line);
		     }
		; 
