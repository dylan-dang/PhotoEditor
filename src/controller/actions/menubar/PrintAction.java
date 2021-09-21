package controller.actions.menubar;

import java.awt.event.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import view.View;
import model.Document;

public class PrintAction extends MenuBarAction {
    private Document doc;

    public PrintAction(View view) {
        this(view, null);
    }

    public PrintAction(View view, Document doc) {
        super(view, "Print...", "ctrl P");
        this.doc = doc;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (doc == null)
            doc = view.getSelectedDocument();
        if (doc == null)
            return;
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new PrintableImage(printJob, doc.flattened()));
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch (PrinterException prt) {
                prt.printStackTrace();
            }
        }
    }
}
