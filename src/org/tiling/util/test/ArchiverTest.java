package org.tiling.util.test;

import java.io.*;

import junit.framework.*;

import org.tiling.util.Archiver;

public class ArchiverTest extends TestCase {

	public ArchiverTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		return new TestSuite(ArchiverTest.class);
	}

	public void tearDown() {
		try {
			Archiver.getInstance().delete("number");
		} catch (Exception e) {
		}
	}

	public void testStoreAndRetrieve() throws IOException {
		Archiver.getInstance().store("number", new Integer(7));
		assertEquals(new Integer(7), Archiver.getInstance().retrieve("number"));

		Archiver.getInstance().store("number", new Integer(17));
		assertEquals(new Integer(17), Archiver.getInstance().retrieve("number"));
	}

	public void testStoreAndDelete() throws IOException {

		try {
			Archiver.getInstance().retrieve("number");
		} catch (IOException e) {
			assertEquals("No such archive number", e.getMessage());
		}

		Archiver.getInstance().store("number", new Integer(7));
		assertEquals(new Integer(7), Archiver.getInstance().retrieve("number"));
		Archiver.getInstance().delete("number");

		try {
			Archiver.getInstance().retrieve("number");
		} catch (IOException e) {
			assertEquals("No such archive number", e.getMessage());
		}

	}

	public void testStoreAndRetrieveUserObject() throws IOException {
		/*
		Wrapper w = new Wrapper();
		w.setInt(7);
		Archiver.getInstance().store("number", w);
		Object o = Archiver.getInstance().retrieve("number");
		assertEquals(w, o);
		*/
	} 
}