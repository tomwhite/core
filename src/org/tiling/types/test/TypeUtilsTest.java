package org.tiling.types.test;

import org.tiling.types.*;

import java.util.*;
import junit.framework.*;
/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:11:46)
 */
public class TypeUtilsTest extends TestCase {
/**
 * TypeTest constructor comment.
 * @param name java.lang.String
 */
public TypeUtilsTest(String name) {
	super(name);
}
/**
 * Insert the method's description here.
 * Creation date: (29/09/99 12:07:48)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	junit.ui.TestRunner.main(new String[] {TypeUtilsTest.class.getName()});
}
/**
 * Insert the method's description here.
 * Creation date: (29/09/99 12:13:32)
 * @return junit.framework.TestSuite
 */
public static TestSuite suite() {
	return new TestSuite(TypeUtilsTest.class);
}
/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:51:25)
 */
public void testGetTypes() {
	Typed a = new SimpleTyped();
	Typed b = new SimpleTyped() {

	};
	Object o = new Object();
	List list = new ArrayList();
	list.add(a);
	list.add(b);
	list.add(o);
	
	Set set = TypeUtils.getTypes(list);
	assertTrue("a", set.contains(a.getType()));
	assertTrue("b", set.contains(b.getType()));
	assertTrue("o", !set.contains(o));
}
/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:12:57)
 */
public void testRestrict() {
	Typed a = new SimpleTyped();
	Typed b = new SimpleTyped() {

	};
	Object o = new Object();
	List list = new ArrayList();
	list.add(a);
	list.add(b);
	list.add(o);

	Collection c = TypeUtils.restrict(list, b.getType());
	assertTrue(!c.contains(a));
	assertTrue(c.contains(b));
	assertTrue(!c.contains(o));

	c = TypeUtils.restrict(list, a.getType());
	assertTrue(c.contains(a));
	assertTrue(c.contains(b));
	assertTrue(!c.contains(o));
}
}