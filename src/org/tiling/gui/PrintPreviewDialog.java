package org.tiling.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.*;

import org.tiling.UI;

import java.awt.print.Paper;/**
 * A PrintPreviewDialog embeds a PrintPreview in a Swing JDialog.
 */
public class PrintPreviewDialog extends JDialog {

	private Canvas2D canvas;



	protected class Printer extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			new PrinterBean().print(canvas, paper);
			dispose();
		}
	}

	protected class DialogCloser extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	private Paper paper;	public PrintPreviewDialog(Frame owner, Canvas2D canvas, Paper paper) {
		super(owner, "Print Preview");

		this.canvas = canvas;
		this.paper = paper;

		PrintPreview preview = new PrintPreview(canvas, paper);
		getContentPane().setBackground(preview .getBackground());
		getContentPane().add(preview, BorderLayout.CENTER);

		JButton print = new JButton("Print");
		print.addActionListener(new Printer());
		JButton close = new JButton("Close");
		close.addActionListener(new DialogCloser());
		JPanel panel = new JPanel();
		panel.add(print);
		panel.add(close);
		getContentPane().add(panel, BorderLayout.SOUTH);

		setResizable(false);

		pack();
	}
}