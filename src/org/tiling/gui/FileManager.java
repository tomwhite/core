package org.tiling.gui;

import java.awt.Component;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.tiling.util.History;/**
 * A FileManager is a facade to the swing JFileChooser class.
 */
public class FileManager implements Serializable {



	private static FileManager instance = new FileManager();

	private FileManager() {
		ownerToHistoryMap = new HashMap();
	}

	public static FileManager getInstance() {
		return instance;
	}

	public File chooseFileToOpen(Component parent) {
		return chooseFileToOpen(parent, parent);
	}

	public File chooseFileToOpen(Component parent, Object owner) {
		return chooseFileToOpen(parent, owner, null);
	}

	public File chooseFileToOpen(Component parent, Object owner, FileFilter fileFilter) {
		return chooseFile(parent, owner, fileFilter, true);
	}

	public File chooseFileToSave(Component parent) {
		return chooseFileToSave(parent, parent);
	}

	public File chooseFileToSave(Component parent, Object owner) {
		return chooseFileToSave(parent, owner, null);
	}

	public File chooseFileToSave(Component parent, Object owner, FileFilter fileFilter) { 
		return chooseFile(parent, owner, fileFilter, false);
	}

	private Map ownerToHistoryMap;	private File chooseFile(Component parent, Object owner, FileFilter fileFilter, boolean open) {
		History history = (History) ownerToHistoryMap.get(owner);
		File file = null;
		if (history != null) {
			file = (File) history.predict();
		} else {
			history = new History();
		}
		JFileChooser fileChooser = new JFileChooser(file);
		if (fileFilter != null) {
			fileChooser.setFileFilter(fileFilter);
		}
		int returnValue = open ? fileChooser.showOpenDialog(parent) : fileChooser.showSaveDialog(parent);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			history.remember(file);
			ownerToHistoryMap.put(owner, history);
			return file;
		} else {
			return null;
		}
	}}