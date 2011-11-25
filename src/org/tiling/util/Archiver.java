package org.tiling.util;

import java.beans.*; 
import java.io.*; 

/**
 * A utility class for archiving objects in a long-term store.
 */
public class Archiver {

	private static Archiver instance = new Archiver();

	private static final String TEMP_FILE_PREFIX = "archive";

	private File archiveRoot;

	private Archiver() {
		archiveRoot = new File(System.getProperty("user.home"), "archive");
		archiveRoot.mkdir();
	}

	public static Archiver getInstance() {
		return instance;
	}

	public void store(String name, Object object) throws IOException {

		File archiveDirectory = new File(archiveRoot, name);
		if (!archiveDirectory.exists()) {
			if (!archiveDirectory.mkdir()) {
				throw new IOException("Could not create directory " + archiveDirectory);
			}
		}

		// Write object to tempfile
		File tempfile = null;
		try {
			tempfile = File.createTempFile(TEMP_FILE_PREFIX, null, archiveDirectory);
			archive(tempfile, object);
		} catch (IOException e) {
			if (tempfile != null) {
				tempfile.delete();
			}
			throw e;
		}

		// Find next version v
		int version = nextVersion(archiveDirectory);

		// Rename name to name.v
		if (version > 0) {
			File file = new File(archiveDirectory, "0");
			File versionedFile = new File(archiveDirectory, "" + version);
			boolean renamed = file.renameTo(versionedFile);
			if (!renamed) {
				System.err.println("Could not rename " + file + " to " + versionedFile);
			}
		}

		// Rename tempfile to name
		File file = new File(archiveDirectory, "0");
		boolean renamed = tempfile.renameTo(file);
		if (!renamed) {
			if (tempfile != null) {
				tempfile.delete();
			}	
			System.err.println("Could not rename " + tempfile + " to " + file);
		}

	}

	public Object retrieve(String name) throws IOException {
		File archiveDirectory = new File(archiveRoot, name);
		if (!archiveDirectory.exists()) {
			throw new IOException("No such archive " + name);
		}
		Object object = null;
		try {
			object = unarchive(new File(archiveDirectory, "0"));
		} catch (IOException e) {
			System.out.println("Could not retrieve version 0: " + e);
		}
		if (object == null) {
			int version = nextVersion(archiveDirectory) - 1;
			while (object == null && version > 0) {
				try {
					object = unarchive(new File(archiveDirectory, "" + version));
				} catch (IOException e) {
					System.out.println("Could not retrieve version " + version + ": " + e);
				}
				version--;
			}
		}
		return object;
	}

	public void delete(String name) throws IOException {
		File archiveDirectory = new File(archiveRoot, name);
		if (!archiveDirectory.exists()) {
			throw new IOException("No such archive " + name);
		}
		File[] versions = archiveDirectory.listFiles();
		for (int i = 0; i < versions.length; i++) {
			if (!versions[i].delete()) {
				throw new IOException("Could not delete file " + versions[i]);
			}
		}
		if (!archiveDirectory.delete()) {
			throw new IOException("Could not delete directory " + archiveDirectory);
		}
	}

	private void archive(File file, Object object) throws IOException {
		/*
		XMLEncoder encoder = null;
		try {
			encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
			encoder.setExceptionListener(new ExceptionListener() {
				public void exceptionThrown(Exception e) {
					e.printStackTrace();
				}		
			});
			encoder.writeObject(object);
		} finally {
			if (encoder != null) {
				encoder.close();
			}
		}
		*/
		Serializer.serialize(object, file);
	}

	private Object unarchive(File file) throws IOException {
		/*
		XMLDecoder decoder = null;
		try {
			decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));
			return decoder.readObject();
		} finally {
			if (decoder != null) {
				decoder.close();
			}
		}
		*/
		return Serializer.deserialize(file);
	}

	private int nextVersion(File archiveDirectory) {
		File[] files = archiveDirectory.listFiles(
			new FileFilter() {
				public boolean accept(File pathname) {
					String filename = pathname.getName();
					return !filename.startsWith(TEMP_FILE_PREFIX);
				}
			} 

		);
		return files.length;
	}


}