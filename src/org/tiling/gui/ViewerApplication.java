package org.tiling.gui;

import java.awt.BorderLayout;

import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JFrame;

/**
 * I wrap a {@link Viewer2D} in a frame to make an application.
 * To write a specific application subclass me and implement
 * {@link#buildViewer()} to create a viewer. Then write a
 * main method to create an instance of the subclass.
 */
public class ViewerApplication extends JFrame {
	protected JDesktopPane desktop;
	protected Viewer2D viewer;
	public ViewerApplication(String title) {
		super(title);
		initialiseDesktop();
		viewer = buildViewer();
		addMaximisedJInternalFrame(viewer);
	}
	public ViewerApplication(String title, Viewer2D viewer) {
		super(title);
		initialiseDesktop();
		this.viewer = viewer;
		addMaximisedJInternalFrame(viewer);
	}
	public void addJInternalFrame(JInternalFrame internalFrame) {
		internalFrame.pack();
		desktop.add(internalFrame, JLayeredPane.DEFAULT_LAYER);
		desktop.moveToFront(internalFrame);
		try {
			internalFrame.setSelected(true);
		} catch (PropertyVetoException e) {
			System.err.println(e);
		}
	}
	private void addMaximisedJInternalFrame(JInternalFrame internalFrame) {
		desktop.add(internalFrame, JLayeredPane.DEFAULT_LAYER);
		internalFrame.setLocation(0, 0);
		try {
			internalFrame.setMaximum(true);
		} catch (PropertyVetoException e) {
			// couldn't maximise pane
		}

		internalFrame.setVisible(true);
	}
	public Viewer2D buildViewer() {
		return null;
	}
	private void initialiseDesktop() {
		desktop = new JDesktopPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(desktop, BorderLayout.CENTER);

		setSize(600, 600);
		setDefaultCloseOperation(3); // JFrame.EXIT_ON_CLOSE in JDK 1.3 onwards
		setVisible(true);	
	}
}
