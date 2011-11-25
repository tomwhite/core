package org.tiling.gui;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import java.io.Serializable;

/**
 * I can print Printable objects.
 */
public class PrinterBean implements Serializable {
	public PrinterBean() {
	}
	public void print(Printable printable, Paper paper) {
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		PageFormat pageFormat = new PageFormat();
		pageFormat.setPaper(paper);
		printerJob.setPrintable(printable, pageFormat);
		if (printerJob.printDialog()) {
			try {
				printerJob.print();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
