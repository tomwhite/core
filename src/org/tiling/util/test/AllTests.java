package org.tiling.util.test;

import junit.framework.*;

public class AllTests extends TestCase {

	public AllTests(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(ArchiverTest.suite());
		suite.addTest(HistoryTest.suite());
		return suite;
	}

}