package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.config.Config;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class EditorPanel {
    private JPanel topPanel;
    private JEditorPane editorPane;
    private JButton btnResetEditor;
    private JButton btnSaveEditor;

    private final String filePath;
    private String originalText;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public EditorPanel(String filePath) {
        this.filePath = filePath;
        fillEditor();

        btnResetEditor.addActionListener( actionEvent -> editorPane.setText(originalText) );
        btnSaveEditor.addActionListener( actionEvent -> {
            // TODO: confirmation dialog
            saveToFile();
        } );

    }

    private void fillEditor() {
        editorPane.setText("");
        try {
            editorPane.read( new FileInputStream( filePath ), null);
        } catch (IOException e) {
            System.err.printf( "Text-file not found: %s%n", filePath );
        }
        originalText = editorPane.getText();
    }

    private void saveToFile() {
        try {
            FileWriter writer = new FileWriter(filePath );
            writer.write(editorPane.getText());
            originalText = editorPane.getText();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.printf( "Error while writing text-file: %s%n", filePath );
        }
    }

    public void insertEditorText( String newText ) {
        int answer = 0;
        if ( ! editorPane.getText().isEmpty() ) {
            answer = JOptionPane.showConfirmDialog(
                null,
                    Config.getInstance().getL10n().getString("editor_confirm.message"),
                    Config.getInstance().getL10n().getString("editor_confirm.title"),
                JOptionPane.YES_NO_OPTION);
        }
        if ( answer == 0 ) {
            editorPane.setText( String.format( "%s\n\n%s", newText, editorPane.getText() ) );
        }
    }

}
