Semantic Analysis main function is type checking and inheritance resolution.
In this assignment, we have to run two passes over the AST(Abstract Syntax Tree). After the passes are over, we get the annotated syntax tree. In this we do not handle any SELF_TYPE keyword as given the problem statement.

In the code level design, we write semantic rules in Semantic.java, and we also made few changes in AST.java. We explicitly make ClassDTO.java which acts as, a wrapper which holds information of classes, methods, attributes and parameters. Errors are handles by the reportError method which returns the file name, line number and the type of semantic errors which occur.

First thing, we do the initialization of the all the basic classes like object class of the main class, four sub classes like IO, Int, Bool, and String. We also have some method for String and IO. 

We have made two passes:
    First pass: Inheritance Graph.
    Second pass: annotating the Inheritance graph by applying all semantic rules.
    
Inheritance Graph:
    Multiple declarations of same class name or invalid hierarchy then it must report Error else maintaining the list of available classes.
    
    Checking if Main class is available in the list of classes, if it exists there then check it must contain main method else report error.
    
    Parent class of the child class must exist else report error.
    
    We check the inheritance cycle, if it exists in the Inheritance graph then report error and exit.
	
	Applying inheritance property on the class, In this we follow bottom up approach. So our approach is to take a class, check if it has any parent class exist then we go to that parent class 		and bubble up to the Object class. After that we make a wrapper which stores the content of that class (method and attributes) and iterate it down to the lower class and add the methods and  		attrribute of upper class to its content and do this till we find the class it return.   

    
LEAST COMMON ANCESTOR: In this we iterate over the graph to find the least common ancestor/parent of two different type of expression.

METHODS: We cannot have multiple method declaration of the same name in the same class. This type of error is reported in the second pass. On the first pass, we store the instance of all method in the ClassDTO.java.

FORMAL PARAMETERS OF METHOD: Formal parameters are traverse from left to right. If parameter is redefined then report Error and also it must conform with the actual parameter wherever it is called.

LET EXPRESSION: In let, we conform the let value type and its type id if it is false we report Error. We also insert all the attributes in the scopeTable and lastly we set the let type.

TYPECASE: Every expression in the case must be validated to its type. Every branch in the case must have valid type defined. If Identical branch exist then report error. Return type of each expression type of the branch must conform with the child class declared. Return type of each case statement must conform with the type branches by the Least Common Ancestor.  

ISVOID: Type must be set to BOOL.

STATIC DISPATCH: All the method of this type must be declared bfore use. It should have valid name in the class. The actual and formal parameter must conform.The type of all the expression must be valid to the type name of the class. Its return type must conform with the type of expression.

DISPATCH: It is checked within the static dispatch and it conform all the rules of static dispatch.

CONDITION: All the predicate it is having must be of bool type. Its sets type of if condition is the lca of then and else expression.  

LOOP: In this also predicate type must be of bool type and it sets the type of loop to Object.

OBJECT CLASS: We check the given object and search it in scopeTable. if it is not present in scope table it will report Error.

BLOCK: Every statement of the block/scope body is evaluated and it sets the type of block to the last statement it access in the body.
	
ASSIGNMENT: The identifier object must conform within the scope or its lca. The expression on the RHS of the assignment must conform with the expression on the LHS.

PLUS: Both the operand must be int. Type of plus must be set to Int.

MINUS: Both the operand must be int. Type of minus must be set to Int.

DIVIDE: Both the operand must be int. Type of divide must be set to Int.

MULTIPLY: Both the operand must be int. Type of mutiply must be set to Int.


Input File:
Demo 1: Let Expression and while loop.
Demo 2: Cases expression.
Demo 3: Let and return Bool.
Demo 4: Comparator file and negation of operand.
Demo 5: Using parent class attribute in child class.
Demo 6: Let Expression.
Bad 1: Cycle exist.
Bad 2: Multiple method of same name.
Bad 3: Duplicate branch Int in case expression. 
Bad 4: Multiple Argument of same name.
Bad 5: Multiple attribute declaration.
