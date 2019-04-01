package cool;

import cool.AST.attr;

public class VariableScope {

	private String name;
	private String scope;
	private String className;
	private int position;
	private String type;
	public VariableScope() {
		// TODO Auto-generated constructor stub
	}
	public VariableScope(String name, String scope, String className, int position, String type) {
		super();
		this.name = name;
		this.scope = scope;
		this.className = className;
		this.position = position;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}

}
