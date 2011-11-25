package org.tiling.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterJob;
import java.io.Serializable;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.tiling.util.Serializer;

import java.beans.Beans;
import java.awt.geom.Point2D;/**
 * I am a component that adds zoom, scroll, and resizing functions to a Canvas2D object.
 * There are also a number of actions defined for use in subclasses, such as
 * saving, loading and printing.
 * @see Canvas2D
 */
public class Viewer2D extends JInternalFrame {

	/**
	 * @serial
	 */
	private Canvas2D canvas;

	/**
	 * @serial
	 */
	private JPanel viewPanel;

	/**
	 * @serial
	 */
	private JSlider slider;

	/**
	 * @serial
	 */
	private double scale;

	/**
	 * @serial
	 */
	private Rectangle bounds, newBounds;  // The enclosing Rectangle for Viewer (only need this for deducing relative movement!)

	public Viewer2D() {
		this("");
	}

	public Viewer2D(String title) {
		this(title, true);
	}

	public Viewer2D(String title, boolean closeable) {
		this(title, true, closeable, true);
	}
	
	// Fix for bug #4220108 "When JDesktop already added to parent, can't see JSlider in JInternalFrame"
	private class InternalFrameListenerForSliderFix extends InternalFrameAdapter implements Serializable {
		public void internalFrameActivated(InternalFrameEvent e) {
			sliderFixHack(slider);
		}
	}

	private void sliderFixHack(Component comp) {
		Dimension dim = comp.getSize();
		dim.height +=1;
		comp.setSize(dim);
		comp.repaint();
		dim.height -=1;
		comp.setSize(dim);
		comp.repaint();
	}
	public void setCanvas2D(Canvas2D canvas) {
		if (this.canvas != null) {
			viewPanel.remove(this.canvas);
		}
		this.canvas = canvas;

		// Centre and scale canvas
		scale = canvas.getAffineTransform().getScaleX();
		double factor = getMagnification();
		canvas.getAffineTransform().scale(factor, factor);

		viewPanel.setBackground(canvas.getBackground());
		viewPanel.add(canvas, BorderLayout.CENTER);

		Dragger dragger = new Dragger();
		canvas.addMouseListener(dragger);
		canvas.addMouseMotionListener(dragger);
		canvas.repaint();

		centreCanvas();

		revalidate();
	}

	public Canvas2D getCanvas2D() {
		return canvas;
	}



	public void fitToCanvas() {
		if (canvas != null) {
			canvas.fitToCanvas();
		}
	}

	public void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		if (canvas != null) {
			canvas.addMouseListener(l);
		}
	}



	protected class Printer extends AbstractAction {
		public Printer() {
			super("Print...");
		}
		public void actionPerformed(ActionEvent e) {
			try {
				new PrinterBean().print(canvas, PrintPreview.A4);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}





	protected class Fitter extends AbstractAction {
		public Fitter() {
			super("Fit View");
		}
		public void actionPerformed(ActionEvent e) {
			fitToCanvas();
		}
	}

	protected class Zoomer implements ChangeListener, Serializable {

		public void stateChanged(ChangeEvent e) {
			if (continuous || !slider.getValueIsAdjusting()) {
				zoom();
			}
		}

	}

	public void processComponentEvent(ComponentEvent e) {
		if (e.getID() == ComponentEvent.COMPONENT_MOVED) {
			// Don't fire the event if the size has changed - leave this exclusively for COMPONENT_RESIZED
			if (!newBounds.getSize().equals(getSize())) {
				return;
			}
		}
		super.processComponentEvent(e);
	}

	protected class Resizer extends ComponentAdapter implements Serializable {

		public void componentMoved(ComponentEvent e) {
			bounds = newBounds;
			newBounds = getBounds();
		}

		public void componentResized(ComponentEvent e) {
			bounds = newBounds;
			newBounds = getBounds();

			// Adjust Metric according to bounds and newBounds
			int xIncr = bounds.x - newBounds.x;
			int yIncr = bounds.y - newBounds.y;

			preConcatenate(AffineTransform.getTranslateInstance(xIncr, yIncr));

		}

	}

	protected class Dragger extends MouseAdapter implements MouseMotionListener, Serializable  {
		int startX, startY;
		boolean leftButton; // translate
		boolean rightButton; // zoom

		public void mousePressed(MouseEvent e) {
			startX = e.getX();
			startY = e.getY();
			leftButton = ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0);
			rightButton = ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0);
		}

		public void mouseReleased(MouseEvent e) {
			if (leftButton && !continuous) {
				int xIncr = e.getX() - startX;
				int yIncr = e.getY() - startY;
				if (xIncr != 0 || yIncr != 0) {
					preConcatenate(AffineTransform.getTranslateInstance(xIncr, yIncr));
				}
			} else if (rightButton) {
				if (canvas != null) {
					Point2D start = canvas.transformToLogicalSpace(startX, startY);
					Point2D end = canvas.transformToLogicalSpace(e.getX(), e.getY());
					fit(new Rectangle2D.Double(start.getX(), end.getY(),
						end.getX() - start.getX(), start.getY() - end.getY()));
					canvas.setSelection(null);
					repaint();
				}
			}
		}
		
		public void mouseDragged(MouseEvent e) {
			if (leftButton && continuous) {
				int xIncr = e.getX() - startX;
				int yIncr = e.getY() - startY;

				startX = e.getX();
				startY = e.getY();

				preConcatenate(AffineTransform.getTranslateInstance(xIncr, yIncr));
			} else if (rightButton) {
				if (canvas != null) {
					Point2D start = canvas.transformToLogicalSpace(startX, startY);
					Point2D end = canvas.transformToLogicalSpace(e.getX(), e.getY());
					Rectangle2D selection = new Rectangle2D.Double(start.getX(), end.getY(),
						end.getX() - start.getX(), start.getY() - end.getY());
					canvas.setSelection(selection);
					repaint();
				}
			}
		}

		public void mouseMoved(MouseEvent e) {
		}

	}

	private static final int DEFAULT_SLIDER_VALUE = 0;		private static final double LOGARITHMIC_BASE = 10.0;		private static final int MAXIMUM_SLIDER_VALUE = (int) (+2 * 50.0);		private static final int MINIMUM_SLIDER_VALUE = (int) (-2 * 50.0);		private static final double SCALING_RESOLUTION = 50.0; // ticks per 10-fold increase
	
	/**
	 * @serial
	 */
	private Zoomer zoomer;		protected class PostscriptPrinter extends AbstractAction {
		public PostscriptPrinter() {
			super("Print to Postscript file...");
		}
		public void actionPerformed(ActionEvent e) {
			try {
				PrinterBean printerBean = (PrinterBean)
					Beans.instantiate(this.getClass().getClassLoader(), "org.tiling.gui.PostscriptPrinterBean");
				printerBean.print(canvas, PrintPreview.A3);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}		protected class PrintPreviewerA3 extends AbstractAction {
		public PrintPreviewerA3() {
			super("Print Preview (A3)...");
		}
		public void actionPerformed(ActionEvent e) {
			PrintPreviewDialog dialog =
				new PrintPreviewDialog((Frame) SwingUtilities.windowForComponent(Viewer2D.this), canvas, PrintPreview.A3);
			dialog.show();
		}
	}		protected class PrintPreviewerA4 extends AbstractAction {
		public PrintPreviewerA4() {
			super("Print Preview (A4)...");
		}
		public void actionPerformed(ActionEvent e) {
			PrintPreviewDialog dialog =
				new PrintPreviewDialog((Frame) SwingUtilities.windowForComponent(Viewer2D.this), canvas, PrintPreview.A4);
			dialog.show();
		}
	}	public Viewer2D(String title, boolean resizable, boolean closeable, boolean iconifiable) {
		super(title, resizable, closeable, true, iconifiable);
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK);

		setRootPaneCheckingEnabled(false); // needed for Serialization

		JPanel zoomPanel = new JPanel();
		slider = new JSlider(JSlider.VERTICAL, MINIMUM_SLIDER_VALUE, MAXIMUM_SLIDER_VALUE, DEFAULT_SLIDER_VALUE);
		zoomer = new Zoomer();
		slider.addChangeListener(zoomer);
		slider.setToolTipText("Zoom");
		zoomPanel.add(slider, BorderLayout.CENTER);

		viewPanel = new JPanel();
		viewPanel.setLayout(new BorderLayout());
		viewPanel.setBackground(Color.white);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(zoomPanel, BorderLayout.WEST);
		getContentPane().add(viewPanel, BorderLayout.CENTER);

		// Listen for motion of this (and hence of the Canvas)
		bounds = newBounds = getBounds();
		addComponentListener(new Resizer());

		// Fix for bug #4220108 "When JDesktop already added to parent, can't see JSlider in JInternalFrame"
		addInternalFrameListener(new InternalFrameListenerForSliderFix());
		// End fix

		setUpMenus();
		
	}		public void addNotify() {
		super.addNotify();
		pack();
	}		protected void fit(Rectangle2D box) {
		if (canvas != null) {
			canvas.fit(box);
		}
	}		public AffineTransform getAffineTransform() {
		return canvas.getAffineTransform();
	}				protected void setUpMenus() {

		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new Printer());
		fileMenu.add(new PostscriptPrinter());
		fileMenu.add(new PrintPreviewerA3());
		fileMenu.add(new PrintPreviewerA4());
		
		JMenu viewMenu = new JMenu("View");
		viewMenu.add(new Centrer());
		viewMenu.add(new Fitter());
		viewMenu.addSeparator();
		JMenuItem continuousCheckBox = new JCheckBoxMenuItem("Continuous");
		continuousCheckBox.addItemListener(new ContinuousCheckBoxListener());
		viewMenu.add(continuousCheckBox);

		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		setJMenuBar(menuBar);

	}	/**
	 * @serial
	 */
	private boolean continuous = false;	protected class Centrer extends AbstractAction {
		public Centrer() {
			super("Centre On Origin");
		}
		public void actionPerformed(ActionEvent e) {
			centreCanvas();
		}
	}	protected class ContinuousCheckBoxListener implements ItemListener, Serializable {
		public void itemStateChanged(ItemEvent e) {
			setContinuous(e.getStateChange() == ItemEvent.SELECTED);
		}
	}	public void centreCanvas() {
		if (canvas == null) {
			return;
		}
		float xScale = (float) canvas.getAffineTransform().getScaleX();
		float yScale = (float) canvas.getAffineTransform().getScaleY();
		float deltaX = (float) canvas.getAffineTransform().getTranslateX();
		float deltaY = (float) canvas.getAffineTransform().getTranslateY();

		AffineTransform centreToOrigin = AffineTransform.getTranslateInstance(
			(0.5 * canvas.getWidth() - deltaX) / xScale,
			(0.5 * canvas.getHeight() - deltaY) / yScale
		);
		concatenate(centreToOrigin);
	}	/**
	 * I concatenate the given transform to the canvas.
	 */
	protected void concatenate(AffineTransform transform) {
		if (canvas != null) {
			canvas.concatenate(transform);
		}
	}	private double getMagnification() {
		return Math.pow(LOGARITHMIC_BASE, slider.getValue() / SCALING_RESOLUTION);
	}	public boolean isContinuous() {
		return continuous;
	}	/**
	 * I pre-concatenate the given transform to the canvas.
	 */
	protected void preConcatenate(AffineTransform transform) {
		if (canvas == null) {
			return;
		}
		canvas.getAffineTransform().preConcatenate(transform);
		canvas.repaint();
	}	public void setContinuous(boolean continuous) {
		this.continuous = continuous;
	}	private void setMagnification(double magnification) {
		// unregister listener!
		slider.removeChangeListener(zoomer);
		slider.setValue((int) (SCALING_RESOLUTION * Math.log(magnification) / Math.log(LOGARITHMIC_BASE)));
		// re-register listener!
		slider.addChangeListener(zoomer);
	}	private void zoom() {
		if (canvas == null) {
			return;
		}
		float xScale = (float) canvas.getAffineTransform().getScaleX(); 
		float yScale = (float) canvas.getAffineTransform().getScaleY(); 
		float deltaX = (float) canvas.getAffineTransform().getTranslateX(); 
		float deltaY = (float) canvas.getAffineTransform().getTranslateY(); 

		double factor = getMagnification() * scale / canvas.getAffineTransform().getScaleX();

		try {
			AffineTransform centreToOrigin = AffineTransform.getTranslateInstance(
				(0.5 * canvas.getWidth() - deltaX) / xScale,
				(0.5 * canvas.getHeight() - deltaY) / yScale
			);
			AffineTransform zoom = AffineTransform.getScaleInstance(factor, factor);
			AffineTransform originToCentre = centreToOrigin.createInverse();

			centreToOrigin.concatenate(zoom);
			centreToOrigin.concatenate(originToCentre);

			concatenate(centreToOrigin);
		} catch (java.awt.geom.NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}

	}}