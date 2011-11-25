package org.tiling.util;

import java.io.*;
import java.util.Arrays;

/**
 * A utility class for serializing and deserializing objects to and from
 * the file system.
 */
public class Serializer {

/*
	public static void main(String[] args) {

		File baseDirectory = new File("..\\classes");
		String extension = ".ser";

		String[] factoryNames = AbstractPrototileFactory.getFactoryNames();
		for (int i = 0; i < factoryNames.length; i++) {
			AbstractPrototileFactory factory = AbstractPrototileFactory.getFactory(factoryNames[i]);
			String[] prototileNames = factory.getPrototileNames();
			for (int j = 0; j < prototileNames.length; j++) {
				System.out.println(prototileNames[j]);
				String name = prototileNames[j];
				Prototile prototile = factory.createPrototile(name);
				serialize(prototile, new File(baseDirectory, name + extension));
			}
		}

	}
*/

	public static void serialize(Object object, File file) {
		serialize(new Object[] {object}, file);
	}

	public static void serialize(Object[] objects, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			for (int i = 0; i < objects.length; i++) {
				oos.writeObject(objects[i]);
			}
			oos.close();
			fos.close();
		} catch (IOException e) {
			System.err.println("Serialization failed: " + e.toString());
		}
	}

	public static Object deserialize(File file) {
		Object[] objs = deserialize(1, file);
		return objs == null ? null : objs[0];
	}

	public static Object[] deserialize(int n, File file) {
		try {
			Object[] obj = new Object[n];
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new SpecialObjectInputStream(fis);
			for (int i = 0; i < n; i++) {
				obj[i] = ois.readObject();
			}
			ois.close();
			fis.close();
			return obj;
		} catch (IOException e) {
			System.err.println("Deserialization failed: " + e.toString());
			return null;
		} catch (ClassNotFoundException e) {
			System.err.println("Deserialization failed: " + e.toString());
			return null;
		}

	}

/*
	public static void main(String[] args) {

		File baseDirectory = new File("..\\classes");
		String extension = ".ser";

		String[] factoryNames = AbstractPrototileFactory.getFactoryNames();
		for (int i = 0; i < factoryNames.length; i++) {
			AbstractPrototileFactory factory = AbstractPrototileFactory.getFactory(factoryNames[i]);
			String[] prototileNames = factory.getPrototileNames();
			for (int j = 0; j < prototileNames.length; j++) {
				System.out.println(prototileNames[j]);
				String name = prototileNames[j];
				Prototile prototile = factory.createPrototile(name);
				serialize(prototile, new File(baseDirectory, name + extension));
			}
		}

	}
*/

}