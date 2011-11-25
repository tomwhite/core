package org.tiling.util;

/**
 * I can tell you the version of packages.
 */
public class Version {

	private Version() {
	}

	public static String getPackageVersion(String packageName) {
		Package p = Package.getPackage(packageName);
		return p == null ? "Unknown" : p.getImplementationVersion();
	}

	private static void printAllPackages() {
		Package[] packages = Package.getPackages();
		for (int i = 0; i < packages.length; i++) {
			System.out.println(packages[i]);
		}
	}

	private static void printAllPackages(String prefix) {
		Package[] packages = Package.getPackages();
		for (int i = 0; i < packages.length; i++) {
			if (packages[i].getName().startsWith(prefix)) {
				System.out.println(packages[i]);
			}
		}
	}

	public static void main(String[] args) {
		String packageName;
		if (args.length == 0) {
			System.out.println(getPackageVersion("org.tiling.util"));
		} else if (args[0].equals("-all")) {
			printAllPackages("org.tiling");
		} else {		
			System.out.println(getPackageVersion(args[0]));
		}
	}

}