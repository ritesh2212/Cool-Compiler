# COOL Compiler #
The program consist of classes as COOL is Object Oriented Language. The class list constitutes of the seperate classes in the program. There are one or more classes in the program.

        program returns [AST.program value]	: 
						cl=class_list EOF 
							{
								$value = new AST.program($cl.value, $cl.value.get(0).lineNo);
							};
The above rule returns the AST's of the class list to print.


		

Each class of the program consist of sevral actions and the actions consist of parsing rules which we use to parse the program, so that the parent rule(pgm), maps to all other classes when they have to genrate the AST's. If some actions are defined outside the lists, the all the link to last child/class is maintained by the parser.  


class_list -> (returns )feature_list : This feature list consist of sevral feature of the class which are seprated by colon. features can be anything, it can either be method(function), or an attribute of the class.




The method consist of list of parameters, if it is outside the class then it called formal parameter. Each parameter is seprated by comma and it has no fix number of parameter. The implementation of formal parameters with its feature list is as given below. 

feature returns[List<formal> formals]  @init{$formals = new ArrayList<formal>();} 
		: OBJECTID LPAREN (formal (COMMA formal)*)? RPAREN  COLON TYPEID LBRACE e=expr RBRACE 
	          { feature meth =  new method($OBJECTID.text,$formals,$TYPEID.text,$e.exprReturn,$OBJECTID.line);
		    $classs::features.add(meth);
		  } 






Both the features consist of building blocks known as expressions. The expression lies within the body of the rule. method -> object(formals):TYPE { expr_body} attr -> object:TYPE <- expression




The main gist of the program lies in the arrangement of rules we write. ANTLR4 provide us feature that handles left recursion and left factoring both are limited to one rule. Precedence can be enforced as antlr guarantees to match the first rule of the grammar first to the expression. This means that if we have to write our rules in accordance to the precedence rule and the rules of grammar as follow: $expr * $expr| $expr / $expr| $expr + $expr| $expr - $expr; this means that in expr a+b*c, $expr*$expr will execute first and then expression $expr+$expr. So that semantics of the AST's are maintained. 


expr -> .. The expression rule uses the two features defined above heavily. We define rules with greater precedence first.

		The way we defined the expression rule is as follows:
			1. Dispatch 
			2. Static dispatch
			3. Arithmetic and logical operations (following precedence).
			4. Assignment operator
			5. Definition of types such as int, string, bool, and OBJECTID
			6. Block list
			7. Loops and conditions. If, While, Case, and Let

1. DISPATCH AND STATIC DISPATCH:
    They are used for method calling. The main difference between the above is that dispatch may have expression as its parameters, as it is calling a method it do not have its body where it calls while static dispatch does. It has list of parameters and it is in similar fashion as of to the function defintion for method feature.  
		expr1=expr ATSYM TYPEID DOT OBJECTID LPAREN (expr2=expr { $expr1.commaExpr.add($expr2.exprReturn);} (COMMA expr3=expr 		  { $expr1.commaExpr.add($expr3.exprReturn);} )*)? RPAREN 
		{$exprReturn = new static_dispatch($expr1.exprReturn,$TYPEID.text,$OBJECTID.text,$expr1.commaExpr,$expr1.exprReturn.lineNo);} 


3. ARITHMETIC AND LOGICAL EXPRESSIONS:
	All the rules we write must maintain its precedence accordingly as these precedence helps us to calculate the value of expression and also also in making of precedence tree. 
	
	expr1=expr STAR expr2=expr {$exprReturn = new mul($expr1.exprReturn,$expr2.exprReturn,$expr1.exprReturn.lineNo);}

4. ASSIGNMENT OPERATOR:
	The definition is similar to attr_entity. The assignment is made then left and right side must not be empty and also there must be only one variable on the left side of operator. 

	OBJECTID ASSIGN e=expr 
		{$exprReturn = new assign($OBJECTID.text,$e.exprReturn,$OBJECTID.line);}


5. TYPE DEFINITIONS:
	It is use for type such Bool, Int or String, there expressions are typecast so that AST holds correct semantics. 
	The typecasting was done as follows:
		INT_CONST  { $exprReturn = new int_const(Integer.parseInt($INT_CONST.text),$INT_CONST.line); }
	
6. BLOCK LIST:
    Block list is a type or list of  expressions or line enclosed within braces. The functions used to maintain list of expressions, classes, features, formals implemented to define the block.
    expr ::= { [[expr; ]]+ }

7. LOOP, IF, CASE, & LET
    The grammar of While and is simple as we have some condition/expression within parenthesis which results in answer in bool, and then branch comes into action if results holds true. Whereas the grammar of Case id bit more complicated, as Case maps to severals different branches, and all the branches of case follows same rule, and there is a lsit which is maintained to store all the branches. All the entities matches with with the tokens that matches AST.branch.
    Let expression: This maps several attributes to one, so we have to maintain nested let expressions in loop and it is concatenated to result of every next let expression. The list of attributes is mapped by attrib_list.
    
    LET obj1=OBJECTID COLON type1=TYPEID (ASSIGN expr1=expr)? 
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
    
    
    

GENERAL:

Each rule returned a value which was of the type AST.class. Where class is the nearest parent class of all the sub rules involved.


Each rule is comprised of valid expression, that create new object and return to parent rule. Object created had a type which belonged to one of subclasses of ASTNODE. All other node create various nodes for the parse tree. Each object created initialized with parameters as specified by subclass constructor. 

ANTLR4 has its own semantics which helps us to deal with error, that is it helps to detect where error while parsing the program, it give us the first line address where it first notices the error, even if it has multiple error in the program. It gives preference of error to class termination semicolon. Even though it occurs late in code but its preference will go to that.


