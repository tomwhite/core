package org.tiling.util;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;

/**
 * An ImageFactory loads Images from the file system.
 */
public class ImageFactory {

	Component component;

	public ImageFactory(Component component) {
		this.component = component;
	}

	public Image loadImage(String filename) {

		Image image = component.getToolkit().getImage(filename);
		try {
			MediaTracker tracker = new MediaTracker(component);
			tracker.addImage(image, 0);
			tracker.waitForID(0);
		} catch (Exception e) {
			System.err.println(e);
		}

		return image;

	}

	public Image loadImage(URL url) {

		Image image = component.getToolkit().getImage(url);
		try {
			MediaTracker tracker = new MediaTracker(component);
			tracker.addImage(image, 0);
			tracker.waitForID(0);
		} catch (Exception e) {}

		return image;

	}

	public BufferedImage loadBufferedImage(String filename) {

		Image image = loadImage(filename);
		int width = image.getWidth(component);
		int height = image.getHeight(component);
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D big = bi.createGraphics();
		big.drawImage(image, 0, 0, component);

		return bi;

	}

	public BufferedImage loadBufferedImage(URL url) {

		Image image = loadImage(url);
		int width = image.getWidth(component);
		int height = image.getHeight(component);
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D big = bi.createGraphics();
		big.drawImage(image, 0, 0, component);

		return bi;

	}

}