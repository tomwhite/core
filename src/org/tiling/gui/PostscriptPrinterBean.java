package org.tiling.gui;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.SimpleDoc;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

/**
 * I can print Printable objects to a Postscript file.
 */
public class PostscriptPrinterBean extends PrinterBean {

	public PostscriptPrinterBean() {
	}

	public void print(Printable printable, Paper paper) {

		String mimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();

/*
		// Use the pre-defined flavour for a Printable from an InputStream
		DocFlavor flavour = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

		StreamPrintServiceFactory[] factories =
		StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
					flavour, mimeType);

		if (factories.length == 0) {
			System.err.println("No suitable factories for " + mimeType);
		} else {
			try {
				FileOutputStream fos = new FileOutputStream("out.ps");
				StreamPrintService streamPrintService = factories[0].getPrintService(fos);
				DocPrintJob printJob = streamPrintService.createPrintJob();
				PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
				attributes.add(MediaSizeName.ISO_A3);

				Doc doc = new SimpleDoc(printable, flavour, null);
				printJob.print(doc, attributes);
				fos.close();

			} catch (PrintException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
*/
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		PageFormat pageFormat = printerJob.defaultPage();
		pageFormat.setPaper(paper);
		printerJob.setPrintable(printable, pageFormat);

		StreamPrintServiceFactory[] factories =
			PrinterJob.lookupStreamPrintServices(mimeType);
		if (factories.length == 0) {
			System.err.println("No suitable factories for " + mimeType);
		} else {
			try {
				FileOutputStream fos = new FileOutputStream("out.ps");
				StreamPrintService streamPrintService = factories[0].getPrintService(fos);
				printerJob.setPrintService(streamPrintService);
				PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
				attributes.add(MediaSizeName.ISO_A4);
				printerJob.print(attributes);
				fos.close();
			} catch (PrinterException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
}
