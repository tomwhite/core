package org.tiling.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.JComponent;

import org.tiling.UI;

import java.awt.event.MouseEvent;import java.awt.geom.Point2D;/**
 * I am a printable Swing drawing surface with an associated AffineTransform.
 * I also support tool tips which can change according to the mouse position on
 * the canvas.
 */
public class Canvas2D extends JComponent implements Printable {

	/**
	 * @serial the pre-transform to apply
	 */
	protected AffineTransform preTransform;

	/**
	 * @serial
	 */
	protected UI ui;

	public Canvas2D() {
		this.preTransform = new AffineTransform();
		setPreferredSize(DEFAULT_SIZE);
		setBackground(Color.white);
	}

	public Canvas2D(UI ui) {
		this(ui, new AffineTransform());
	}

	public AffineTransform getAffineTransform() {
		return preTransform;
	}

	public UI getUI() {
		return ui;
	}

	public void paint(Graphics g) {
		if (ui != null) {
			Graphics2D g2 = (Graphics2D) g;
			g2.transform(getAffineTransform());
			g2.transform(flip);
			ui.paint(g2);
			if (selection != null) {
				g2.setColor(Color.blue);
				g2.draw(selection);
			}
		} else {
			super.paint(g);
		}
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
		if (pageIndex >= 1) {
			return Printable.NO_SUCH_PAGE;
		}
		Graphics2D g2 = (Graphics2D) g;
		Rectangle2D bounds = g.getClip().getBounds2D();
		AffineTransform t = AffineTransform.getTranslateInstance(bounds.getX(), bounds.getY());
		t.concatenate(getAffineTransform());
		g2.transform(t);
		g2.transform(flip);
		ui.paint(g2);
		return Printable.PAGE_EXISTS;
	}

/*
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(preTransform);
		out.writeObject(ui);
	}
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		preTransform = (AffineTransform) in.readObject();
		ui = (UI) in.readObject();
	}
*/
	private static final Dimension DEFAULT_SIZE = new Dimension(500, 500);	private AffineTransform flip = createFlipTransform();	private Rectangle2D selection;	private boolean toolTipsEnabled = false;	public Canvas2D(UI ui, AffineTransform preTransform) {
		this.ui = ui;
		this.preTransform = preTransform;
		setPreferredSize(DEFAULT_SIZE);
		setBackground(ui.getBackground());
	}	/**
	 * I concatenate the given transform to the canvas.
	 */
	protected void concatenate(AffineTransform transform) {
		getAffineTransform().concatenate(transform);
		repaint();
	}	private static AffineTransform createFlipTransform() {
		return new AffineTransform(new double[]{1.0, 0.0, 0.0, -1.0});
	}	protected void fit(Rectangle2D box) {
		float xScale = (float) getAffineTransform().getScaleX();
		float yScale = (float) getAffineTransform().getScaleY();
		float deltaX = (float) getAffineTransform().getTranslateX();
		float deltaY = (float) getAffineTransform().getTranslateY();

		AffineTransform topLeftToOrigin = AffineTransform.getTranslateInstance(
			-deltaX / xScale,
			(getHeight() - deltaY) / yScale
		);

		double factor;
		double fX = getWidth() / box.getWidth();
		double fY = getHeight() / box.getHeight();
		if (Math.abs(fX / xScale) < Math.abs(fY / yScale)) {
			factor = fX;
		} else {
			factor = fY;
		}
		AffineTransform zoom = AffineTransform.getScaleInstance(factor, factor);

		AffineTransform originToTopLeftBox = AffineTransform.getTranslateInstance(
			-box.getMinX(),
			box.getMinY()
		);

		topLeftToOrigin.concatenate(zoom);
		topLeftToOrigin.concatenate(originToTopLeftBox);

		concatenate(topLeftToOrigin);
	}	public void fitToCanvas() {
		fit(getUI().getBounds2D());
	}	public AffineTransform getFlippedAffineTransform() {
		AffineTransform t = (AffineTransform) preTransform.clone();
		t.concatenate(createFlipTransform());
		return t;
	}	public String getToolTipText(MouseEvent event) {
		return getToolTipText(transformToLogicalSpace(event.getX(), event.getY()));
	}	protected String getToolTipText(Point2D point) {
		return "";
	}	public void setSelection(Rectangle2D selection) {
		this.selection = selection;
	}	public void setToolTipsEnabled(boolean toolTipsEnabled) {
		this.toolTipsEnabled = toolTipsEnabled;
		setToolTipText(toolTipsEnabled ? "" : null);
	}	public Point2D transformToLogicalSpace(int x, int y) { 
		try { 
			Point2D p1 = new Point2D.Float(x, y); 
			Point2D p2 = new Point2D.Float(); 
			getFlippedAffineTransform().createInverse().transform(p1, p2); 
			return new Point2D.Float((float) p2.getX(), (float) p2.getY()); 
		} catch (Exception ex) {
			return null;
		}
	}}