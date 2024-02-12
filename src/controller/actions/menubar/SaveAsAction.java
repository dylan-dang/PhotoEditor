package controller.actions.menubar;

import java.awt.event.*;

import model.Document;
import view.View;
import java.io.File;

public class SaveAsAction extends FileMenuBarAction {
    private final Document document;

    public SaveAsAction(View view, Document document) {
        super(view, "Save As...", "ctrl shift S");
        this.document = document;
    }

    public SaveAsAction(View view) {
        this(view, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Document doc = document == null ? view.getSelectedDocument() : document;
        File file = saveFile();
        if (file == null) return;
        doc.setLinkedFile(file);
        new SaveAction(view, doc).execute();
    }
}

