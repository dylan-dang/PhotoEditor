package controller.actions.menubar;

import java.awt.event.*;
import javax.swing.*;
import javax.imageio.*;

import view.View;
import model.Document;
import java.io.File;
import java.io.IOException;

public class SaveAction extends MenuBarAction {
    private Document document;

    public SaveAction(View view, Document document) {
        super(view, "Save", "ctrl S");
        this.document = document;
    }

    public SaveAction(View view) {
        this(view, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Document doc = document == null ? view.getSelectedDocument() : document;
        if (!doc.isLinked()) {
            new SaveAsAction(view, doc).execute();
            return;
        }
        File file = doc.getLinkedFile();
        try {
            ImageIO.write(doc.flattened(), "png", file);
        } catch (IOException ioex) {
            ioex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Something went wrong when trying to save your file.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        doc.setSaved(true);
        view.updateTabNames();
    }
}

