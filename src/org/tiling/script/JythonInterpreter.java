package org.tiling.script;

import java.io.File; 

import org.python.util.PythonInterpreter; 
import org.python.core.*;

/**
 * I run Jython scripts.
 */
public class JythonInterpreter {

	private PythonInterpreter interpreter;

	public JythonInterpreter() {
		interpreter = new PythonInterpreter();
	}

	public void execute(File file) throws PyException {
		interpreter.execfile(file.getAbsolutePath());
	}

	public Object get(String name, Class javaClass) {
		return interpreter.get(name, javaClass);
	}
 
	public void set(String name, Object object) {
		interpreter.set(name, object);
	}

	public static void main(String args[]) {
		if (args.length == 1) {
			new JythonInterpreter().execute(new File(args[0]));
		} else {
			System.err.println("Specify a jython file to run");
			System.exit(1);
		}
	}}