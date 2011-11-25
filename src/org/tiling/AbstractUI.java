package org.tiling;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.tiling.UI;

/**
 * I am a helpful implementation of {@link UI}.
 * Just override <code>paint</code> to call my <code>paint</code>
 * method then draw your shapes.
 */
public abstract class AbstractUI implements UI {
	public static final float LINE_WIDTH = 0.1f;

	protected Color backgroundColor = Color.white;
	protected RenderingHints qualityHints;
	protected Stroke stroke;

	protected Rectangle2D bounds;
	protected AbstractUI() {
		initialiseGraphics();
	}
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}		
	}
	public Color getBackground() {
		return backgroundColor;
	}
	public Rectangle2D getBounds2D() {
		return bounds;
	}
	protected void initialiseGraphics() {
		qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
											RenderingHints.VALUE_ANTIALIAS_ON);
		qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		stroke = new BasicStroke(LINE_WIDTH);
	}
	public void paint(Graphics2D g2) {
		g2.setRenderingHints(qualityHints);
		g2.setStroke(stroke);
	}
	public void setBackground(Color c) {
		backgroundColor = c;
	}
	protected void updateBounds(Shape shape) {
		if (bounds == null) {
			bounds = shape.getBounds2D();
		} else {
			bounds.add(shape.getBounds2D());
		}
	}
}
