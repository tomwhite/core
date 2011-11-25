package org.tiling.types.test;

import org.tiling.types.*;

import junit.framework.*;
/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:11:46)
 */
public class TypeTest extends TestCase {
/**
 * TypeTest constructor comment.
 * @param name java.lang.String
 */
public TypeTest(String name) {
	super(name);
}
/**
 * Insert the method's description here.
 * Creation date: (29/09/99 12:07:48)
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	junit.ui.TestRunner.main(new String[] {TypeTest.class.getName()});
}
/**
 * Insert the method's description here.
 * Creation date: (29/09/99 12:13:32)
 * @return junit.framework.TestSuite
 */
public static TestSuite suite() {
	return new TestSuite(TypeTest.class);
}
/**
 * I...
 * <p>
 * Creation date: (12/12/00 09:12:57)
 */
public void test() {
	Type animal = new Type("animal");
	Type bird = new Type("bird", animal);
	Type robin = new Type("robin", bird);
	Type kite = new Type("kite", bird);

	assertTrue(animal.equals(animal));
	assertTrue(bird.equals(bird));
	assertTrue(robin.equals(robin));
	assertTrue(kite.equals(kite));

	assertTrue(!animal.equals(bird));
	assertTrue(!animal.equals(robin));
	assertTrue(!bird.equals(robin));
	assertTrue(!bird.equals(kite));
	assertTrue(!kite.equals(robin));

	assertTrue(animal.isA(animal));
	assertTrue(bird.isA(bird));
	assertTrue(robin.isA(robin));
	assertTrue(kite.isA(kite));

	assertTrue(bird.isA(animal));
	assertTrue(robin.isA(bird));
	assertTrue(kite.isA(bird));

	assertTrue(!animal.isA(bird));
	assertTrue(!bird.isA(robin));
	assertTrue(!bird.isA(kite));
	assertTrue(!robin.isA(kite));
	
	assertEquals("animal", animal.toString());
	assertEquals("animal.bird.robin", robin.toString());
}
}