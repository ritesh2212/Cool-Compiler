package cool;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cool.AST.assign;
import cool.AST.attr;
import cool.AST.block;
import cool.AST.bool_const;
import cool.AST.class_;
import cool.AST.comp;
import cool.AST.cond;
import cool.AST.divide;
import cool.AST.eq;
import cool.AST.expression;
import cool.AST.feature;
import cool.AST.formal;
import cool.AST.int_const;
import cool.AST.isvoid;
import cool.AST.leq;
import cool.AST.loop;
import cool.AST.lt;
import cool.AST.method;
import cool.AST.mul;
import cool.AST.neg;
import cool.AST.new_;
import cool.AST.no_expr;
import cool.AST.object;
import cool.AST.plus;
import cool.AST.static_dispatch;
import cool.AST.string_const;
import cool.AST.sub;

public class Codegen {

	private static final String BOOL_TYPE = "Bool";
	private static final String IO_TYPE = "IO";
	private static final String OBJECT_TYPE = "Object";
	private static final String STRING_TYPE = "String";
	private static final String INT_TYPE = "Int";
	private static final String MAIN_METHOD = "main";
	private static final String MAIN_CLASS = "Main";
	final HashMap<String, class_> classesAvailable = new HashMap<>();
	// %class.Object = type { }, %class.IO = type { }
	final HashMap<String, ArrayList<String>> classTypeStruct = new HashMap<>();

	// String : i8*, Int : i32, Bool : i8, Object : %class.Object
	final HashMap<String, String> classTypeMap = new HashMap<>();

	final HashMap<String, String> stringConstants = new HashMap<>();
	final HashMap<String, ClassDTO> classesInheritanceDetails = new HashMap<>();
	final HashMap<String, ClassDTO> classesWO = new HashMap<>();

	int stringCount = 0;
	int methodLine = 0;
	StringBuilder methodDef;
	final HashMap<String, String> methodsDefinations = new HashMap<>();
	String classUnderConstruction;
	private final ScopeTable<VariableScope> varTable = new ScopeTable<VariableScope>();

	public Codegen(AST.program program, PrintWriter out) {
		initialize(out);
		processInheritenceGraph(program);

		for (class_ classDef : program.classes) {
			// String fileName = classDef.filename;
			varTable.enterScope();
			String className = classDef.name;
			classUnderConstruction = className;
			varTable.insertAll(classesInheritanceDetails.get(className).getAttribteScope());
			collectClassData(classDef);
			varTable.exitScope();
		}

		printClassType(out);

		printStringConsts(out);

		for (Entry<String, String> entry : methodsDefinations.entrySet()) {
			out.println(entry.getValue());
			out.println();
		}

		// declares
		printDeclareMethods(out);
	}

	private void printDeclareMethods(PrintWriter out) {
		out.println();
		out.println(IRConstants.PRINTF);
		out.println(IRConstants.SCANF);
		out.println(IRConstants.STRCAT);
		out.println(IRConstants.STRLEN);
		out.println(IRConstants.EXIT);
		out.println(IRConstants.MALLOC);
		out.println();
	}

	private void printStringConsts(PrintWriter out) {

		out.println();

		for (Entry<String, String> entry : stringConstants.entrySet()) {
			StringBuilder strInst = new StringBuilder();
			strInst.append(entry.getKey()).append(IRConstants.EQUALTO).append(entry.getValue());
			out.println(strInst);
		}
		out.println("@.st.1 = " + getStringProp("%d"));
		out.println("@.st.2 = " + getStringProp("%s"));
		out.println("@.obj.typename = " + getStringProp("Object"));
		out.println();
		out.println();
	}

	private void printClassType(PrintWriter out) {
		out.println();
		for (Entry<String, ArrayList<String>> entry : classTypeStruct.entrySet()) {
			ArrayList<String> typeList = entry.getValue();
			StringBuilder typeInst = new StringBuilder();
			typeInst.append(entry.getKey()).append(IRConstants.EQUALTO).append(IRConstants.TYPE).append(" { ");
			for (String type : typeList) {
				if (!typeList.get(typeList.size() - 1).equals(type)) {
					typeInst.append(type).append(", ");
				} else {
					typeInst.append(type);
				}
			}
			typeInst.append(" }");
			out.println(typeInst);
		}
	}

	private void processInheritenceGraph(AST.program program) {
		init();

		for (class_ clazz : program.classes) {
			classesAvailable.put(clazz.name, clazz);
		}

		for (Entry<String, class_> entry : classesAvailable.entrySet()) {
			class_ classInstance = entry.getValue();
			applyInheritenceOnClasses(classInstance);
		}

	}

	private ClassDTO applyInheritenceOnClasses(class_ clas) {

		String className = clas.name;
		String classParent = clas.parent;
		// System.out.println("parent : "+ classParent);
		if (classesInheritanceDetails.containsKey(className)) {
			return classesInheritanceDetails.get(className);
		}
		ClassDTO parentDetails = applyInheritenceOnClasses(classesAvailable.get(classParent));

		ClassDTO classDetails = new ClassDTO(className, classParent, parentDetails.getMethodList(),
				parentDetails.getAttrList());
		classDetails.getAttribteScope().putAll(parentDetails.getAttribteScope());
		
		HashMap<String, attr> tempAttrs = new HashMap<String, attr>();
		HashMap<String, method> tempMethods = new HashMap<String, method>();
		HashMap<String, VariableScope> variableList = new HashMap<String, VariableScope>();

		ArrayList<String> attrType = new ArrayList<>();
		VariableScope var;
		int attrPosition = 0;
		// attrType.add()
		for (feature featureInstance : clas.features) {
			if (featureInstance instanceof attr) {
				attr attribute = (attr) featureInstance;
				tempAttrs.put(attribute.name, attribute);
				String typeid = attribute.typeid;

				attrType.add(getClassTypeName(typeid));
				var = new VariableScope();
				var.setType(typeid);
				var.setClassName(className);
				var.setScope(IRConstants.SCOPE_CLASS);
				var.setPosition(attrPosition);
				variableList.put(attribute.name, var);
				attrPosition++;
			} else if (featureInstance instanceof method) {
				method methodVO = (method) featureInstance;
				tempMethods.put(methodVO.name, methodVO);
			}
		}
		attrType.add(getClassTypeName(clas.parent));
		String classTypeName = getClassTypeName(className);
		classTypeStruct.put(classTypeName, attrType);
		classTypeMap.put(className, classTypeName);
		classDetails.getAttrList().putAll(tempAttrs);
		classDetails.getMethodList().putAll(tempMethods);
		classDetails.getAttribteScope().putAll(variableList);
		classDetails.getAttrListProvided().putAll(tempAttrs);
		classDetails.getMethodListProvided().putAll(tempMethods);
		classesInheritanceDetails.put(clas.name, classDetails);
		return classDetails;
	}

	String getClassTypeName(String typeid) {
		String type;
		if (typeid.equals(INT_TYPE)) {
			type = "i32";
		} else if (typeid.equals(STRING_TYPE)) {
			type = "i8*";
		} else if (typeid.equals(BOOL_TYPE)) {
			type = "i8";
		} else {
			type = "%class." + typeid;
		}
		return type;
	}

	private void init() {
		List<feature> objectFeatures = new ArrayList<>();
		objectFeatures.add(new method("abort", new ArrayList<formal>(), OBJECT_TYPE, new no_expr(0), 0));
		objectFeatures.add(new method("type_name", new ArrayList<formal>(), STRING_TYPE, new no_expr(0), 0));
		classesAvailable.put(OBJECT_TYPE, new class_(OBJECT_TYPE, null, null, objectFeatures, 0));
		// has

		HashMap<String, method> iomethodList = new HashMap<>();
		iomethodList.put("abort", new method("abort", new ArrayList<formal>(), OBJECT_TYPE, new no_expr(0), 0));
		iomethodList.put("type_name", new method("type_name", new ArrayList<formal>(), STRING_TYPE, new no_expr(0), 0));

		classesInheritanceDetails.put(OBJECT_TYPE, new ClassDTO(OBJECT_TYPE, null, iomethodList, null));
		String objType = getClassTypeName(OBJECT_TYPE);
		classTypeStruct.put(objType, new ArrayList<>());
		classTypeMap.put(OBJECT_TYPE, objType);

		List<feature> IOFeatures = new ArrayList<>();
		List<formal> io_out_string = new ArrayList<formal>();
		io_out_string.add(new formal("x", STRING_TYPE, 0));
		IOFeatures.add(new method("out_string", io_out_string, IO_TYPE, new no_expr(0), 0));
		List<formal> io_out_int = new ArrayList<formal>();
		io_out_int.add(new formal("x", "Int", 0));
		IOFeatures.add(new method("out_int", io_out_int, IO_TYPE, new no_expr(0), 0));
		List<formal> io_in_string = new ArrayList<formal>();
		IOFeatures.add(new method("in_string", io_in_string, STRING_TYPE, new no_expr(0), 0));
		List<formal> io_in_int = new ArrayList<formal>();
		IOFeatures.add(new method("in_int", io_in_int, INT_TYPE, new no_expr(0), 0));
		classesAvailable.put(IO_TYPE, new class_(IO_TYPE, null, OBJECT_TYPE, IOFeatures, 0));
		//
		HashMap<String, method> methodList = new HashMap<>();
		methodList.put("out_string", new method("out_string", io_out_string, IO_TYPE, new no_expr(0), 0));
		methodList.put("out_int", new method("out_int", io_out_int, IO_TYPE, new no_expr(0), 0));
		methodList.put("in_string", new method("in_string", io_in_string, STRING_TYPE, new no_expr(0), 0));
		methodList.put("in_int", new method("in_int", io_in_int, INT_TYPE, new no_expr(0), 0));
		methodList.putAll(iomethodList);
		classesInheritanceDetails.put(IO_TYPE, new ClassDTO(IO_TYPE, OBJECT_TYPE, methodList, null));
		String ioType = getClassTypeName(IO_TYPE);
		ArrayList<String> iotypes = new ArrayList<>();
		iotypes.add(objType);
		classTypeStruct.put(ioType, iotypes);
		classTypeMap.put(IO_TYPE, ioType);

		List<feature> IntFeatures = new ArrayList<>();
		classesAvailable.put(INT_TYPE, new class_(INT_TYPE, null, OBJECT_TYPE, IntFeatures, 0));

		methodList = new HashMap<>();
		methodList.putAll(iomethodList);
		classesInheritanceDetails.put(INT_TYPE, new ClassDTO(INT_TYPE, OBJECT_TYPE, methodList, null));
		// classTypeStruct.put(getClassTypeName(INT_TYPE), new ArrayList<>());
		classTypeMap.put(INT_TYPE, IRConstants.INT_I32);
		///
		List<feature> StringFeatures = new ArrayList<>();
		List<formal> string_length = new ArrayList<formal>();
		StringFeatures.add(new method("length", string_length, INT_TYPE, new no_expr(0), 0));
		List<formal> string_concat = new ArrayList<formal>();
		string_concat.add(new formal("s", STRING_TYPE, 0));
		StringFeatures.add(new method("concat", string_concat, STRING_TYPE, new no_expr(0), 0));
		List<formal> string_substr = new ArrayList<formal>();
		string_substr.add(new formal("i", INT_TYPE, 0));
		string_substr.add(new formal("l", INT_TYPE, 0));
		StringFeatures.add(new method("substr", string_substr, STRING_TYPE, new no_expr(0), 0));
		classesAvailable.put(STRING_TYPE, new class_(STRING_TYPE, null, OBJECT_TYPE, StringFeatures, 0));

		methodList = new HashMap<>();
		methodList.put("length", new method("length", string_length, INT_TYPE, new no_expr(0), 0));
		methodList.put("concat", new method("concat", string_concat, STRING_TYPE, new no_expr(0), 0));
		methodList.put("substr", new method("substr", string_substr, STRING_TYPE, new no_expr(0), 0));
		methodList.putAll(iomethodList);
		classesInheritanceDetails.put(STRING_TYPE, new ClassDTO(STRING_TYPE, OBJECT_TYPE, methodList, null));
		// classTypeStruct.put(getClassTypeName(STRING_TYPE), new ArrayList<>());
		classTypeMap.put(STRING_TYPE, IRConstants.SRTING_I8);

		List<feature> boolFeatures = new ArrayList<>();
		classesAvailable.put(BOOL_TYPE, new class_(BOOL_TYPE, null, OBJECT_TYPE, boolFeatures, 0));

		methodList = new HashMap<>();
		methodList.putAll(iomethodList);
		classesInheritanceDetails.put(BOOL_TYPE, new ClassDTO(BOOL_TYPE, OBJECT_TYPE, methodList, null));
		// classTypeStruct.put(getClassTypeName(BOOL_TYPE), new ArrayList<>());
		classTypeMap.put(BOOL_TYPE, IRConstants.BOOL_I8);
	}

	private void collectClassData(class_ class_) {
		HashMap<String, method> methods = classesInheritanceDetails.get(class_.name).getMethodListProvided();
		HashMap<String, attr> attrs = classesInheritanceDetails.get(class_.name).getAttrListProvided();
		// for (feature featureInstance : class_.features) {
		for (Entry<String, method> entry : methods.entrySet()) {
			method methodObj = entry.getValue();
			if (class_.name.equals(MAIN_CLASS) && methodObj.name.equals(MAIN_METHOD)) {
				// StringBuilder methodDef = new StringBuilder();
				// methodDef = "define i32 @main()";
				String mainmethodDef = "define i32 @main() {\n " + "entry:\n" + "  %m = alloca %class.Main, align 4\n"
						+ "  call void @_ZN4MainC2Ev(%class.Main* %m)\n" + "  %call = call "
						+ getTypeReference(methodObj.typeid) + " @_ZN4Main4mainEv(%class.Main* %m)\n" + "  ret i32 0\n"
						+ "}\n";
				methodsDefinations.put("@main", mainmethodDef);
				// continue;
			}

			validateMethod(methodObj, class_.name); // typecast expression to method type so that it matches the
													// 'method' type of function
		}

		String classRefParam = "%this";
		String constrctor = "define linkonce_odr dso_local void " + getConstMangleName(classUnderConstruction) + "("
				+ getTypeReference(classUnderConstruction) + " " + classRefParam + ") unnamed_addr {\n" + "entry:\n";
		setMethodLine(0);
		methodDef = new StringBuilder();
		methodDef.append(constrctor);
		int attrCount = 0;
		for (Entry<String, attr> entry : attrs.entrySet()) {
			// validateAttributes(entry.getValue());

			attr attr = entry.getValue();
			expression exprValue = attr.value;
			// String
			if (exprValue.getClass() != no_expr.class) {
				String getLocInst = getInstIDInMethod("%get");
				String classType = classTypeMap.get(classUnderConstruction);
				String locationofVar = " " + getLocInst + " = getelementptr inbounds " + classType + ", " + classType
						+ "* " + classRefParam + ", i32 0, i32 " + attrCount + "\n";
				methodDef.append(locationofVar);
				String value = validateExpr(exprValue);

				if (!attr.typeid.equals(exprValue.type)) {
					String bitcast = bitcast(exprValue.type, attr.typeid, value);
					value = bitcast;
				}
				methodDef.append(storeInst(value, classTypeMap.get(attr.typeid), getLocInst));
				// checkAndAddtoStrs(exprValue);
			}

			attrCount++;
		}
		if (class_.parent != null && !class_.parent.equals(OBJECT_TYPE)) {
			String instID = bitcast(classUnderConstruction, class_.parent, classRefParam);
			methodDef.append(callConstructor(class_.parent, instID, classTypeMap.get(class_.parent)));
		}
		methodDef.append(" ret void\n" + "}\n");
		methodsDefinations.put(getConstMangleName(classUnderConstruction), methodDef.toString());
		// }

	}
	
	
	/**
	 * This function returns the mangled name of constructor of class based
	 * on the name of class and its size.
	 * 
	 * @param className
	 * @return
	 */
	private String getConstMangleName(String className) {
		// TODO Auto-generated method stub
		StringBuilder mangled = new StringBuilder();
		mangled.append("@_ZN").append(className.length()).append(className).append("C2Ev");
		return mangled.toString();
	}

	
	/**
	 * This function return mangled name of the function used in the class which is based
	 * on the its class_name size, class name,function_name size and function_name. 
	 * 
	 * @param className
	 * @param methodName
	 * @return
	 */
	private String getFunMangleName(String className, String methodName) {
		// TODO Auto-generated method stub
		StringBuilder mangled = new StringBuilder();
		mangled.append("@_ZN").append(className.length()).append(className).append(methodName.length())
				.append(methodName).append("Ev");
		return mangled.toString();
	}

	/**
	 * This function takes method as input and checks whether the formal parameter
	 * variable are of same name. It also checks whether the declared and defined
	 * method name are of same type.
	 * 
	 * @param method
	 * @param className TODO
	 * @return TODO
	 */

	String getTypeReference(String className) {

		if (className.equals(STRING_TYPE)) {
			return classTypeMap.get(className);
		}
		if (className.equals(INT_TYPE)) {
			return classTypeMap.get(className);
		}
		if (className.equals(BOOL_TYPE)) {
			return classTypeMap.get(className);
		} else {
			return classTypeMap.get(className) + "*";
		}
	}
	
	
	/**
	 * This method validate the with its class and declare the structure of method
	 * with dumping all the expression in the class with series of calls and 
	 * dumping all expression IR code and it also dumps return value and its type.  
	 * 
	 * @param method
	 * @param className
	 */
	private void validateMethod(method method, String className) {
		varTable.enterScope();
		setMethodLine(0);
		methodDef = new StringBuilder();
		// build the formal parameter list
		String classType = getTypeReference(className) + " %this";
		StringBuilder formals = new StringBuilder(classType);
		int count = 0;
		StringBuilder allocaStore = new StringBuilder();
		for (formal formal : method.formals) {
			varTable.insert(formal.name,
					new VariableScope(formal.name, IRConstants.SCOPE_METHOD, className, count, formal.typeid));
			String paramterName = "%" + formal.name + count;
			String paramtype = classTypeMap.get(formal.typeid);
			formals.append(" ,").append(paramtype).append(IRConstants.SPACE).append(paramterName);
			allocaStore.append(allocaInst(paramterName, paramtype))
					.append(storeInst(paramterName, paramtype, paramterName + ".addr"));
			count++;
			// allocaInst(prefix, allocaType)
		}

		String returnType = getTypeReference(method.typeid);
		String funMangleName = getFunMangleName(className, method.name);
		String startMethod = "define linkonce_odr " + returnType + " " + funMangleName + "(" + formals + ") {\n"
				+ "entry:\n";
		methodDef.append(startMethod);
		methodDef.append("%retval = alloca " + classTypeMap.get(method.typeid) + "\n");
		methodDef.append(allocaStore);
		expression methodBody = method.body;
		String op1 = validateExpr(methodBody);

		if (!method.typeid.equals(methodBody.type)) {
			if (!(methodBody.type.equals(INT_TYPE) || methodBody.type.equals(BOOL_TYPE)
					|| methodBody.type.equals(STRING_TYPE))) {
				String bitcast = bitcast(methodBody.type, method.typeid, op1);
				op1 = bitcast;
			} else {
				op1 = "%retval";
			}
		}

		String rt = " ret " + returnType + " " + op1 + "\n" + "}\n";
		methodDef.append(rt);
		methodsDefinations.put(funMangleName, methodDef.toString());
		varTable.exitScope();
		// checkAndAddtoStrs(methodBody);
	}
	
	
	/**
	 * Set method line number to for every method.
	 * 
	 * @param lineNum
	 */
	void setMethodLine(int lineNum) {
		methodLine = 0;
	}
	
	
	/**
	 * This functions returns the next line number for expression in the method.  
	 * 
	 * @return
	 */
	int getMethodNextLineNo() {
		return methodLine++;
	}

	
	/**
	 * This function returns the string property like private, unnamed_addr and all 
	 * which is require for dumping the code for functions.
	 * 
	 * @param str
	 * @return
	 */
	private String getStringProp(String str) {
		// TODO Auto-generated method stub
		StringBuilder strProp = new StringBuilder();
		strProp.append(IRConstants.PRIVATE).append(IRConstants.SPACE).append(IRConstants.UNNAMED_ADDR)
				.append(IRConstants.SPACE).append(IRConstants.CONSTANT).append(IRConstants.SPACE)
				.append(getStringSizematix(str)).append(IRConstants.SPACE).append("c").append("\"")
				.append(escapeSpecialCharacters(str)).append("\\00\"").append(", ").append(IRConstants.ALIGN)
				.append(IRConstants.SPACE).append(1);
		return strProp.toString();
	}

	String escapeSpecialCharacters(String text) {
		return text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\n", "\\\\0A").replaceAll("\t", "\\\\09")
				.replaceAll("\b", "\\\\08").replaceAll("\f", "\\\\0C").replaceAll("\"", "\\\\\"")
				.replaceAll("\r", "\\\\0D").replaceAll("\033", "\\\\033").replaceAll("\001", "\\\\001")
				.replaceAll("\002", "\\\\002").replaceAll("\003", "\\\\003").replaceAll("\004", "\\\\004")
				.replaceAll("\022", "\\\\022").replaceAll("\013", "\\\\013").replaceAll("\000", "\\\\000");
	}

	String getStringSizematix(String str) {

		return "[" + (str.length() + 1) + " x i8]";
	}

	private String getStrConstName(int strCount) {
		// TODO Auto-generated method stub
		return "@.str." + strCount;
	}

	private String validateExpr(expression expr) {
		/*
		 * validateXYZ functions given in the following order program = class_list class
		 * = feature = method / attribute ID <- expr [ASSIGN] <expr>.<id>(<expr>,....)
		 * [DISPATCH] <expr>@<type>.<id> (<expr>,....) [STATIC DISPATCH] if <expr> then
		 * <expr> else <expr> fi [COND] while <expr> loop <expr> pool [WHILE] { <expr>,
		 * <expr>, ... } [BLOCK] let ID : TYPE <- expr [LET-EXPR] case <expr> of ID :
		 * TYPE => expr esac [CASE] new TYPE [NEW] isvoid <expr> [ISVOID] expr + expr
		 * [PLUS] expr - expr [SUB] expr * expr [MUL] expr / expr [DIVIDE] ~expr [COMP]
		 * expr < expr [LT] expr <= expr [LEQ] expr = expr [EQ] not expr [NEG] (expr) ID
		 * [ integer [INT_CONST] string [STRING_CONST] true [BOOL_CONST] false
		 * [BOOL_CONST]
		 */
		if (expr instanceof assign)
			return validateAssignment((assign) expr);
		else if (expr instanceof plus)
			return validatePlusExpr((plus) expr);
		else if (expr instanceof sub)
			return validateSubExpr((sub) expr);
		else if (expr instanceof mul)
			return validateMulExpr((mul) expr);
		else if (expr instanceof divide)
			return validateDivExpr((divide) expr);
		else if (expr instanceof eq)
			return validateEQExpr((eq) expr);
		else if (expr instanceof neg)
			return validateNegExpr((neg) expr);
		else if (expr instanceof new_)
			return validateNewExpr((new_) expr);
		else if (expr instanceof isvoid)
			return validateISVoidExpr((isvoid) expr);
		else if (expr instanceof int_const)
			return validateIntExpr((int_const) expr);
		else if (expr instanceof string_const)
			return validateStringExpr((string_const) expr);
		else if (expr instanceof bool_const)
			return validateBoolExpr((bool_const) expr);
		else if (expr instanceof cond)
			return validateConditionExpr((cond) expr);
		else if (expr instanceof object)
			return validateObjectExpr((object) expr);
		else if (expr instanceof static_dispatch)
			return validateStaticMethodCallExpr((static_dispatch) expr);
		else if (expr instanceof comp)
			return validateCompExpr((comp) expr);
		else if (expr instanceof lt)
			return validateLessThanExpr((lt) expr);
		else if (expr instanceof leq)
			return validateLessEqualExpr((leq) expr);
		else if (expr instanceof loop)
			return validateLoopExpr((loop) expr);
		else if (expr instanceof block)
			return validateBlockExpr((block) expr);
		return null;
	}

	/**
	 * This function look up the symbol table for the expression whether it is
	 * declared or not. and also conform the type of the variable.
	 * 
	 * @param assign
	 */
	private String validateAssignment(assign assign) {
		expression e1 = assign.e1;
		String var = assign.name;
		VariableScope varProp = varTable.lookUpGlobal(var);
		String returnValue = validateExpr(e1);
		String store;
		// TODO To check whether the identifier is from the some class or formal
		String storeType = classTypeMap.get(varProp.getType());
		if (varProp.getScope().equals(IRConstants.SCOPE_CLASS)) {
			String refrenceClass = varProp.getClassName();
			String bitcast;
			String s1 = "%this";
			if (!refrenceClass.equals(classUnderConstruction)) {
				bitcast = bitcast(classUnderConstruction, refrenceClass, s1);
				s1 = bitcast;
			}
			String classType = classTypeMap.get(refrenceClass);
			String getLocInst = getInstIDInMethod("%get");
			String locationofVar = " " + getLocInst + " = getelementptr inbounds " + classType + ", " + classType + "* "
					+ s1 + ", i32 0, i32 " + varProp.getPosition() + "\n";
			methodDef.append(locationofVar);
			if (!e1.type.equals(varProp.getType())) {
				getLocInst = bitcast(e1.type, varProp.getType(), getLocInst);

			}
			store = storeInst(returnValue, storeType, getLocInst);
			methodDef.append(store);
			String load = getInstIDInMethod("%load");
			methodDef.append(" ").append(load).append(" = load ").append(storeType).append(", ").append(storeType)
					.append("* ").append(getLocInst).append("\n");
			return load;
		} else {
			String getLocInst = "%" + var + varProp.getPosition() + ".addr";
			if (!e1.type.equals(varProp.getType())) {
				getLocInst = bitcast(e1.type, varProp.getType(), getLocInst);
			}
			store = storeInst(returnValue, storeType, getLocInst);
			methodDef.append(store);
			String load = getInstIDInMethod("%load");
			methodDef.append(" ").append(load).append(" = load ").append(storeType).append(", ").append(storeType)
					.append("* ").append(getLocInst).append("\n");
			return load;
		}
		// parameter
	}

	private String storeInst(String value, String storeType, String getLocInst) {
		return " store " + storeType + " " + value + ", " + storeType + "* " + getLocInst + "\n";
	}

	private String allocaInst(String prefix, String allocaType) {
		// String load = getInstIDInMethod("%load");
		return " " + prefix + ".addr = alloca " + allocaType + "\n";
	}

	private String bitcast(String from, String to, String loc) {
		String bitcast;
		bitcast = "%bitcast" + getMethodNextLineNo();
		methodDef.append(" ").append(bitcast)
				.append(" = bitcast " + getTypeReference(from) + " " + loc + " to " + getTypeReference(to) + "\n");
		return bitcast;
	}

	/**
	 * This function first process the caller of the function, then we process all
	 * the argument of the function. Caller type must conform the type of called
	 * function. Error when dispatched method not present, number of argument not
	 * matched or the type of argument not conform i.e. argument of actual and
	 * formal parameter not matched.
	 * 
	 * @param sd
	 */
	private String validateStaticMethodCallExpr(static_dispatch sd) {
		String callKey = "%call" + getMethodNextLineNo();
		String returnValue = null;
		expression methodCaller = sd.caller;
		String calleeref = validateExpr(methodCaller); // first process the caller.
		// checkAndAddtoStrs(methodCaller);
		String methodeName = sd.name;
		String callInst = null;
		String typeID = sd.typeid;
		StringBuilder act = new StringBuilder();
		boolean modifyList = false;
		boolean isloadrequired = false;
		if (typeID.equals(IO_TYPE)) {
			if (methodeName.equals("out_string")) {
				callInst = " " + callKey + " = call i32 (i8*, ...) @printf( ";
				modifyList = true;
				returnValue = calleeref;
			} else if (methodeName.equals("in_string")) {
				String temp = "%tmp" + getMethodNextLineNo();
				methodDef.append(" ").append(temp).append(" = ").append("call noalias i8* @malloc(i64 8192)")
						.append(IRConstants.NEW_LINE);
				callInst = " " + callKey
						+ " = call i32 (i8*, ...) @scanf( i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.st.2, i32 0, i32 0), i8* "
						+ temp;
				
				returnValue = temp;
			} else if (methodeName.equals("out_int")) {
				callInst = " " + callKey
						+ " = call i32 (i8*, ...) @printf( i8*  getelementptr inbounds ([3 x i8], [3 x i8]* @.st.1, i32 0, i32 0) ";
				returnValue = calleeref;
			} else if (methodeName.equals("in_int")) {
				String temp = "%tmp" + getMethodNextLineNo();
				methodDef.append(" ").append(temp).append(" = ").append("alloca i32").append(IRConstants.NEW_LINE);
				callInst = " " + callKey
						+ " = call i32 (i8*, ...) @scanf( i8*  getelementptr inbounds ([3 x i8], [3 x i8]* @.st.1, i32 0, i32 0), i32* "
						+ temp;
				isloadrequired = true;
				returnValue = temp;
			}
		} else if (typeID.equals(OBJECT_TYPE)) {
			if (methodeName.equals("abort")) {
				callInst = " " + callKey + " = call void @exit(i32 0";
			} else if (methodeName.equals("type_name")) {
				callInst = " " + callKey
						+ " = call i32 (i8*, ...) @printf( i8*  getelementptr inbounds ([6 x i8], [6 x i8]* @.obj.typename, i32 0, i32 0) ";
			}
			// methodDef.append(callInst);
		} else {

			if (!methodCaller.type.equals(sd.typeid)) {
				String newInt = "%bitcast" + getMethodNextLineNo();
				methodDef.append(newInt).append(" = bitcast " + getTypeReference(methodCaller.type) + " " + calleeref
						+ " to " + getTypeReference(sd.typeid) + "\n");
				calleeref = newInt;
			}
			callInst = " " + callKey + " = call " + getTypeReference(sd.type) + " "
					+ getFunMangleName(sd.typeid, sd.name) + "(";
			// methodDef.append(callInst);
			String classType = getTypeReference(sd.typeid) + " " + calleeref;
			act.append(classType);
			returnValue = callKey;
		}
		String parameters;

		int count = 0;
		for (expression actualParameter : sd.actuals) { // then process all of the actual parameters (left-to-right)
			parameters = validateExpr(actualParameter);
			if (!modifyList) {
				act.append(" ,");
			}

			act.append(classTypeMap.get(actualParameter.type)).append(IRConstants.SPACE).append(parameters);
		}
		methodDef.append(callInst).append(act).append(")").append(IRConstants.NEW_LINE);

		if (isloadrequired) {
			String load = getInstIDInMethod("%load");
			methodDef.append(" ").append(load).append(" = load ").append("i32").append(", ").append("i32").append("* ")
					.append(returnValue).append("\n");
			returnValue = load;
		}
		return returnValue;
	}

	/*
	 * private void checkAndAddtoStrs(expression expr) {
	 * 
	 * if(expr instanceof string_const) { stringCount++;
	 * stringConstants.put(getStrConstName(stringCount),
	 * getStringProp(((string_const) expr).value)); } }
	 */

	/**
	 * This functions checks the predicate of function either it is bool or not, if
	 * Bool the process further and also finds least common ancestor. set the type
	 * for the conditionExpr
	 * 
	 * @param conditionExpr
	 */
	private String validateConditionExpr(cond conditionExpr) {

		expression exprPredicate = conditionExpr.predicate;
		validateExpr(exprPredicate);
		expression condIfbody = conditionExpr.ifbody;
		expression condElsebody = conditionExpr.elsebody;
		validateExpr(condIfbody);
		validateExpr(condElsebody);
		return null;
	}

	/**
	 * It sets the type of new_expr to its type id and set to Object if type not
	 * defined
	 * 
	 * @param new_expr
	 */
	private String validateNewExpr(new_ new_expr) {

		String newType = new_expr.type;
		String returnNew = "%new" + getMethodNextLineNo();
		methodDef.append(" " + returnNew + " = alloca " + classTypeMap.get(newType) + "\n");
		String type = null;
		String store;
		if (newType.equals(INT_TYPE)) {
			type = "i32";
			store = storeInst("0", type, returnNew);
			methodDef.append(store);
		} else if (newType.equals(STRING_TYPE)) {
			type = "i8*";
			store = storeInst("", type, returnNew);
			methodDef.append(store);
		} else if (newType.equals(BOOL_TYPE)) {
			type = "i8";
			store = storeInst("0", type, returnNew);
			methodDef.append(store);
		} else if (!(newType.equals(OBJECT_TYPE) || newType.equals(IO_TYPE))) {
			type = "%class." + newType;
			methodDef.append(callConstructor(newType, returnNew, type));
		}
		return returnNew;
	}

	private String callConstructor(String className, String classRef, String type) {
		return " call void " + getConstMangleName(className) + "(" + type + "* " + classRef + ")\n";
	}
	

	/**
	 * Not Found
	 * 
	 * @param isvoidExpr
	 */
	private String validateISVoidExpr(isvoid isvoidExpr) {
		// isvoidExpr.
		if (isvoidExpr.e1 instanceof no_expr) {

		}

		isvoidExpr.type = BOOL_TYPE;
		return null;
	}
	

	/**
	 * A Function that process the node whether both given expressions for addition
	 * are of Integer or not if . and also set the type as Int.
	 * 
	 * @param plusExpr
	 */
	private String validatePlusExpr(plus plusExpr) {
		expression e1 = plusExpr.e1;
		expression e2 = plusExpr.e2;
		String op1 = validateExpr(e1);
		String op2 = validateExpr(e2);
		String returnAdd = "%add" + getMethodNextLineNo();
		methodDef.append(" " + returnAdd + " = add nsw i32 " + op1 + ", " + op2 + "\n");
		return returnAdd;
	}

	/**
	 * A Function that process the node whether both given expressions for
	 * subtraction are of Integer or not if . and also set the type as Int.
	 * 
	 * @param subExpr
	 */
	private String validateSubExpr(sub subExpr) {
		expression e1 = subExpr.e1;
		expression e2 = subExpr.e2;
		String op1 = validateExpr(e1);
		String op2 = validateExpr(e2);
		String returnAdd = "%sub" + getMethodNextLineNo();
		methodDef.append(" " + returnAdd + " = sub nsw i32 " + op1 + ", " + op2 + "\n");
		return returnAdd;
	}
	

	/**
	 * A Function that process the node whether both given expressions for
	 * multiplication operator are of Integer or not if and also set the type as
	 * Integer.
	 * 
	 * @param mulExpr
	 */
	private String validateMulExpr(mul mulExpr) {
		expression e1 = mulExpr.e1;
		expression e2 = mulExpr.e2;
		String op1 = validateExpr(e1);
		String op2 = validateExpr(e2);
		String returnAdd = "%mul" + getMethodNextLineNo();
		methodDef.append(" " + returnAdd + " = mul nsw i32 " + op1 + ", " + op2 + "\n");
		return returnAdd;
	}

	/**
	 * A Function that process the node whether both given expressions for divide
	 * operator are of Integer or not if.
	 * 
	 * @param divideExpr
	 */
	private String validateDivExpr(divide divideExpr) {
		expression e1 = divideExpr.e1;
		expression e2 = divideExpr.e2;
		String op1 = validateExpr(e1);
		String op2 = validateExpr(e2);
		String returnAdd = "%div" + getMethodNextLineNo();
		methodDef.append(" " + returnAdd + " = sdiv i32 " + op1 + ", " + op2 + "\n");
		return returnAdd;
	}

	/**
	 * Functions find out the whether the object is present in scopetable or not if
	 * present then assign its type otherwise set objectExpr type to Object
	 * 
	 * @param objectExpr
	 */
	private String validateObjectExpr(object objectExpr) {
		// TODO Get t
		String objectAddress = objectExpr.name;
		VariableScope varProp = varTable.lookUpGlobal(objectAddress);
		String storeType = classTypeMap.get(varProp.getType());
		if (varProp.getScope().equals(IRConstants.SCOPE_CLASS)) {
			String refrenceClass = varProp.getClassName();
			String s1 = "%this";
			
			//classDetails.getAttribteScope().putAll(parentDetails.getAttribteScope());
		    //
			String bitcast;
		            if (!refrenceClass.equals(classUnderConstruction)) {
		                bitcast = bitcast(classUnderConstruction, refrenceClass, s1);
		                s1 = bitcast;
		            }
		     //

			String classType = classTypeMap.get(refrenceClass);
			String getLocInst = getInstIDInMethod("%get");
			String locationofVar = " " + getLocInst + " = getelementptr inbounds " + classType + ", " + classType + "* "
					+ s1 + ", i32 0, i32 " + varProp.getPosition() + "\n";
			methodDef.append(locationofVar);
			String load = getInstIDInMethod("%load");
			methodDef.append(" ").append(load).append(" = load ").append(storeType).append(", ").append(storeType)
					.append("* ").append(getLocInst).append("\n");
			return load;
		} else {
			String getLocInst = "%" + objectAddress + varProp.getPosition() + ".addr";
			String load = getInstIDInMethod("%load");
			methodDef.append(" ").append(load).append(" = load ").append(storeType).append(", ").append(storeType)
					.append("* ").append(getLocInst).append("\n");
			return load;
		}
	}

	/**
	 * This function checks whether the predicate of loop is bool or not if it is
	 * not bool then it will print some errors and goto body of loops and also set
	 * the type of loop as 'Object' class.
	 * 
	 * @param loopExpr
	 */
	private String validateLoopExpr(loop loopExpr) {

		expression predicateExpr = loopExpr.predicate;
		validateExpr(predicateExpr);

		validateExpr(loopExpr.body);
		// checkAndAddtoStrs(loopExpr.body);
		return null;
	}

	/**
	 * block consist of expression and every block has atleast one expression and
	 * the type of block is set to the type of the last expression. Iterate over all
	 * the expression and then set the type of the block
	 * 
	 * @param blockExpr
	 */
	private String validateBlockExpr(block blockExpr) {
		String op = "";
		for (expression e : blockExpr.l1) {
			op = validateExpr(e);
		}
		return op;
	}

	/**
	 * Function assign Integer constant to variable.
	 * 
	 * @param int_constExpr
	 */
	private String validateIntExpr(int_const int_constExpr) {
		// int_constExpr.type = INT_TYPE;
		return String.valueOf(int_constExpr.value);
	}

	/**
	 * function assigns string constant to variable.
	 * 
	 * @param string_constExpr
	 */
	private String validateStringExpr(string_const string_constExpr) {
		// string_constExpr.type = STRING_TYPE;
		stringCount++;
		String str = getStrConstName(stringCount);
		stringConstants.put(str, getStringProp(string_constExpr.value));
		String matrix = getStringSizematix(string_constExpr.value);
		return "getelementptr inbounds (" + matrix + ", " + matrix + "* " + str + ", i32 0, i32 0)";
	}

	/**
	 * This function checks whether the type is of Bool or not. If not log an error.
	 * set the type to Bool
	 * 
	 * @param compExpr
	 */
	private String validateCompExpr(comp compExpr) {

		expression e1 = compExpr.e1;
		String bool = validateExpr(e1);
		String inst1 = getInstIDInMethod("%tobool");
		String inst2 = getInstIDInMethod("%lnot");
		String inst3 = getInstIDInMethod("%frombool");
		String comp = " " + inst1 + " = trunc i8 " + bool + " to i1\n" + " " + inst2 + " = xor i1 " + inst1 + ", true\n"
				+ "  " + inst3 + " = zext i1 " + inst2 + " to i8\n";
		methodDef.append(comp);
		return inst3;
	}

	String getInstIDInMethod(String prefix) {
		return prefix + getMethodNextLineNo();
	}

	/**
	 * A Function that process the argument of node whether the both type of less
	 * then are of same type.
	 * 
	 * @param lessThanExpr
	 */
	private String validateLessThanExpr(lt lessThanExpr) {
		expression e1 = lessThanExpr.e1;
		expression e2 = lessThanExpr.e2;
		String op1 = validateExpr(e1);
		String op2 = validateExpr(e2);
		String inst1 = getInstIDInMethod("%cmp");
		String cmp = " " + inst1 + " = icmp slt i32 " + op1 + ", " + op2 + "\n";
		methodDef.append(cmp);
		return inst1;
	}

	/**
	 * A Function that process the argument of node whether the both type of less
	 * then equal to are of INT type. Set the type to bool
	 * 
	 * @param lessEqual
	 */
	private String validateLessEqualExpr(leq lessEqual) {
		expression e1 = lessEqual.e1;
		expression e2 = lessEqual.e2;
		String op1 = validateExpr(e1);
		String op2 = validateExpr(e2);
		String inst1 = getInstIDInMethod("%cmp");
		String cmp = " " + inst1 + " = icmp sle i32 " + op1 + ", " + op2 + "\n";
		methodDef.append(cmp);
		return inst1;
	}

	/**
	 * A Function that process the argument of node whether the both type of equal
	 * operator are of INT type. If not log an error Set the type to bool
	 * 
	 * @param eqExpr
	 */
	private String validateEQExpr(eq eqExpr) {
		expression e1 = eqExpr.e1;
		expression e2 = eqExpr.e2;
		String op1 = validateExpr(e1);
		// checkAndAddtoStrs(e1);
		String op2 = validateExpr(e2);
		String inst1 = getInstIDInMethod("%cmp");
		String cmp = " " + inst1 + " = icmp eq i32 " + op1 + ", " + op2 + "\n";
		methodDef.append(cmp);
		return inst1;
	}

	/**
	 * Checks whether the expression is of Integer type or not. if not then report
	 * an error Set the type to Int
	 * 
	 * @param negExpr
	 */
	private String validateNegExpr(neg negExpr) {
		expression e1 = negExpr.e1;
		String op = validateExpr(e1);
		String inst1 = getInstIDInMethod("%sub");
		String neg = " " + inst1 + " = sub nsw i32 0, " + op + "\n";
		methodDef.append(neg);
		return inst1;
	}

	/**
	 * Function assign Bool type to expression.
	 * 
	 * @param bool_constExpr
	 */
	private String validateBoolExpr(bool_const bool_constExpr) {
		bool_constExpr.type = BOOL_TYPE;
		return String.valueOf(bool_constExpr.value);
	}

	void initialize(PrintWriter out) {
		printTargetDataLayout(out);
		printTargetSystem(out);
	}

	private void printTargetSystem(PrintWriter out) {
		StringBuilder target = new StringBuilder();
		target.append(IRConstants.TARGET).append(IRConstants.SPACE).append(IRConstants.TRIPLE)
				.append(IRConstants.EQUALTO).append(IRConstants.TARGET_TRIPLE);
		out.println(target);
	}

	private void printTargetDataLayout(PrintWriter out) {
		StringBuilder target = new StringBuilder();
		target.append(IRConstants.TARGET).append(IRConstants.SPACE).append(IRConstants.DATALAYOUT)
				.append(IRConstants.EQUALTO).append(IRConstants.TARGET_DATALAYOUT);
		out.println(target);
	}

}
