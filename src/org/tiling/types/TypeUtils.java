package org.tiling.types;

import java.util.*;

/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:42:42)
 */
public class TypeUtils {
private TypeUtils() {
}
/**
 * I return a set of all the types in the given collection.
 */
public static Set getTypes(Collection collection) {
	Set set = new HashSet();
	for (Iterator i = collection.iterator(); i.hasNext(); ) {
		Object o = i.next();
		if (o instanceof Typed) {
			Typed typed = (Typed) o;
			set.add(typed.getType());
		}
	}
	return set;
}
/**
 * I return a view of the given collection, restricted to objects of the given type.
 */
public static Collection restrict(Collection collection, Type type) {
	Collection c = new ArrayList();
	for (Iterator i = collection.iterator(); i.hasNext(); ) {
		Object o = i.next();
		if (o instanceof Typed) {
			Typed typed = (Typed) o;
			if (typed.getType().isA(type)) {
				c.add(typed);
			}
		}
	}
	return Collections.unmodifiableCollection(c);	
}
}