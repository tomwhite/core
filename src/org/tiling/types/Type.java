package org.tiling.types;

public class Type {

	private String name;
	private Type parent;

	public Type(String name) {
		this(name, null);
	}
	public Type(String name, Type parent) {
		this.name = name;
		this.parent = parent;
	}
	public boolean equals(Object anotherType) {
		if (anotherType instanceof Type) {
			Type type = (Type) anotherType;
			return name.equals(type.name) && (parent == null ? type.parent == null : parent.equals(type.parent));
		}
		return false;
	}
	public String getName() {
		return name;
	}
	public Type getParent() {
		return parent;
	}
/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:56:34)
 * @return int
 */
public int hashCode() {
	int hashCode = name.hashCode();
	if (parent != null) {
		hashCode ^= parent.hashCode();
	}
	return hashCode;
}
	public boolean isA(Type type) {
		return equals(type) || (parent != null && parent.isA(type));
	}
public String toString() {
	if (parent == null) {
		return name;
	} else {
		return parent.toString() + "." + name;
	}
}
}