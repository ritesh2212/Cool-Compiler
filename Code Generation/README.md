In this assignment of CodeGeneration, we have introduced three other java file: ClassDTO.java, IRConstant.java, VariableScope.java.

We explicitly make ClassDTO.java which acts as, a wrapper which holds information of classes, methods, attributes, and parameters. 

IRConstants.java, In this, we have explicitly declared some of the definition of the functions like printf(), scanf(), exit(), strlen(), strcat() and malloc() so that we can dump their declaration directly over the IR file. In this, we also made some constants like space, size of int/char and some other constant required for IR representations of the Cool file and dumped it directly wherever needed.

VaribaleScope.java, In this file, we have stored attributes, its scope, its class, position in class and its type. This file is helpful in assigning the variable, and also whenever new keywords come, this is helpful so that we can make a constructor which will assign the value to all the attributes present in that class or inherited class and Inherited class attribute will be allocated first, then its class. 

Most of the functionality of this assignment are same as semantic analysis. Like initializing all the basic classes methods(IO, Object, String), making of Inheritance graph over the classes present in the list of classes. We have classesInheritanceDetails this is a hashmap which takes the class name as a key and ClassDTO as its value, so it is easier to track down the list of attributes and method a class can access. We made a method that will globally define all the constant string present in our program. 

We initialize all the attributes based on call and in a serial wise and on the adjacent address so that it is easier to track down attributes.
To handle assigned attributes we made another java(VariableScope) file to track down the location, scope, and its class name so that for inherited attribute we can assign a value to its original position, and this class only use the value as a reference and in assign operator first handle RHS of Expression that is it first calculate value for RHS statement then store the result to its address. 

To handle new keyword, we made a method that takes find the type id of expression, if typeid of expression is Int/String/Bool then dump value, assigned value, type, and new keyword its type. If typeid is other than keyword, then make the constructor of the class and assign values to attributes.

A constructor is made when a new object of a class is created, all of the inherited and local attributes must be initialized. Inherited attributes which child inherits from its parent are initialized first in inheritance order beginning with the attributes of the greatest ancestor class. In the same class they are initialized in the order they are written. The return type of constructor is void with the mangled class name.

We also dumped the code of IR for addition, multiplication, subtraction, division, less than and less than equal. First, we validate the first expression than the second operator. If either of the attributes is constant Integer, then we write it as it is else we get the address of that attribute then pass the address as a parameter of operators. First, we need to initialize the address for the attributes, and then we have to get the starting point of the address via getelementptr and length of the address. If is already assigned then it will store the value. If method caller type doesn't matches to static dispatch type caller type, then we will bitcast the return type as parent type.

We goto block of methods and validate every expression over the method we design so that its gets desired IR as an output in the .ll file. Every expression holds a different set IR code to handle or write over the file like of neg expression possess only unary operator, so we have to validate only one attributes, and after that, we assign that attributes. 

For every method in the program, we first initialize the method with the mangled name with the class name as the parameter to keep track as which class it belongs and after that, we dump the entry statement in the method. Every method returns its address from where it starts. At every method call, we conform the parameters of methods and insert it into variable scope file if some attribute is called it first take a look at parameters if it matches then it assigns or take values from there else it looks over its hierarchy to access or conform its type. 

We only handle static dispatch that is classname@classname.function_name(). So whenever it called a constructor is called automatically. After that, it goes to the argument of the method if the class type of method is of IO then it will call methods of the IO class else normal method call with or without parameter based on definition and process the expression.

We do not handle if condition, while condition, string related function like strlen(), strcat(), substr(pos,len). 

