package de.sdr.astro.cat.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.config.Profile;
import de.sdr.astro.cat.forms.common.ImageDisplayPanel;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.model.PointDouble;
import de.sdr.astro.cat.model.Session;
import de.sdr.astro.cat.gui.overlays.Overlay;
import de.sdr.astro.cat.gui.overlays.TextOverlay;
import de.sdr.astro.cat.util.Util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ImageExportPanel {
    private JPanel topPanel;
    private JPanel panelInteractions;
    private JPanel panelMain;
    private JButton btnSelect;
    private JPanel panelNavigation;
    private JButton btnPrev;
    private JButton btnNext;
    private JLabel lblImageName;
    private JLabel lblSessionName;
    private JTextField tfObject;
    private JTextField tfObserver;
    private JTextField tfCamera;
    private JTextField tfTelescope;
    private JCheckBox checkObject;
    private JTextField tfExposure;
    private JTextField tfFilter;
    private JCheckBox checkObserver;
    private JCheckBox checkTelescope;
    private JCheckBox checkCamera;
    private JCheckBox checkExposure;
    private JCheckBox checkFilter;
    private JPanel panelImage;
    private JPanel panelElements;
    private JSplitPane splitPaneMain;
    private JLayeredPane layeredPane = new JLayeredPane();

    private ImageDisplayPanel imageDisplayPanel = new ImageDisplayPanel();

    private Session session = null;
    private List<Image> imageList = null;
    private int imageIndex = 0;
    private String lastFolder = Config.getInstance().getScanFolder();

    private PointDouble overlayPosition = new PointDouble(75, 75);
    private int overlayFontSize;
    private Color overlayColor = Color.LIGHT_GRAY;
    private TextOverlay overlayObject = new TextOverlay("", overlayPosition, overlayColor);
    ;
    private TextOverlay overlayObserver = new TextOverlay("", overlayPosition.add(0, 4), overlayColor);
    private TextOverlay overlayTelescope = new TextOverlay("", overlayPosition.add(0, 8), overlayColor);
    private TextOverlay overlayCamera = new TextOverlay("", overlayPosition.add(0, 12), overlayColor);
    private TextOverlay overlayExposure = new TextOverlay("", overlayPosition.add(0, 16), overlayColor);
    private TextOverlay overlayFilter = new TextOverlay("", overlayPosition.add(0, 20), overlayColor);
    ;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public ImageExportPanel() {
        panelImage.add(imageDisplayPanel, BorderLayout.CENTER);
        btnSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(lastFolder));
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Astro - sessions (yyyy-mm-dd)";
                    }
                });
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    lastFolder = chooser.getSelectedFile().getPath();
                    handleSessionSelection(lastFolder);
                }
            }
        });
        btnPrev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setImage(imageIndex - 1);
            }
        });
        btnNext.addActionListener(actionEvent -> setImage(imageIndex + 1));
        checkObject.addItemListener(itemEvent -> {
            tfObject.setEnabled(!checkObject.isSelected());
        });
        checkCamera.addItemListener(itemEvent -> {
            tfCamera.setEnabled(!checkCamera.isSelected());
        });
        checkTelescope.addItemListener(itemEvent -> {
            tfTelescope.setEnabled(!checkTelescope.isSelected());
        });
        checkExposure.addItemListener(itemEvent -> {
            tfExposure.setEnabled(!checkExposure.isSelected());
        });
        checkFilter.addItemListener(itemEvent -> {
            tfFilter.setEnabled(!checkFilter.isSelected());
        });
        checkObserver.addItemListener(itemEvent -> {
            tfObserver.setEnabled(!checkObserver.isSelected());
        });
        addKeyListener(tfObserver, overlayObserver);
        addKeyListener(tfObject, overlayObject);
        addKeyListener(tfCamera, overlayCamera);
        addKeyListener(tfTelescope, overlayTelescope);
        addKeyListener(tfExposure, overlayExposure);
        addKeyListener(tfFilter, overlayFilter);
    }

    private void addKeyListener(JTextField tf, TextOverlay overlay) {
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                System.out.println("key released: " + tf.getText());
                overlay.setText(tf.getText());
                imageDisplayPanel.overlaysChanged();
            }
        });
    }

    private void handleSessionSelection(String path) {
        try {
            setSession(new Session(path));
        } catch (Exception x) {
            System.err.println("invalid session selected: " + path);
        }
    }

    public void setSession(Session session) {
        if (session != null) {
            lblSessionName.setText(session.getAstroObjectName() + " - Session: " + session.getName());
            this.session = session;
            this.imageList = session.getImageMap().get(Model.RESULTS);
            setImage(0);
            fillSessionEquipment();
        }
    }


    private void fillSessionEquipment() {
        Profile profile = Config.getInstance().readSessionProfile(session);
        if (profile != null) {
            tfObject.setText(session.getAstroObjectName());
            tfObserver.setText("");
            tfTelescope.setText(profile.getTelescopeById(profile.getMainTelescopeId()).getName());
            tfCamera.setText(profile.getCameraById(profile.getMainCameraId()).getName());
            tfExposure.setText(Util.formatExposure(session.totalCaptureTime(Model.LIGHTS)) + " min");
            tfFilter.setText("<filter>");
        }
        // initial drawing of the overlays
        initOverlays();
    }

    private void initOverlays() {

        overlayObject.setText(tfObject.getText());
        overlayObserver.setText(tfObserver.getText());
        overlayTelescope.setText(tfTelescope.getText());
        overlayCamera.setText(tfCamera.getText());
        overlayExposure.setText(tfExposure.getText());
        overlayFilter.setText(tfFilter.getText());

        List<Overlay> overlayList = new ArrayList<>();

        overlayList.add(overlayObserver);
        overlayList.add(overlayObject);
        overlayList.add(overlayCamera);
        overlayList.add(overlayTelescope);
        overlayList.add(overlayExposure);
        overlayList.add(overlayFilter);

        imageDisplayPanel.replaceOverlays(overlayList);
    }

    private void setImage(int index) {
        System.out.println("index " + index);
        if (imageList.isEmpty())
            return;
        if (index < 0)
            index = imageList.size() - 1;
        if (index >= imageList.size())
            index = 0;

        imageIndex = index;
        Image image = imageList.get(imageIndex);

        imageDisplayPanel.setImage(image.getPath());

        String num = String.format(" (%d / %d)", imageIndex + 1, imageList.size());
        lblImageName.setText(image.getName() + num);
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
        topPanel.setPreferredSize(new Dimension(1024, 768));
        splitPaneMain = new JSplitPane();
        splitPaneMain.setDividerLocation(600);
        splitPaneMain.setDividerSize(6);
        splitPaneMain.setResizeWeight(1.0);
        topPanel.add(splitPaneMain, BorderLayout.CENTER);
        panelMain = new JPanel();
        panelMain.setLayout(new BorderLayout(0, 0));
        panelMain.setPreferredSize(new Dimension(100, 100));
        splitPaneMain.setLeftComponent(panelMain);
        panelMain.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelNavigation = new JPanel();
        panelNavigation.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        panelMain.add(panelNavigation, BorderLayout.NORTH);
        lblImageName = new JLabel();
        Font lblImageNameFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblImageName.getFont());
        if (lblImageNameFont != null) lblImageName.setFont(lblImageNameFont);
        lblImageName.setText("");
        panelNavigation.add(lblImageName, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(200, -1), null, 0, false));
        lblSessionName = new JLabel();
        Font lblSessionNameFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblSessionName.getFont());
        if (lblSessionNameFont != null) lblSessionName.setFont(lblSessionNameFont);
        lblSessionName.setText("");
        panelNavigation.add(lblSessionName, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnNext = new JButton();
        btnNext.setText("=>");
        panelNavigation.add(btnNext, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnPrev = new JButton();
        btnPrev.setText("<=");
        panelNavigation.add(btnPrev, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelImage = new JPanel();
        panelImage.setLayout(new BorderLayout(0, 0));
        panelMain.add(panelImage, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setPreferredSize(new Dimension(24, 16));
        panelImage.add(panel1, BorderLayout.SOUTH);
        panelInteractions = new JPanel();
        panelInteractions.setLayout(new GridBagLayout());
        panelInteractions.setPreferredSize(new Dimension(250, 24));
        splitPaneMain.setRightComponent(panelInteractions);
        panelElements = new JPanel();
        panelElements.setLayout(new GridLayoutManager(9, 3, new Insets(0, 0, 0, 0), -1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelInteractions.add(panelElements, gbc);
        checkObject = new JCheckBox();
        checkObject.setText("");
        panelElements.add(checkObject, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfObject = new JTextField();
        panelElements.add(tfObject, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        checkObserver = new JCheckBox();
        checkObserver.setText("");
        panelElements.add(checkObserver, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfObserver = new JTextField();
        panelElements.add(tfObserver, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkTelescope = new JCheckBox();
        checkTelescope.setText("");
        panelElements.add(checkTelescope, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfTelescope = new JTextField();
        panelElements.add(tfTelescope, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkCamera = new JCheckBox();
        checkCamera.setText("");
        panelElements.add(checkCamera, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfCamera = new JTextField();
        panelElements.add(tfCamera, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkExposure = new JCheckBox();
        checkExposure.setText("");
        panelElements.add(checkExposure, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExposure = new JTextField();
        panelElements.add(tfExposure, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkFilter = new JCheckBox();
        checkFilter.setText("");
        panelElements.add(checkFilter, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfFilter = new JTextField();
        panelElements.add(tfFilter, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSelect = new JButton();
        this.$$$loadButtonText$$$(btnSelect, this.$$$getMessageFromBundle$$$("l10n", "btn_select.session"));
        panelElements.add(btnSelect, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panelElements.add(spacer1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelElements.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panelElements.add(spacer3, new GridConstraints(0, 2, 9, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelInteractions.add(panel2, gbc);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
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