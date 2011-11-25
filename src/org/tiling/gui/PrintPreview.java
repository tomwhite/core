package org.tiling.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import org.tiling.UI;

import java.awt.print.Paper;
/**
 * A PrintPreview allows the user to see the bounds of a graphical view to be printed.
 */
public class PrintPreview extends Canvas2D {

	private PrinterJob printerJob;
	private PageFormat pageFormat;
	private Rectangle2D clip;



	public void paint(Graphics g) {
		g.setClip(clip);
		super.paint(g);
	}

	public static final Paper A3;		public static final Paper A4;	
	public static final Paper A5;	public static final Paper A6;
	static {
		final int inch = 72;
		final double mm_per_inch = 25.4;

		A3 = new Paper();
		A3.setSize(inch * 297 / mm_per_inch, inch * 420 / mm_per_inch);
		A3.setImageableArea(inch, inch, A3.getWidth() - 2 * inch, A3.getHeight() - 2 * inch);

		A4 = new Paper();
		A4.setSize(inch * 210 / mm_per_inch, inch * 297 / mm_per_inch);
		A4.setImageableArea(inch, inch, A4.getWidth() - 2 * inch, A4.getHeight() - 2 * inch);

		A5 = new Paper();
		A5.setSize(inch * 148 / mm_per_inch, inch * 210 / mm_per_inch);

		A6 = new Paper();
		A6.setSize(inch * 105 / mm_per_inch, inch * 148 / mm_per_inch);
	}
	
	public PrintPreview(Canvas2D canvas, Paper paper) {
		super(canvas.getUI());
		preTransform = (AffineTransform) canvas.getAffineTransform().clone();

		double factor = 0.5;
		preTransform.preConcatenate(AffineTransform.getScaleInstance(factor, factor));

		double x = paper.getImageableX() * factor;
		double y = paper.getImageableY() * factor;
		double imageableWidth = paper.getImageableWidth() * factor;
		double imageableHeight = paper.getImageableHeight() * factor;
		double width = paper.getWidth() * factor;
		double height = paper.getHeight() * factor;

		preTransform.preConcatenate(AffineTransform.getTranslateInstance(x, y));
		clip = new Rectangle2D.Double(x, y, imageableWidth, imageableHeight);

		setPreferredSize(new Dimension((int) width, (int) height));

	}
}