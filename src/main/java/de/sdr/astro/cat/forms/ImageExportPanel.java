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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.Timer;

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
    private JButton btnColor;
    private JButton btnSave;
    private JSpinner spinnerFontSize;
    private JButton btnRight;
    private JButton btnLeft;
    private JButton btnUp;
    private JButton btnDown;
    private JPanel panelPosition;
    private JButton btnTopLeft;
    private JButton btnTopRight;
    private JButton btnBottomLeft;
    private JButton btnBottomRight;
    private JLayeredPane layeredPane = new JLayeredPane();

    private ImageDisplayPanel imageDisplayPanel = new ImageDisplayPanel();

    private Session session = null;
    private List<Image> imageList = null;
    private int imageIndex = 0;
    private String lastFolder = Config.getInstance().getScanFolder();

    private PointDouble inititalOverlayPosition = new PointDouble(80, 80);
    private PointDouble currentOverlayPosition = inititalOverlayPosition;
    private double initialOverlayFontSize = Config.getInstance().getOverlayConfig().getRelFontSize();
    private double currentOverlayFontSize = initialOverlayFontSize;
    private Color overlayColor = Config.getInstance().getOverlayConfig().getExportTextColor();
    private TextOverlay overlayObject = new TextOverlay("", inititalOverlayPosition, overlayColor);
    private TextOverlay overlayObserver = new TextOverlay("", inititalOverlayPosition.add(0, (initialOverlayFontSize + 1)), overlayColor);
    private TextOverlay overlayTelescope = new TextOverlay("", inititalOverlayPosition.add(0, 2 * (initialOverlayFontSize + 1)), overlayColor);
    private TextOverlay overlayCamera = new TextOverlay("", inititalOverlayPosition.add(0, 3 * (initialOverlayFontSize + 1)), overlayColor);
    private TextOverlay overlayExposure = new TextOverlay("", inititalOverlayPosition.add(0, 4 * (initialOverlayFontSize + 1)), overlayColor);
    private TextOverlay overlayFilter = new TextOverlay("", inititalOverlayPosition.add(0, 5 * (initialOverlayFontSize + 1)), overlayColor);

    private Timer moveTimer;
    private Timer scheduledMoveTimer;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public ImageExportPanel() {
        panelImage.add(imageDisplayPanel, BorderLayout.CENTER);
        btnSelect.addActionListener(actionEvent -> {
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
        });
        btnPrev.addActionListener(actionEvent -> setImage(imageIndex - 1));
        btnNext.addActionListener(actionEvent -> setImage(imageIndex + 1));

        addCheckBoxListener(checkObject, tfObject, overlayObject);
        addCheckBoxListener(checkObserver, tfObserver, overlayObserver);
        addCheckBoxListener(checkTelescope, tfTelescope, overlayTelescope);
        addCheckBoxListener(checkCamera, tfCamera, overlayCamera);
        addCheckBoxListener(checkExposure, tfExposure, overlayExposure);
        addCheckBoxListener(checkFilter, tfFilter, overlayFilter);

        addKeyListener(tfObserver, overlayObserver);
        addKeyListener(tfObject, overlayObject);
        addKeyListener(tfCamera, overlayCamera);
        addKeyListener(tfTelescope, overlayTelescope);
        addKeyListener(tfExposure, overlayExposure);
        addKeyListener(tfFilter, overlayFilter);

        addMoveButtonListener(btnUp, 0.0, -0.3);
        addMoveButtonListener(btnDown, 0.0, 0.3);
        addMoveButtonListener(btnLeft, -0.3, 0.0);
        addMoveButtonListener(btnRight, 0.3, 0.0);

        btnSave.addActionListener(actionEvent -> {
            Image currentImage = imageList.get(imageIndex);

            JFileChooser chooser = new JFileChooser(currentImage.getFolder());
            String newName = currentImage.getPureName() + ".png";
            chooser.setSelectedFile(new File(newName));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            chooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JPEG + PNG", new String[]{"jpg", "jpeg", "png"});
            chooser.addChoosableFileFilter(filter);

            int returnVal = chooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File fileName = chooser.getSelectedFile();
                System.out.println("Selected file for saving: " + fileName);

                boolean doSave = true;
                if (fileName.exists()) {
                    int answer = JOptionPane.showConfirmDialog(
                            null,
                            String.format(Config.getInstance().getL10n().getString("overwrite_confirm.msg"), fileName.getName()),
                            Config.getInstance().getL10n().getString("overwrite.confirm.title"),
                            JOptionPane.YES_NO_OPTION);
                    if (answer != 0) {
                        AstroCatGui.getInstance().setStatus("File saving aborted");
                        System.out.println("File saving aborted");
                        // indicate a break here
                        doSave = false;
                    }
                }
                if (doSave) {
                    BufferedImage imageToSave = imageDisplayPanel.exportVisibleImage();
                    try {
                        ImageIO.write(imageToSave, "png", fileName);
                        AstroCatGui.getInstance().setStatus(String.format("File %s has been saved.", fileName));
                        System.out.println(String.format("File %s has been saved.", fileName));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        spinnerFontSize.addChangeListener(changeEvent -> {
            SpinnerModel dateModel = spinnerFontSize.getModel();
            if (dateModel instanceof SpinnerNumberModel) {
                setOverlayFontSize(((SpinnerNumberModel) dateModel).getNumber().doubleValue());
            }
        });

        btnTopRight.addActionListener(actionEvent -> setOverlayLocation(new PointDouble(80.0, 5.0), currentOverlayFontSize));
        btnTopLeft.addActionListener(actionEvent -> setOverlayLocation(new PointDouble(3.0, 5.0), currentOverlayFontSize));
        btnBottomRight.addActionListener(actionEvent -> setOverlayLocation(new PointDouble(80.0, 80.0), currentOverlayFontSize));
        btnBottomLeft.addActionListener(actionEvent -> setOverlayLocation(new PointDouble(3.0, 80.0), currentOverlayFontSize));
        btnColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                overlayColor = JColorChooser.showDialog(null, "Overlay color", overlayColor);
                setOverlayColor(overlayColor);
                imageDisplayPanel.overlaysChanged();
            }
        });
    }

    class MoveTimerTask extends TimerTask {
        double dx, dy;

        public MoveTimerTask(double dx, double dy) {
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public void run() {
            shiftOverlays(dx, dy);
        }
    }

    private void addCheckBoxListener(JCheckBox check, JTextField tf, TextOverlay overlay) {
        check.addItemListener(itemEvent -> {
            tf.setEnabled(check.isSelected());
            overlay.setText(tf.isEnabled() ? tf.getText() : "");
            imageDisplayPanel.overlaysChanged();
        });
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

    private void addMoveButtonListener(JButton btn, double dx, double dy) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // do it once
                shiftOverlays(dx, dy);

                // schedule a 1 second timer that will start the Move-TimerTask, if not cancelled before
                scheduledMoveTimer = new Timer();
                scheduledMoveTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        moveTimer = new Timer(true);
                        moveTimer.scheduleAtFixedRate(new MoveTimerTask(dx, dy), 0, 100);
                    }
                }, 500);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (scheduledMoveTimer != null)
                    scheduledMoveTimer.cancel();
                if (moveTimer != null)
                    moveTimer.cancel();
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
            tfFilter.setText(session.getFilterInfoString());
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

        spinnerFontSize.setModel(new SpinnerNumberModel(initialOverlayFontSize, 1.0, 5.0, 0.1));

        imageDisplayPanel.replaceOverlays(overlayList);
    }

    private void setOverlayColor(Color color) {
        overlayObject.setColor(color);
        overlayObserver.setColor(color);
        overlayCamera.setColor(color);
        overlayTelescope.setColor(color);
        overlayExposure.setColor(color);
        overlayFilter.setColor(color);
    }

    private void setOverlayFontSize(double relFontSize) {
        currentOverlayFontSize = currentOverlayFontSize;

        // TODO: collect overlays in list
        overlayObject.setRelFontSize(relFontSize);
        overlayObserver.setRelFontSize(relFontSize);
        overlayCamera.setRelFontSize(relFontSize);
        overlayTelescope.setRelFontSize(relFontSize);
        overlayExposure.setRelFontSize(relFontSize);
        overlayFilter.setRelFontSize(relFontSize);

        // relocate Overlay lines according to new font-size
        setOverlayLocation(currentOverlayPosition, relFontSize);

        imageDisplayPanel.overlaysChanged();
    }

    private void setOverlayLocation(PointDouble p, double relFontSize) {
        currentOverlayPosition = p;
        // re-calculate positions ( Line distances )
        overlayObject.setPosition(p.add(0, 0));
        overlayObserver.setPosition(p.add(0, 1 * (currentOverlayFontSize + 1)));
        overlayTelescope.setPosition(p.add(0, 2 * (currentOverlayFontSize + 1)));
        overlayCamera.setPosition(p.add(0, 3 * (currentOverlayFontSize + 1)));
        overlayExposure.setPosition(p.add(0, 4 * (currentOverlayFontSize + 1)));
        overlayFilter.setPosition(p.add(0, 5 * (currentOverlayFontSize + 1)));

        imageDisplayPanel.overlaysChanged();
    }

    private void shiftOverlays(double dx, double dy) {
        setOverlayLocation(currentOverlayPosition.add(dx, dy), currentOverlayFontSize);
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
        panelNavigation.add(lblSessionName, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), new Dimension(200, -1), null, 0, false));
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
        panelElements.setLayout(new GridLayoutManager(18, 5, new Insets(0, 0, 0, 0), -1, -1));
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
        checkObject.setSelected(true);
        checkObject.setText("");
        panelElements.add(checkObject, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfObject = new JTextField();
        panelElements.add(tfObject, new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        checkObserver = new JCheckBox();
        checkObserver.setSelected(true);
        checkObserver.setText("");
        panelElements.add(checkObserver, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfObserver = new JTextField();
        panelElements.add(tfObserver, new GridConstraints(6, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkTelescope = new JCheckBox();
        checkTelescope.setSelected(true);
        checkTelescope.setText("");
        panelElements.add(checkTelescope, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfTelescope = new JTextField();
        panelElements.add(tfTelescope, new GridConstraints(8, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkCamera = new JCheckBox();
        checkCamera.setSelected(true);
        checkCamera.setText("");
        panelElements.add(checkCamera, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfCamera = new JTextField();
        panelElements.add(tfCamera, new GridConstraints(10, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkExposure = new JCheckBox();
        checkExposure.setSelected(true);
        checkExposure.setText("");
        panelElements.add(checkExposure, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfExposure = new JTextField();
        panelElements.add(tfExposure, new GridConstraints(12, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkFilter = new JCheckBox();
        checkFilter.setSelected(true);
        checkFilter.setText("");
        panelElements.add(checkFilter, new GridConstraints(14, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tfFilter = new JTextField();
        panelElements.add(tfFilter, new GridConstraints(14, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSelect = new JButton();
        this.$$$loadButtonText$$$(btnSelect, this.$$$getMessageFromBundle$$$("l10n", "btn_select.session"));
        panelElements.add(btnSelect, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panelElements.add(spacer1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panelElements.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panelElements.add(spacer3, new GridConstraints(7, 4, 8, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(10, -1), new Dimension(10, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("l10n", "common_object"));
        panelElements.add(label1, new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("l10n", "common_observer"));
        panelElements.add(label2, new GridConstraints(5, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_telescope"));
        panelElements.add(label3, new GridConstraints(7, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_main.camera"));
        panelElements.add(label4, new GridConstraints(9, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, this.$$$getMessageFromBundle$$$("l10n", "sessioninfo_total.exposure"));
        panelElements.add(label5, new GridConstraints(11, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$(label6, this.$$$getMessageFromBundle$$$("l10n", "astrocatgui_label_filter"));
        panelElements.add(label6, new GridConstraints(13, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnColor = new JButton();
        this.$$$loadButtonText$$$(btnColor, this.$$$getMessageFromBundle$$$("l10n", "common_color"));
        panelElements.add(btnColor, new GridConstraints(17, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSave = new JButton();
        this.$$$loadButtonText$$$(btnSave, this.$$$getMessageFromBundle$$$("l10n", "common_save"));
        panelElements.add(btnSave, new GridConstraints(17, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spinnerFontSize = new JSpinner();
        panelElements.add(spinnerFontSize, new GridConstraints(16, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, 32), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("rel. Font size");
        panelElements.add(label7, new GridConstraints(16, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelPosition = new JPanel();
        panelPosition.setLayout(new GridBagLayout());
        panelElements.add(panelPosition, new GridConstraints(15, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(16, -1), null, 0, false));
        btnLeft = new JButton();
        Font btnLeftFont = this.$$$getFont$$$(null, Font.BOLD, -1, btnLeft.getFont());
        if (btnLeftFont != null) btnLeft.setFont(btnLeftFont);
        btnLeft.setPreferredSize(new Dimension(80, 30));
        btnLeft.setText("<");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        panelPosition.add(btnLeft, gbc);
        btnRight = new JButton();
        Font btnRightFont = this.$$$getFont$$$(null, Font.BOLD, -1, btnRight.getFont());
        if (btnRightFont != null) btnRight.setFont(btnRightFont);
        btnRight.setPreferredSize(new Dimension(80, 30));
        btnRight.setText(">");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        panelPosition.add(btnRight, gbc);
        btnTopLeft = new JButton();
        btnTopLeft.setBackground(new Color(-10130835));
        btnTopLeft.setMaximumSize(new Dimension(20, 20));
        btnTopLeft.setMinimumSize(new Dimension(20, 20));
        btnTopLeft.setPreferredSize(new Dimension(20, 20));
        btnTopLeft.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelPosition.add(btnTopLeft, gbc);
        btnTopRight = new JButton();
        btnTopRight.setBackground(new Color(-10130835));
        btnTopRight.setMaximumSize(new Dimension(20, 20));
        btnTopRight.setMinimumSize(new Dimension(20, 20));
        btnTopRight.setPreferredSize(new Dimension(20, 20));
        btnTopRight.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panelPosition.add(btnTopRight, gbc);
        btnBottomLeft = new JButton();
        btnBottomLeft.setBackground(new Color(-10130835));
        btnBottomLeft.setMaximumSize(new Dimension(20, 20));
        btnBottomLeft.setMinimumSize(new Dimension(20, 20));
        btnBottomLeft.setPreferredSize(new Dimension(20, 20));
        btnBottomLeft.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        panelPosition.add(btnBottomLeft, gbc);
        btnBottomRight = new JButton();
        btnBottomRight.setBackground(new Color(-10130835));
        btnBottomRight.setMaximumSize(new Dimension(20, 20));
        btnBottomRight.setMinimumSize(new Dimension(20, 20));
        btnBottomRight.setPreferredSize(new Dimension(20, 20));
        btnBottomRight.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        panelPosition.add(btnBottomRight, gbc);
        btnDown = new JButton();
        Font btnDownFont = this.$$$getFont$$$(null, -1, -1, btnDown.getFont());
        if (btnDownFont != null) btnDown.setFont(btnDownFont);
        btnDown.setPreferredSize(new Dimension(80, 30));
        btnDown.setText("v");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelPosition.add(btnDown, gbc);
        btnUp = new JButton();
        Font btnUpFont = this.$$$getFont$$$(null, Font.BOLD, -1, btnUp.getFont());
        if (btnUpFont != null) btnUp.setFont(btnUpFont);
        btnUp.setPreferredSize(new Dimension(80, 30));
        btnUp.setText("^ ");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        panelPosition.add(btnUp, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("%");
        panelElements.add(label8, new GridConstraints(16, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
    private void $$$loadLabelText$$$(JLabel component, String text) {
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
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
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