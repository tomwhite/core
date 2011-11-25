package org.tiling.util;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;

/**
 * A JarManager maintains the list of jars known to the application.
 * It also provides class loading services.
 */
public class JarManager implements Serializable {

	private Set jarURLs;
	private transient ClassLoader classLoader;

	private static JarManager instance = new JarManager();

	private JarManager() {
		jarURLs = new HashSet();
		classLoader = ClassLoader.getSystemClassLoader();
	}

	private JarManager(Set jarURLs) {
		this.jarURLs = jarURLs;
		classLoader = new URLClassLoader((URL[]) jarURLs.toArray(new URL[0]));
	}

	public static JarManager getInstance() {
		return instance;
	}

	private void readObject(ObjectInputStream stream) throws IOException {
		try {
			stream.defaultReadObject();
			instance = new JarManager(jarURLs);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean addURL(String jarURL) {
		try {
			return addURL(new URL(jarURL));
		} catch (MalformedURLException e) {
			return false;
		}
	}

	public boolean addURL(URL jarURL) {
		boolean added = jarURLs.add(jarURL);
		if (added) {
			classLoader = new URLClassLoader((URL[]) jarURLs.toArray(new URL[0]));
		}
		return added;
	}

	public Set getJarURLs() {
		return Collections.unmodifiableSet(jarURLs);
	}

	public Class loadClass(String className) throws ClassNotFoundException {
		return classLoader.loadClass(className);	
	}

	/**
	 * @return a Set of objects of type clazz from a jar specified by jarURL.
	 */
	public Set instantiateFromJar(String jarURL, Class clazz) {
		Set objects = new HashSet();

		try {
			addURL(jarURL);
			URL jarFormatURL = new URL("jar:" + jarURL + "!/");
			JarURLConnection uc = (JarURLConnection) jarFormatURL.openConnection();
			for (Enumeration enum = uc.getJarFile().entries(); enum.hasMoreElements(); ) {
				JarEntry entry = (JarEntry) enum.nextElement();
				String entryName = entry.getName();
				if (entryName.endsWith(".class")) {
					String className = entryName.substring(0, entryName.lastIndexOf('.'));
					className = className.replace('/', '.');
					Class c = classLoader.loadClass(className);
					if (clazz.isAssignableFrom(c)) {
						objects.add(c.newInstance());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.unmodifiableSet(objects);
	}

	/**
	 * @return a Set of class names of type clazz from a jar specified by jarURL.
	 */
	public Set find(String jarURL, Class clazz) {
		Set classNames = new TreeSet();

		try {
			addURL(jarURL);
			URL jarFormatURL = new URL("jar:" + jarURL + "!/");
			JarURLConnection uc = (JarURLConnection) jarFormatURL.openConnection();
			for (Enumeration enum = uc.getJarFile().entries(); enum.hasMoreElements(); ) {
				JarEntry entry = (JarEntry) enum.nextElement();
				String entryName = entry.getName();
				if (entryName.endsWith(".class")) {
					String className = entryName.substring(0, entryName.lastIndexOf('.'));
					className = className.replace('/', '.');
					if (clazz.isAssignableFrom(classLoader.loadClass(className))) {
						classNames.add(className);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.unmodifiableSet(classNames);
	}

}