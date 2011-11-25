package org.tiling.types.test;

import org.tiling.types.*;

import junit.framework.*;

/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:21:21)
 */
public class TypedTest extends TestCase {
/**
 * TypedTest constructor comment.
 * @param name java.lang.String
 */
public TypedTest(String name) {
	super(name);
}
/**
 * Insert the method's description here.
 * Creation date: (29/09/99 12:07:48)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	junit.ui.TestRunner.main(new String[] {TypedTest.class.getName()});
}
/**
 * Insert the method's description here.
 * Creation date: (29/09/99 12:13:32)
 * @return junit.framework.TestSuite
 */
public static TestSuite suite() {
	return new TestSuite(TypedTest.class);
}
/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:21:45)
 */
public void test() {
	Typed a = new SimpleTyped();
	Typed b = new SimpleTyped() {

	};

	Typed c = new SimpleTyped() {
		public Type getType() {
			return new Type("c", super.getType());
		}
	};

	Typed d = new SimpleTyped() {
		public Type getType() {
			return new Type("d", super.getType());
		}
	};

	assertTrue(b.getType().isA(a.getType()));
	assertTrue(c.getType().isA(a.getType()));
	assertTrue(d.getType().isA(a.getType()));
	
	assertTrue(!a.getType().isA(b.getType()));
	assertTrue(!a.getType().isA(c.getType()));
	assertTrue(!a.getType().isA(d.getType()));
	
	assertTrue(!b.getType().isA(c.getType()));
	assertTrue(!b.getType().isA(d.getType()));
	assertTrue(!c.getType().isA(b.getType()));
	assertTrue(!d.getType().isA(b.getType()));
	assertTrue(!c.getType().isA(d.getType()));
	assertTrue(!d.getType().isA(c.getType()));
}
}