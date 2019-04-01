package cool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cool.AST.expression;
import cool.AST.formal;

import static cool.AST.*;

public class Semantic {
	private boolean errorFlag = false;

	public void reportError(String filename, int lineNo, String error) {
		errorFlag = true;
		System.err.println(filename + ":" + lineNo + ": " + error);
	}

	public boolean getErrorFlag() {
		return errorFlag;
	}

	/*
	 * Don't change code above this line
	 */
	private static final String BOOL_TYPE = "Bool";
	private static final String IO_TYPE = "IO";
	private static final String OBJECT_TYPE = "Object";
	private static final String STRING_TYPE = "String";
	private static final String INT_TYPE = "Int";
	private static final String MAIN_METHOD = "main";
	private static final String MAIN_CLASS = "Main";
	private static String fileName;
	final HashMap<String, class_> classesAvailable = new HashMap<>();
	final HashMap<String, ClassDTO> classesInheritanceDetails = new HashMap<>();
	final List<String> noInheritance = new ArrayList<>();
	final List<String> alreadyDefined = new ArrayList<>();
	private boolean isAttrORMethodFollows = true;
	private final ScopeTable<attr> scopeTable = new ScopeTable<attr>();

	public Semantic(program program) {
		// Do the initial setup
		init();

		// Check all the inheritance rules a
		boolean isHeritenceGraphCorrect = isHeritenceGraphValid(program);

		if (!isHeritenceGraphCorrect) {
			return;
		}

		// For logging purpose
		/*
		 * for (Entry<String, ClassDTO> entry : classesInheritanceDetails.entrySet()) {
		 * System.out.println(entry.getKey() + " value :  " + entry.getValue()); }
		 */

		for (class_ classDef : program.classes) {
			fileName = classDef.filename;
			scopeTable.enterScope();
			// scopeTable.insert("self", new attr("self", classDef.name, new
			// no_expr(classDef.lineNo), classDef.lineNo));
			scopeTable.insertAll(classesInheritanceDetails.get(classDef.name).getAttrList());
			validateClass(classDef);
			scopeTable.exitScope();
		}
	}

//===========================================================================================================================//

	/**
	 * 
	 * This function do all the basic initialization of the classes required in cool
	 * 
	 * Classes already present in the table: Object, IO, String, Int, Bool
	 * 
	 * Object has methods: abort() method of type 'Object', type_name() method of
	 * type 'String'.
	 * 
	 * IO has methods: out_string(x : String) of type IO, out_int(x : Int) of type
	 * IO, in_string() of type String, in_int() of type String.
	 * 
	 * String has methods: length() which return Int i.e. length of string,
	 * concat(s: String) method concat two String and return string, substr(i : Int,
	 * l : Int) method finds all substring and return String.
	 * 
	 */

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

		List<feature> IOFeatures = new ArrayList<>();
		List<formal> io_out_string = new ArrayList<formal>();
		io_out_string.add(new formal("x", STRING_TYPE, 0));
		IOFeatures.add(new method("out_string", io_out_string, IO_TYPE, new no_expr(0), 0));
		List<formal> io_out_int = new ArrayList<formal>();
		io_out_int.add(new formal("x", INT_TYPE, 0));
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

		List<feature> IntFeatures = new ArrayList<>();
		classesAvailable.put(INT_TYPE, new class_(INT_TYPE, null, OBJECT_TYPE, IntFeatures, 0));

		methodList = new HashMap<>();
		methodList.putAll(iomethodList);
		classesInheritanceDetails.put(INT_TYPE, new ClassDTO(INT_TYPE, OBJECT_TYPE, methodList, null));
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

		List<feature> boolFeatures = new ArrayList<>();
		classesAvailable.put(BOOL_TYPE, new class_(BOOL_TYPE, null, OBJECT_TYPE, boolFeatures, 0));

		methodList = new HashMap<>();
		methodList.putAll(iomethodList);
		classesInheritanceDetails.put(BOOL_TYPE, new ClassDTO(BOOL_TYPE, OBJECT_TYPE, methodList, null));

		noInheritance.add(STRING_TYPE);
		noInheritance.add(INT_TYPE);
		noInheritance.add(BOOL_TYPE);

		alreadyDefined.addAll(noInheritance);
		alreadyDefined.add(OBJECT_TYPE);
		alreadyDefined.add(IO_TYPE);
	}

//===========================================================================================================================//	

	/**
	 * Validate the various constraints
	 * 
	 * @param program
	 * @return
	 */
	private boolean isHeritenceGraphValid(program program) {
		boolean isRestrictedClsDefinedOrInherit = isRestrichedClassReDefinedOrInherited(program);
		if (isRestrictedClsDefinedOrInherit) {
			return false;
		}
		// Check the constraint for the Main class
		boolean isMainClassValid = isMainClassValid();
		if (!isMainClassValid) {
			return false;
		}

		// Check All parents are defined for the class
		boolean isParentNotDefined = isParentDefined();
		if (isParentNotDefined) {
			return false;
		}
		// Check the cycle in the class
		boolean iscycle = isCyclePresent();
		if (iscycle) {
			return false;
		}

		// populate the virtual Class table for the classes available
		for (Entry<String, class_> entry : classesAvailable.entrySet()) {
			class_ classInstance = entry.getValue();
			applyInheritenceOnClasses(classInstance);
		}
		if (!isAttrORMethodFollows) {
			return false;
		}
		return true;
	}

	/**
	 * Check whether restricted classes are redefined or inherited And also populate
	 * the classesAvailable Map
	 * 
	 * @param program
	 * @param noInheritance
	 * @param alreadyDefined
	 * @return
	 */
	private boolean isRestrichedClassReDefinedOrInherited(program program) {
		boolean isRestrictedClsDefinedOrInherit = false;
		for (class_ clazz : program.classes) {
			fileName = clazz.filename;
			String classUnderAnalysis = clazz.name;
			if (alreadyDefined.contains(classUnderAnalysis)) {
				reportError(fileName, clazz.lineNo, "Can't redefine the class : " + classUnderAnalysis);
				isRestrictedClsDefinedOrInherit = true;
			} else if (noInheritance.contains(clazz.parent)) {
				reportError(fileName, clazz.lineNo, "Can't inherit the class : " + clazz.parent);
				isRestrictedClsDefinedOrInherit = true;
			} else {
				classesAvailable.put(clazz.name, clazz);
				alreadyDefined.add(clazz.name);
			}
		}
		return isRestrictedClsDefinedOrInherit;
	}

	/**
	 * The function check the existence of the parent for the respective class
	 * 
	 * @return
	 */
	private boolean isParentDefined() {
		boolean isParentNotDefined = false;
		for (Entry<String, class_> entry : classesAvailable.entrySet()) {
			class_ classInstance = entry.getValue();
			String classParent = classInstance.parent;
			// 12345 System.out.println("Class name = " + entry.getKey() + ", Value = " +
			// classParent);
			if (!classesAvailable.containsKey(classParent) && !OBJECT_TYPE.equals(entry.getKey())) {
				reportError(classInstance.filename, classInstance.lineNo, "Parent Class doesn't exit : " + classParent);
				isParentNotDefined = true;
			}
		}
		return isParentNotDefined;
	}

	/**
	 * Check the cycle in the inheritance structure of the class
	 * 
	 * @return
	 */
	private boolean isCyclePresent() {
		boolean iscycle = false;
		for (Entry<String, class_> entry : classesAvailable.entrySet()) {
			class_ classInstance = entry.getValue();
			String className = classInstance.name;
			String classParent = classInstance.parent;
			Map<String, class_> visitedClass = new HashMap<>();
			visitedClass.put(className, classInstance);
			// System.out.println("className----------------------------------- :" +
			// className);
			while (classParent != null && !classParent.equals(OBJECT_TYPE)) {
				class_ parent = visitedClass.get(classParent);
				// System.out.println("parent :" + parent);
				if (parent != null) {
					// System.out.println("parent :" + parent.name);
					reportError(classInstance.filename, parent.lineNo, "Cycle exist  for Class " + classInstance.name);
					iscycle = true;
					break;
				} else {
					parent = classesAvailable.get(classParent);
					visitedClass.put(classParent, parent);
					classParent = parent.parent;
					// System.out.println("-----------------------------------"+classParent);
				}
			}
		}
		return iscycle;
	}

	/**
	 * Check the constraints for the Main Class
	 * 
	 * @return
	 */
	private boolean isMainClassValid() {
		boolean isMainClassValid = false;
		if (classesAvailable.containsKey(MAIN_CLASS)) {
			class_ mainClass = classesAvailable.get(MAIN_CLASS);
			for (feature f : mainClass.features) {
				if (f instanceof method && ((method) f).name.equals(MAIN_METHOD)) {
					if (((method) f).formals.size() == 0) {
						isMainClassValid = true;
						break;
					} else {
						reportError(fileName, f.lineNo, "formals paramter is not Empty");
					}
				}
			}
			if (!isMainClassValid) {
				reportError(fileName, 0, "main method not defined In Main Class");
			}
		} else {
			reportError(fileName, 0, "Main Class not defined");
		}
		return isMainClassValid;
	}

	/**
	 * Apply the resolve features of the class from the parent
	 * 
	 * @param clas
	 * @return
	 */
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

		HashMap<String, attr> tempAttrs = new HashMap<String, attr>();
		HashMap<String, method> tempMethods = new HashMap<String, method>();

		/*
		 * Checks for the following errors within a class: - multiple attribute
		 * definitions - multiple method definitions
		 */
		boolean isAttrInParent = false;
		for (feature featureInstance : clas.features) {
			if (featureInstance instanceof attr) {
				attr attribute = (attr) featureInstance;
				if (classDetails.getAttrList().containsKey(attribute.name)) {
					reportError(fileName, attribute.value.lineNo,
							"Attribute " + attribute.name + " is an attribute of an inherited class");
					isAttrORMethodFollows = false;
					isAttrInParent = true;
				}
			} else if (featureInstance instanceof method) {
				method methodVO = (method) featureInstance;
				if (tempMethods.containsKey(methodVO.name)) {
					reportError(clas.filename, methodVO.lineNo, "Method " + methodVO.name + " is multiply defined.");
					isAttrORMethodFollows = false;
				} else {
					tempMethods.put(methodVO.name, methodVO);
				}
			}
		}

		if (!isAttrInParent) {
			for (feature featureInst : clas.features) {
				if (featureInst instanceof attr) {
					attr attribute = (attr) featureInst;
					String attrName = attribute.name;
					if (tempAttrs.containsKey(attrName)) {
						reportError(clas.filename, attribute.lineNo,
								"Attribute " + attrName + " is multiply defined in class.");
						isAttrORMethodFollows = false;
					}
					tempAttrs.put(attrName, attribute);
				}
			}
		}
		classDetails.getAttrList().putAll(tempAttrs);

		for (Entry<String, method> entry : tempMethods.entrySet()) {
			if (classDetails.getMethodList().containsKey(entry.getKey())) {
				method parent_method = classDetails.getMethodList().get(entry.getKey());
				method methodVO = entry.getValue();
				List<formal> parentFormals = parent_method.formals;
				List<formal> childFormals = methodVO.formals;
				int exprLineNo = methodVO.lineNo;
				String methoName = methodVO.name;
				if (childFormals.size() != parentFormals.size()) {
					reportError(fileName, exprLineNo,
							"Incompatible number of formal parameters in redefined method " + methoName);
					isAttrORMethodFollows = false;
				} else {
					if (!methodVO.typeid.equals(parent_method.typeid)) {
						reportError(fileName, exprLineNo, "In redefined method " + methoName + ", return type "
								+ methodVO.typeid + " is different from original return type " + parent_method.typeid);
						isAttrORMethodFollows = false;
					}
					for (int i = 0; i < childFormals.size(); ++i) {
						if (!childFormals.get(i).typeid.equals(parentFormals.get(i).typeid)) {
							reportError(fileName, exprLineNo,
									"In redefined method " + methoName + ", parameter type" + childFormals.get(i).typeid
											+ " is different from original type " + parentFormals.get(i).typeid);
							isAttrORMethodFollows = false;
						}
					}
				}
			}

			if (isAttrORMethodFollows) {
				classDetails.getMethodList().put(entry.getKey(), entry.getValue());
			}
		}

		classesInheritanceDetails.put(clas.name, classDetails);
		return classDetails;
	}

//============================================================================================================================//

	/**
	 * This function takes class as its argument and differentiate the features
	 * between attributes and methods.
	 * 
	 * @param class_
	 */
	private void validateClass(class_ class_) {
		for (feature featureInstance : class_.features) {
			if (featureInstance instanceof method) {
				validateMethod((method) featureInstance); // typecast expression to method type so that it matches the
															// 'method' type of function
			} else if (featureInstance instanceof attr) {
				validateAttributes((attr) featureInstance);
			}
		}
	}

	/**
	 * This function takes method as input and checks whether the formal parameter
	 * variable are of same name. It also checks whether the declared and defined
	 * method name are of same type.
	 * 
	 * @param method
	 */
	private void validateMethod(method method) {
		scopeTable.enterScope();

		for (formal formalParameter : method.formals) {
			String argName = formalParameter.name;
			attr argument = scopeTable.lookUpLocal(argName);
			if (argument != null) {
				reportError(fileName, argument.lineNo, "Formal parameter " + argument.name + " is multiply defined.");
			}
			scopeTable.insert(argName, new attr(argName, formalParameter.typeid, new no_expr(formalParameter.lineNo),
					formalParameter.lineNo));
		}
		expression methodBody = method.body;
		validateExpr(methodBody);
		if (!conforms(methodBody.type, method.typeid)) {
			reportError(fileName, methodBody.lineNo, "Inferred return type " + methodBody.type + " of method "
					+ method.name + " does not conform to declared return type " + method.typeid);
		}
		scopeTable.exitScope();
	}

	private void validateAttributes(attr attr) {

		expression exprValue = attr.value;
		if (exprValue.getClass() != no_expr.class) {
			validateExpr(exprValue);
			if (!conforms(exprValue.type, attr.typeid)) {
				reportError(fileName, exprValue.lineNo,
						"Inferred type " + exprValue.type + " of initialization of attribute " + attr.name
								+ " does not conform to declared type " + attr.typeid);
			}
		}
	}

	private void validateExpr(expression expr) {
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
			validateAssignment((assign) expr);
		else if (expr instanceof plus)
			validatePlusExpr((plus) expr);
		else if (expr instanceof sub)
			validateSubExpr((sub) expr);
		else if (expr instanceof mul)
			validateMulExpr((mul) expr);
		else if (expr instanceof divide)
			validateDivExpr((divide) expr);
		else if (expr instanceof eq)
			validateEQExpr((eq) expr);
		else if (expr instanceof neg)
			validateNegExpr((neg) expr);
		else if (expr instanceof new_)
			validateNewExpr((new_) expr);
		else if (expr instanceof isvoid)
			validateISVoidExpr((isvoid) expr);
		else if (expr instanceof int_const)
			validateIntExpr((int_const) expr);
		else if (expr instanceof string_const)
			validateStringExpr((string_const) expr);
		else if (expr instanceof bool_const)
			validateBoolExpr((bool_const) expr);
		else if (expr instanceof cond)
			validateConditionExpr((cond) expr);
		else if (expr instanceof object)
			validateObjectExpr((object) expr);
		else if (expr instanceof static_dispatch)
			validateStaticMethodCallExpr((static_dispatch) expr);
		else if (expr instanceof dispatch)
			validateMethodCall((dispatch) expr);
		else if (expr instanceof let)
			validateLetExpr((let) expr);
		else if (expr instanceof typcase)
			validateCaseExpr((typcase) expr);
		else if (expr instanceof comp)
			validateComplementExpr((comp) expr);
		else if (expr instanceof lt)
			validateLessThanExpr((lt) expr);
		else if (expr instanceof leq)
			validateLessEqualExpr((leq) expr);
		else if (expr instanceof loop)
			validateLoopExpr((loop) expr);
		else if (expr instanceof block)
			validateBlockExpr((block) expr);
	}

	/**
	 * This function look up the symbol table for the expression whether it is
	 * declared or not. and also conform the type of the variable.
	 * 
	 * @param assign
	 */
	private void validateAssignment(assign assign) {
		expression e1 = assign.e1;
		validateExpr(e1);
		AST.attr attribute = scopeTable.lookUpGlobal(assign.name);
		String exprType = e1.type;
		if (attribute == null) {
			reportError(fileName, assign.lineNo, "Assignment to undeclared variable " + assign.name);
		} else if (!conforms(exprType, attribute.typeid)) {
			reportError(fileName, assign.lineNo,
					"Type " + exprType + " of assigned expression does not conform to declared type " + attribute.typeid
							+ " of identifier " + attribute.name);
		}
		assign.type = exprType;
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
	private void validateStaticMethodCallExpr(static_dispatch sd) {
		method meth = null;
		boolean found = false;

		int sdLineNo = sd.lineNo;
		expression methodCaller = sd.caller;
		validateExpr(methodCaller); // first process the caller.

		for (expression actualParameter : sd.actuals) { // then process all of the actual parameters (left-to-right)
			validateExpr(actualParameter);
		}
		ClassDTO classDetail = classesInheritanceDetails.get(sd.typeid);

		if (classDetail == null) {
			reportError(fileName, sdLineNo, "Static dispatch to undefined class " + sd.typeid);
		} else if (!conforms(methodCaller.type, classDetail.getName())) {
			reportError(fileName, sdLineNo, "Expression type " + methodCaller.type
					+ " does not conform to declared static dispatch type " + classDetail.getName());
		} else {
			if (classDetail.getMethodList().containsKey(sd.name)) {
				found = true;
				meth = classDetail.getMethodList().get(sd.name);
				if (sd.actuals.size() != meth.formals.size()) {
					reportError(fileName, sdLineNo, "Method " + meth.name + " invoked with wrong number of arguments.");
				} else {
					for (int i = 0; i < sd.actuals.size(); ++i) {
						String actual_type = sd.actuals.get(i).type;
						String formal_type = meth.formals.get(i).typeid;
						if (!conforms(actual_type, formal_type))
							reportError(fileName, sdLineNo, "In call of method " + meth.name + ", type " + actual_type
									+ " does not conform to declared type " + formal_type);
					}
				}
			} else {
				reportError(fileName, sdLineNo, "Static dispatch to undefined method " + sd.name);
			}
		}
		if (found) {
			sd.type = meth.typeid;
		} else {
			sd.type = OBJECT_TYPE;
		}
	}

	/**
	 * This function first process the dispatch caller, all its expressions and
	 * caller type of the dispatch. Report Error when argument of caller and calle
	 * function doesn't match, or return type doesn't match
	 * 
	 * @param dispatchExpr
	 */
	private void validateMethodCall(dispatch dispatchExpr) {
		method methodVO = null;
		boolean found = false;
		int dispatchExprLineNo = dispatchExpr.lineNo;
		expression callerExpr = dispatchExpr.caller;
		validateExpr(callerExpr);

		List<expression> actuals = dispatchExpr.actuals;
		for (expression actualParameter : actuals) {
			validateExpr(actualParameter);
		}

		ClassDTO classDetail = classesInheritanceDetails.get(callerExpr.type);

		if (classDetail == null) {
			reportError(fileName, dispatchExprLineNo, "Class " + callerExpr.type + " is undefined.");
		} else {
			String dispatchName = dispatchExpr.name;
			if (classDetail.getMethodList().containsKey(dispatchName)) {

				found = true;
				methodVO = classDetail.getMethodList().get(dispatchName);
				List<formal> formals = methodVO.formals;

				if (actuals.size() != formals.size()) {
					reportError(fileName, dispatchExprLineNo,
							"Method " + methodVO.name + " invoked with wrong number of arguments.");
				} else {
					for (int i = 0; i < actuals.size(); ++i) {
						String actualType = actuals.get(i).type;
						String formalType = formals.get(i).typeid;
						if (!conforms(actualType, formalType)) {
							reportError(fileName, dispatchExprLineNo, "In call of method " + methodVO.name + ", type "
									+ actualType + " does not conform to declared type " + formalType);
						}
					}
				}
			} else {
				reportError(fileName, dispatchExprLineNo, "Dispatch to undefined method " + dispatchName);
			}
		}
		if (found) {
			dispatchExpr.type = methodVO.typeid;
		} else {
			dispatchExpr.type = OBJECT_TYPE;
		}

	}

	/**
	 * This functions checks the predicate of function either it is bool or not, if
	 * Bool the process further and also finds least common ancestor.
	 * set the type for the conditionExpr
	 * 
	 * @param conditionExpr
	 */
	private void validateConditionExpr(cond conditionExpr) {

		expression exprPredicate = conditionExpr.predicate;
		validateExpr(exprPredicate);
		if (!exprPredicate.type.equals(BOOL_TYPE)) {
			reportError(fileName, exprPredicate.lineNo, "Predicate of 'if' does not have type Bool.");
		}
		expression condIfbody = conditionExpr.ifbody;
		expression condElsebody = conditionExpr.elsebody;
		validateExpr(condIfbody);
		validateExpr(condElsebody);
		conditionExpr.type = longestCommonAnst(condIfbody.type, condElsebody.type);
	}

	/**
	 * It sets the type of new_expr to its type id 
	 * and set to Object if type not defined
	 * 
	 * @param new_expr
	 */
	private void validateNewExpr(new_ new_expr) {
		String exprTypeid = new_expr.typeid;
		ClassDTO classDetail = classesInheritanceDetails.get(exprTypeid);
		if (classDetail == null) {
			reportError(fileName, new_expr.lineNo, "'new' used with undefined class " + exprTypeid);
			new_expr.type = OBJECT_TYPE;
		} else {
			new_expr.type = exprTypeid;
		}
	}

	/**
	 * Function assign Bool Type to isVoid.
	 * 
	 * @param isvoidExpr
	 */
	private void validateISVoidExpr(isvoid isvoidExpr) {
		isvoidExpr.type = BOOL_TYPE;
	}

	/**
	 * A Function that process the node whether both given expressions for addition are of
	 * Integer or not if . and also set the type as Int.
	 * 
	 * @param plusExpr
	 */
	private void validatePlusExpr(plus plusExpr) {
		expression e1 = plusExpr.e1;
		expression e2 = plusExpr.e2;
		validateExpr(e1);
		validateExpr(e2);
		if (!e1.type.equals(INT_TYPE) || !e2.type.equals(INT_TYPE)) {
			reportError(fileName, plusExpr.lineNo,
					"Both given expressions of plus operator aren't of Integer type : " + e1.type + " + " + e2.type);
		}
		plusExpr.type = INT_TYPE;
	}

	/**
	 * A Function that process the node whether both given expressions for subtraction are
	 * of Integer or not if . and also set the type as Int.
	 * 
	 * @param subExpr
	 */
	private void validateSubExpr(sub subExpr) {
		expression e1 = subExpr.e1;
		expression e2 = subExpr.e2;
		validateExpr(e1);
		validateExpr(e2);
		if (!e1.type.equals(INT_TYPE) || !e2.type.equals(INT_TYPE)) {
			reportError(fileName, subExpr.lineNo,
					"Both given expressions of subtract operator aren't of Integer type" + e1.type + " - " + e2.type);
		}
		subExpr.type = INT_TYPE;
	}

	/**
	 * A Function that process the node whether both given expressions for multiplication
	 * operator are of Integer or not if and also set the type as Integer.
	 * 
	 * @param mulExpr
	 */
	private void validateMulExpr(mul mulExpr) {
		expression e1 = mulExpr.e1;
		expression e2 = mulExpr.e2;
		validateExpr(e1);
		validateExpr(e2);
		if (!e1.type.equals(INT_TYPE) || !e2.type.equals(INT_TYPE)) {
			reportError(fileName, mulExpr.lineNo,
					"Both given expressions of multiply operator aren't of Integer type " + e1.type + " * " + e2.type);
		}
		mulExpr.type = INT_TYPE;
	}

	/**
	 * A Function that process the node whether both given expressions for divide operator
	 * are of Integer or not if.
	 * 
	 * @param divideExpr
	 */
	private void validateDivExpr(divide divideExpr) {
		expression e1 = divideExpr.e1;
		expression e2 = divideExpr.e2;
		validateExpr(e1);
		validateExpr(e2);
		if (!e1.type.equals(INT_TYPE) || !e2.type.equals(INT_TYPE)) {
			reportError(fileName, divideExpr.lineNo,
					"Both given expressions of divide operator aren't of Integer type " + e1.type + " / " + e2.type);
		}
		divideExpr.type = INT_TYPE;
	}

	/**
	 * Functions find out the whether the object is present in scopetable or not if
	 * present then assign its type otherwise set objectExpr type to Object
	 * 
	 * @param objectExpr
	 */
	private void validateObjectExpr(object objectExpr) {

		attr attribute = scopeTable.lookUpGlobal(objectExpr.name);
		if (attribute == null) {
			reportError(fileName, objectExpr.lineNo, "Undeclared identifier " + objectExpr.name);
			objectExpr.type = OBJECT_TYPE;
		} else {
			objectExpr.type = attribute.typeid;
		}
	}

	/**
	 * This function checks whether the predicate of loop is bool or not if it is
	 * not bool then it will print some errors and  goto body of loops and also set
	 * the type of loop as 'Object' class.
	 * 
	 * @param loopExpr
	 */
	private void validateLoopExpr(loop loopExpr) {

		expression predicateExpr = loopExpr.predicate;
		validateExpr(predicateExpr);
		if (!predicateExpr.type.equals(BOOL_TYPE)) {
			reportError(fileName, predicateExpr.lineNo, "Loop condition does not have type Bool.");
		}
		validateExpr(loopExpr.body);
		loopExpr.type = OBJECT_TYPE;
	}

	/**
	 * block consist of expression and every block has atleast one expression and the
	 * type of block is set to the type of the last expression.
	 * Iterate over all the expression and then set the type of the block
	 * 
	 * @param blockExpr
	 */
	private void validateBlockExpr(block blockExpr) {
		for (expression e : blockExpr.l1) {
			validateExpr(e);
		}
		blockExpr.type = blockExpr.l1.get(blockExpr.l1.size() - 1).type;
	}

	/**
	 * In this function the result of every expression is bound to id's until all
	 * the variables are initialized . All the Id's are visible to body's so we
	 * store that in scopeTable.
	 * 
	 * @param letExpr
	 */
	private void validateLetExpr(let letExpr) {
		expression letValue = letExpr.value;
		if (letValue.getClass() != no_expr.class) {
			validateExpr(letValue);
			if (!conforms(letValue.type, letExpr.typeid)) {
				reportError(fileName, letExpr.lineNo, "Inferred type of " + letValue.type + " of initialization" + "of "
						+ letExpr.name + " does not conform to idenitifier's declared type " + letExpr.typeid);
			}
		}
		scopeTable.enterScope();
		scopeTable.insert(letExpr.name, new attr(letExpr.name, letExpr.typeid, letValue, letExpr.lineNo));
		validateExpr(letExpr.body);
		letExpr.type = letExpr.body.type;
		scopeTable.exitScope();
	}

	/**
	 * In this function it first process the predicate of the CASE then it process
	 * all the branches of the case according to the priority Explicit runtime
	 * checks
	 * 
	 * @param typcase
	 */
	private void validateCaseExpr(typcase typcase) {
		validateExpr(typcase.predicate);
		for (branch brch : typcase.branches) {
			scopeTable.enterScope();
			ClassDTO classDetail = classesInheritanceDetails.get(brch.type);
			if (classDetail == null) {
				reportError(fileName, brch.lineNo, "Class " + brch.type + " of case branch is undefined.");
				scopeTable.insert(brch.name, new attr(brch.name, OBJECT_TYPE, brch.value, brch.lineNo)); 
			} else {
				scopeTable.insert(brch.name, new attr(brch.name, brch.type, brch.value, brch.lineNo));
			}
			validateExpr(brch.value);
			scopeTable.exitScope();
		}
		HashMap<String, Boolean> branchTypes = new HashMap<String, Boolean>();
		branch b = typcase.branches.get(0);
		String typ = b.value.type;

		for (branch br : typcase.branches) {
			if (!branchTypes.containsKey(br.type)) {
				branchTypes.put(br.type, true);
			} else {
				reportError(fileName, br.lineNo, "Duplicate branch " + br.type + " in case statement.");
			}

			typ = longestCommonAnst(typ, br.value.type);
		}
		typcase.type = typ;
	}

	/**
	 * Function assign Integer constant to variable.
	 * 
	 * @param int_constExpr
	 */
	private void validateIntExpr(int_const int_constExpr) {
		int_constExpr.type = INT_TYPE;
	}

	/**
	 * function assigns string constant to variable.
	 * 
	 * @param string_constExpr
	 */
	private void validateStringExpr(string_const string_constExpr) {
		string_constExpr.type = STRING_TYPE;
	}

	/**
	 * This function checks whether the type is of Bool or not.
	 * If not log an error. set the type to Bool
	 * 
	 * @param compExpr
	 */
	private void validateComplementExpr(comp compExpr) {

		expression e1 = compExpr.e1;
		validateExpr(e1);

		if (!e1.type.equals(BOOL_TYPE)) {
			reportError(fileName, compExpr.lineNo, "Argument of 'not' has type " + e1.type + " instead of Bool.");
		}
		compExpr.type = BOOL_TYPE;

	}

	/**
	 * A Function that process the argument of node whether the both type of less
	 * then are of same type.
	 * 
	 * @param lessThanExpr
	 */
	private void validateLessThanExpr(lt lessThanExpr) {
		expression e1 = lessThanExpr.e1;
		expression e2 = lessThanExpr.e2;
		validateExpr(e1);
		validateExpr(e2);
		if (!e1.type.equals(INT_TYPE) || !e2.type.equals(INT_TYPE)) {
			reportError(fileName, lessThanExpr.lineNo,
					"We cannot compare non Integer value" + e1.type + " < " + e2.type);
		}
		lessThanExpr.type = BOOL_TYPE;
	}

	/**
	 * A Function that process the argument of node whether the both type of less
	 * then equal to are of INT type.
	 * Set the type to bool
	 * @param lessEqual
	 */
	private void validateLessEqualExpr(leq lessEqual) {
		expression e1 = lessEqual.e1;
		expression e2 = lessEqual.e2;
		validateExpr(e1);
		validateExpr(e2);
		if (!e1.type.equals(INT_TYPE) || !e2.type.equals(INT_TYPE)) {
			reportError(fileName, lessEqual.lineNo, "We cannot compare non Integer value" + e1.type + " <= " + e2.type);
		}
		lessEqual.type = BOOL_TYPE;
	}

	/**
	 * A Function that process the argument of node whether the both type of equal
	 * operator are of INT type. If not log an error
	 * Set the type to bool
	 * @param eqExpr
	 */
	private void validateEQExpr(eq eqExpr) {
		expression e1 = eqExpr.e1;
		expression e2 = eqExpr.e2;
		validateExpr(e1);
		validateExpr(e2);
		List<String> basic_types = Arrays.asList(STRING_TYPE, INT_TYPE, BOOL_TYPE);
		if (basic_types.contains(e1.type) || basic_types.contains(e2.type)) {
			if (!e1.type.equals(e2.type)) {
				reportError(fileName, eqExpr.lineNo,
						"Comparision not possible as type of Lvalue and Rvalue are different");
			}
		}
		eqExpr.type = BOOL_TYPE;
	}

	/**
	 * Checks whether the expression is of Integer type or not. if not then report
	 * an error
	 * Set the type to Int
	 * @param negExpr
	 */
	private void validateNegExpr(neg negExpr) {
		expression e1 = negExpr.e1;
		validateExpr(e1);
		if (!e1.type.equals(INT_TYPE)) {
			reportError(fileName, negExpr.lineNo, "Argument of '~' has type " + e1.type + " instead of Int");
		}
		negExpr.type = INT_TYPE;
	}

	/**
	 * Function assign Bool type to expression.
	 * 
	 * @param bool_constExpr
	 */
	private void validateBoolExpr(bool_const bool_constExpr) {
		bool_constExpr.type = BOOL_TYPE;
	}

//===========================================================================================================================//	

	/**
	 * Function conforms whether the Type1 is the child of type2 or not
	 * 
	 * @param type1
	 * @param type2
	 * @return
	 */
	boolean conforms(String type1, String type2) {
		if (type1.equals(type2)) {
			return true;
		} else {
			if (classesInheritanceDetails.get(type1) != null) {
				type1 = classesInheritanceDetails.get(type1).getParent();
				if (type1 == null) {
					return false;
				} else {
					return conforms(type1, type2);
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * Functions finds out the longest common ancestor for both the type.
	 * 
	 * @param type_1
	 * @param type_2
	 * @return
	 */
	private String longestCommonAnst(String type_1, String type_2) {
		String type_1_temp = type_1;
		String type_2_temp = type_2;
		while (type_1_temp != null) {
			while (type_2_temp != null) {
				if (type_1_temp.equals(type_2_temp)) {
					return type_1_temp;
				}
				type_2_temp = classesInheritanceDetails.get(type_2_temp).getParent();
			}
			type_2_temp = type_2;
			type_1_temp = classesInheritanceDetails.get(type_1_temp).getParent();
		}
		return null;
	}
}