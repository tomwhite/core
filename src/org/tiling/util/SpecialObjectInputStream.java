package org.tiling.util;

import java.io.*;

class SpecialObjectInputStream extends ObjectInputStream {

	SpecialObjectInputStream(FileInputStream fis) throws IOException, StreamCorruptedException {
		super(fis);
	}

	/**
	 * Overridden to load classes from jars, if not in java.* or javax.* hierarchy.
	 * @see JarManager
	 */
	protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
		String className = v.getName();
		if (className.startsWith("java") || className.startsWith("[Ljava")) {
			return super.resolveClass(v);
		} else {
//			System.out.println("resolving " + v.getName());
			Class c = JarManager.getInstance().loadClass(className);
			return c;
		}
	}

}