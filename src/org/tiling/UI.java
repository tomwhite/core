package org.tiling;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/**
 * A UI encapsulates the graphical view of some entity.
 */
public interface UI extends Cloneable {
	public void paint(Graphics2D g2);
	
	public void setBackground(Color c);

	public Color getBackground();

	public Rectangle2D getBounds2D();

	public Object clone();}