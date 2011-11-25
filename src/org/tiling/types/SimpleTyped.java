package org.tiling.types;

import java.util.*;

public class SimpleTyped implements Typed {
/**
 * SimpleTyped constructor comment.
 */
public SimpleTyped() {
}
	public Type getType() {
		List hierarchy = new ArrayList();
		Class clazz = getClass();
		while (clazz != null) {
			hierarchy.add(0, clazz);
			clazz = clazz.getSuperclass();
		}

		// walk down hierarchy constructing types
		Type type = null;
		for (Iterator i = hierarchy.iterator(); i.hasNext(); ) {
			clazz = (Class) i.next();
			String name = clazz.getName();
			type = new Type(name.substring(name.lastIndexOf('.') + 1), type);
		}
		return type;
	}
}