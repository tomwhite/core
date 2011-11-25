package org.tiling.util.test;



import junit.framework.*;



import org.tiling.util.History;public class HistoryTest extends TestCase {
	public HistoryTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		return new TestSuite(HistoryTest.class);
	}







 
	public void test() {
		History h = new History();
		assertNull("No history", h.predict());
		h.remember("a");
		assertEquals("1", "a", h.predict());
		h.remember("b");
		assertEquals("2", "b", h.predict());
		h.remember("a");
		assertEquals("3", "b", h.predict());
		h.remember("b");
		assertEquals("4", "a", h.predict());
	}}