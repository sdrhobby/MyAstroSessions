package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

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

        btnResetEditor.addActionListener(actionEvent -> editorPane.setText(originalText));
        btnSaveEditor.addActionListener(actionEvent -> {
            // TODO: confirmation dialog
            saveToFile();
        });
        btnSaveEditor.setMnemonic('S');

        editorPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK && (e.getKeyCode() == KeyEvent.VK_S)) {
                    saveToFile();
                }
            }
        });
    }

    private void fillEditor() {
        editorPane.setText("");
        try {
            editorPane.read(new FileInputStream(filePath), null);
        } catch (IOException e) {
            System.err.printf("Text-file not found: %s%n", filePath);
        }
        originalText = editorPane.getText();
    }

    private void saveToFile() {
        try {
            System.out.println("writing readme-file: " + filePath);
            FileWriter writer = new FileWriter(filePath);
            writer.write(editorPane.getText());
            originalText = editorPane.getText();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.err.printf("Error while writing text-file: %s%n", filePath);
        }
    }

    public void insertEditorText(String newText) {
        int answer = 0;
        if (!editorPane.getText().isEmpty()) {
            answer = JOptionPane.showConfirmDialog(
                    null,
                    Config.getInstance().getL10n().getString("editor_confirm.message"),
                    Config.getInstance().getL10n().getString("editor_confirm.title"),
                    JOptionPane.YES_NO_OPTION);
        }
        if (answer == 0) {
            editorPane.setText(String.format("%s\n\n%s", newText, editorPane.getText()));
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));
        final JScrollPane scrollPane1 = new JScrollPane();
        topPanel.add(scrollPane1, BorderLayout.CENTER);
        editorPane = new JEditorPane();
        scrollPane1.setViewportView(editorPane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        panel1.setPreferredSize(new Dimension(0, 36));
        topPanel.add(panel1, BorderLayout.SOUTH);
        btnResetEditor = new JButton();
        this.$$$loadButtonText$$$(btnResetEditor, this.$$$getMessageFromBundle$$$("l10n", "common_reset"));
        panel1.add(btnResetEditor);
        btnSaveEditor = new JButton();
        this.$$$loadButtonText$$$(btnSaveEditor, this.$$$getMessageFromBundle$$$("l10n", "common_save"));
        panel1.add(btnSaveEditor);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

}
