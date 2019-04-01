package cool;

import java.util.HashMap;

import cool.AST.attr;
import cool.AST.method;

public class ClassDTO {

	private String name;
	private String parent;
	private HashMap<String, method> methodList = new HashMap<>();
	private HashMap<String, attr> attrList= new HashMap<>();
	private HashMap<String, method> methodListProvided = new HashMap<>();
	public HashMap<String, method> getMethodListProvided() {
		return methodListProvided;
	}
	public void setMethodListProvided(HashMap<String, method> methodListProvided) {
		this.methodListProvided = methodListProvided;
	}
	public HashMap<String, attr> getAttrListProvided() {
		return attrListProvided;
	}
	public void setAttrListProvided(HashMap<String, attr> attrListProvided) {
		this.attrListProvided = attrListProvided;
	}
	private HashMap<String, attr> attrListProvided = new HashMap<>();
	
	private HashMap<String, VariableScope> attribteScope = new HashMap<>();
	public ClassDTO(String name, String parent, HashMap<String, method> methodList, HashMap<String, attr> attrList) {
		this.name = name;
		this.parent = parent;
		if(methodList != null) {
		this.methodList.putAll(methodList);
		}
		if(attrList!= null) {
		this.attrList.putAll(attrList);
		}
	}
	public HashMap<String, VariableScope> getAttribteScope() {
		return attribteScope;
	}
	public void setAttribteScope(HashMap<String, VariableScope> attribteScope) {
		this.attribteScope = attribteScope;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public HashMap<String, method> getMethodList() {
		return methodList;
	}
	public void setMethodList(HashMap<String, method> methodList) {
		this.methodList = methodList;
	}
	public HashMap<String, attr> getAttrList() {
		return attrList;
	}
	public void setAttrList(HashMap<String, attr> attrList) {
		this.attrList = attrList;
	}
	@Override
	public String toString() {
		return "ClassDTO [name=" + name + ", parent=" + parent + ", methodList=" + methodList + ", attrList=" + attrList
				+ "]";
	}
	
}
