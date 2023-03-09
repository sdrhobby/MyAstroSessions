package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.common.EditorPanel;
import de.sdr.astro.cat.gui.*;
import de.sdr.astro.cat.model.AstroObject;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.model.Session;
import de.sdr.astro.cat.util.Util;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AstroCatGui {

    // this allows to perform some logic parts in the Kotlin world
    public JPanel topPanelAstroCatGui;
    private JTree treeSessionFolders;
    private JTextField textFieldPath;
    private JButton buttonSelect;
    private JButton buttonScan;
    private JLabel labelStatus;
    private JPanel panelContent;
    private JPanel panelFolders;
    private JPanel panelTimeline;
    private JPanel panelAstroObjects;

    private JCheckBox checkBoxAutoScan;
    private JTree treeSessionTimeline;
    private JTree treeAstroObjects;
    private JTextField tfFilter;
    private JTabbedPane tabbedPaneTrees;
    private JButton btnClearFilter;
    private final ImagePanel panelImage = new ImagePanel();
    private final ImageMetadataListPanel panelImageMetadataList = new ImageMetadataListPanel();

    private Model dataModel;
    private Skymap skymap;

    private DefaultMutableTreeNode folderTreeRoot;
    private DefaultMutableTreeNode timelineTreeRoot;
    private DefaultMutableTreeNode astrobjectsTreeRoot;

    private JFrame mainFrame;

    public JFrame getMainFrame() {
        return mainFrame;
    }

    public void setMainFrame(JFrame mainFrame) {

        this.mainFrame = mainFrame;

        // perform automatic scan on startup?
        // this is delayed until here to ensure that the mainFrame is known already
        checkBoxAutoScan.setSelected(Config.getInstance().getAutoscan());
        if (Config.getInstance().getAutoscan())
            doScan();

    }

    public JPanel getTopPanel() {
        return topPanelAstroCatGui;
    }

    private static AstroCatGui singleton;
    private JDialog skymapDialog;
    private final PopupMenuHandler popupMenuHandler;

    public static AstroCatGui getInstance() {
        if (singleton == null) {
            singleton = new AstroCatGui();
        }
        return singleton;
    }

    private AstroCatGui() {
        singleton = this;
        popupMenuHandler = new PopupMenuHandler(treeSessionFolders);
        // initialize scan path from configuration (taken from config file)
        textFieldPath.setText(Config.getInstance().getScanFolder());

        treeSessionFolders.setModel(new InvisibleTreeModel(getFolderTreeRoot(), true, true));

        treeSessionTimeline.setModel(new InvisibleTreeModel(getTimelineTreeRoot(), true, true));

        treeAstroObjects.setModel(new InvisibleTreeModel(getAstrobjectsTreeRoot(), true, true));

        checkBoxAutoScan.addItemListener(
                itemEvent -> Config.getInstance().setAutoscan(checkBoxAutoScan.isSelected())
        );

        buttonSelect.addActionListener(actionEvent -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setSelectedFile(new File(textFieldPath.getText()));
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                textFieldPath.setText(chooser.getSelectedFile().getPath());
            }
        });
        buttonScan.addActionListener(actionEvent -> doScan());
        treeSessionFolders.addTreeSelectionListener(treeSelectionEvent -> handleTreeSelectionChange(treeSessionFolders));
        treeSessionTimeline.addTreeSelectionListener(treeSelectionEvent -> handleTreeSelectionChange(treeSessionTimeline));
        treeAstroObjects.addTreeSelectionListener(treeSelectionEvent -> handleTreeSelectionChange(treeAstroObjects));
        treeSessionFolders.addMouseListener(new PopupMouseAdapter(treeSessionFolders, false));
        treeSessionTimeline.addMouseListener(new PopupMouseAdapter(treeSessionTimeline, true));
        treeAstroObjects.addMouseListener(new PopupMouseAdapter(treeAstroObjects, true));

        tfFilter.addActionListener(actionEvent -> filterTree(tfFilter.getText()));
        btnClearFilter.addActionListener(actionEvent -> filterTree(""));
        tfFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                filterTree(tfFilter.getText());
            }
        });
    }

    private void filterTree(String filterText) {
        TreeFilter tf = new TreeFilter(getActiveTree());
        int deepestMatchLevel = tf.applyFilter(filterText);
        expandNodesToLevel(getActiveTree(), getActiveTreeRoot(), deepestMatchLevel);
        System.out.printf("Deepest match level was: %d%n", deepestMatchLevel);

    }

    private JTree getActiveTree() {
        switch (tabbedPaneTrees.getSelectedIndex()) {
            case 0:
                return treeSessionFolders;
            case 1:
                return treeSessionTimeline;
            case 2:
                return treeAstroObjects;
        }
        return null;
    }

    private DefaultMutableTreeNode getActiveTreeRoot() {
        switch (tabbedPaneTrees.getSelectedIndex()) {
            case 0:
                return getFolderTreeRoot();
            case 1:
                return getTimelineTreeRoot();
            case 2:
                return getAstrobjectsTreeRoot();
        }
        return null;
    }

    private void doScan() {
        setCursor(Cursor.WAIT_CURSOR);
        setStatus(String.format("%s %s", Config.getInstance().getL10n().getString("astrocatgui_status_scanning.folder"), textFieldPath.getText()));

        // clear the content panel
        panelContent.removeAll();

        // put the scan folder to the config and save the config-file
        Config.getInstance().setScanFolder(textFieldPath.getText());
        Config.getInstance().saveConfig();

        // perform actual scan operation in background
        SwingWorker<Model, Void> scanWorker = new SwingWorker<Model, Void>() {
            @Override
            protected Model doInBackground() {
                return new Model(textFieldPath.getText());
            }

            @Override
            protected void done() {
                try {
                    dataModel = get();
                    updateFolderTree(dataModel);
                    updateTimelineTree();
                    updateAstroObjectsTree();

                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                treeSessionFolders.expandRow(0);
                setStatus(MessageFormat.format(Config.getInstance().getL10n().getString("astrocatgui_status.finished_scanning"), textFieldPath.getText()));
                setCursor(Cursor.DEFAULT_CURSOR);

            }
        };
        scanWorker.execute();
    }

    public void setStatus(@NotNull String status) {
        labelStatus.setText(status);
        labelStatus.repaint();
    }

    @NotNull
    public DefaultMutableTreeNode getFolderTreeRoot() {
        if (folderTreeRoot == null) {
            folderTreeRoot = new PathObjectTreeNode(Config.getInstance().getL10n().getString("common_folders"), Model.ASTRO_OBJECT, new AstroObject(textFieldPath.getText(), false), true);
        }
        return folderTreeRoot;
    }

    public DefaultMutableTreeNode getTimelineTreeRoot() {
        if (timelineTreeRoot == null) {
            timelineTreeRoot = new TypedNode(Config.getInstance().getL10n().getString("common_sessions"), Model.SCAFFOLD, true);
        }
        return timelineTreeRoot;
    }

    public DefaultMutableTreeNode getAstrobjectsTreeRoot() {
        if (astrobjectsTreeRoot == null) {
            astrobjectsTreeRoot = new TypedNode(Config.getInstance().getL10n().getString("common_astroobjects"), Model.SCAFFOLD, true);
        }
        return astrobjectsTreeRoot;
    }

    @NotNull
    public DefaultTreeModel getFolderTreeModel() {
        return (DefaultTreeModel) treeSessionFolders.getModel();
    }

    public DefaultTreeModel getTimelineTreeModel() {
        return (DefaultTreeModel) treeSessionTimeline.getModel();
    }

    public DefaultTreeModel getAstroObjectsTreeModel() {
        return (DefaultTreeModel) treeAstroObjects.getModel();
    }

    public static void setCursor(int cursorType) {
        SwingUtilities.getRoot(singleton.getTopPanel()).setCursor(Cursor.getPredefinedCursor(cursorType));
    }

    private void updateFolderTree(Model model) {
        DefaultTreeModel treeModel = getFolderTreeModel();
        DefaultMutableTreeNode rootNode = getFolderTreeRoot();
        rootNode.removeAllChildren();

        model.getAstroObjects().forEach(astroObject -> rootNode.add(createAstroObjectNode(astroObject)));
        treeModel.reload();
    }

    /***
     * take the values from the folder tree and tranform them to a timeline tree
     * showing root/year/month/sessions
     */
    private void updateTimelineTree() {
        Enumeration<TreeNode> nodesEnum = folderTreeRoot.breadthFirstEnumeration();
        SortedSet<Session> sessions = new TreeSet<>();

        // create plain set of sessions, sorted by creation date top-down
        while (nodesEnum.hasMoreElements()) {
            PathObjectTreeNode node = (PathObjectTreeNode) nodesEnum.nextElement();
            if (Model.SESSION.equals(node.getNodeType())) {
                sessions.add((Session) node.getNodeObject());
            }
        }
        System.out.println("--------Sessions: ");
        int currentYear = 0;
        int currentMonth = -1;
        DefaultTreeModel treeModel = getTimelineTreeModel();
        DefaultMutableTreeNode rootNode = getTimelineTreeRoot();
        rootNode.removeAllChildren();

        TypedNode yearNode = null;
        TypedNode monthNode = null;

        // iterate sorted sessions, from newest to oldest
        for (Session session : sessions) {
            System.out.println(session.getName());
            int year = session.getDate().getYear();
            int month = session.getDate().getMonthValue();
            // if next year starts, create year node under root
            if (year != currentYear) {
                yearNode = new TypedNode("" + year, Model.YEAR, true);
                rootNode.add(yearNode);
            }
            // if next month starts, create month node under year
            if (month != currentMonth) {
                monthNode = new TypedNode(Util.getMonthName(month), Model.MONTH, true);
                yearNode.add(monthNode);
            }
            PathObjectTreeNode sessionNode = createSessionNode(session);
            sessionNode.setUserObject(String.format("%s   (%s)", session.getName(), session.getAstroObjectName()));
            monthNode.add(sessionNode);
            currentYear = year;
            currentMonth = month;
        }
        treeModel.reload();
    }

    /***
     * take the values from the folder tree and tranform them to a timeline tree
     * showing root/year/month/sessions
     */
    private void updateAstroObjectsTree() {
        Enumeration<TreeNode> nodesEnum = folderTreeRoot.breadthFirstEnumeration();
        SortedSet<AstroObject> objects = new TreeSet<>();

        // create plain set of sessions, sorted by creation date top-down
        while (nodesEnum.hasMoreElements()) {
            PathObjectTreeNode node = (PathObjectTreeNode) nodesEnum.nextElement();
            if (Model.ASTRO_OBJECT.equals(node.getNodeType())) {
                AstroObject ao = (AstroObject) node.getNodeObject();
                // only take "real" objects into account (the ones that have sessions assigned)
                if (ao.hasSessions())
                    objects.add((AstroObject) node.getNodeObject());
            }
        }
        // now we have a sorted set of AstroObjects
        getAstrobjectsTreeRoot().removeAllChildren();

        for (AstroObject ao : objects) {
            // add only AstroObjects that have sessions - no recursion
            getAstrobjectsTreeRoot().add(createAstroObjectNode(ao, true));
        }
        getAstroObjectsTreeModel().reload();
    }


    private PathObjectTreeNode createAstroObjectNode(AstroObject astroObject) {
        return createAstroObjectNode(astroObject, false);
    }

    private PathObjectTreeNode createAstroObjectNode(AstroObject astroObject, boolean addOnlySessions) {
//        PathObjectTreeNode aoNode = new PathObjectTreeNode(astroObject.getName(), Model.ASTRO_OBJECT, new AstroObject(textFieldPath.getText()));
        PathObjectTreeNode aoNode = new PathObjectTreeNode(astroObject.getName(), Model.ASTRO_OBJECT, astroObject, true);
        if (!addOnlySessions) {
            astroObject.getAstroObjects().forEach(ao -> aoNode.add(createAstroObjectNode(ao)));
        }
        astroObject.getSessions().forEach(session -> aoNode.add(createSessionNode(session)));
        return aoNode;
    }

    public PathObjectTreeNode createSessionNode(Session session) {
        PathObjectTreeNode sessionNode = new PathObjectTreeNode(session.getName(), Model.SESSION, session, true);
        session.getImageMap().forEach((type, imageList) -> {
            PathObjectTreeNode typeNode = new PathObjectTreeNode(String.format("%s (%d)", type, imageList.size()), type, session, true);
            if (Model.LIGHTS.equals(type)) {
                // potentially create sub-nodes for each filter
                addLightsWithFilterNodes(session, typeNode);
            } else {
                // these are plain structures, just create the type-node (dark, flat etc.) and add images directly
                imageList.forEach(image -> {
                    PathObjectTreeNode imageNode = new PathObjectTreeNode(image.getName(), image.getType(), image, false);
                    typeNode.add(imageNode);
                });
            }
            sessionNode.add(typeNode);
        });
        return sessionNode;
    }

    public void addLightsWithFilterNodes(Session session, PathObjectTreeNode lightsNode) {
        Map<String, List<Image>> lightFiltersMap = session.createLightFiltersMap();
        lightFiltersMap.forEach((filter, imageList) -> {
            final PathObjectTreeNode activeNode;
            if (!filter.isEmpty()) {
                activeNode = new PathObjectTreeNode(String.format("%s (%d)", filter, imageList.size()), Model.FILTER, session, true);
                lightsNode.add(activeNode);
            } else {
                activeNode = lightsNode;
            }

            imageList.forEach(image -> {
                PathObjectTreeNode imageNode = new PathObjectTreeNode(image.getName(), image.getType(), image, false);
                // if there is no filter, add directly to the lights-node
                activeNode.add(imageNode);
            });
        });
    }


    public PathObjectTreeNode getTreeNodeByNodeObject(Object nodeObject) {
        return getNodeByUserObject((PathObjectTreeNode) getFolderTreeRoot(), nodeObject);
    }

    public void selectTreeNodeByNodeObject(Object nodeObject) {
        PathObjectTreeNode targetNode = getNodeByUserObject((PathObjectTreeNode) getFolderTreeRoot(), nodeObject);
        if (targetNode != null)
            navigateToNode(targetNode);
    }

    private void navigateToNode(PathObjectTreeNode node) {
        TreeNode[] nodes = getFolderTreeModel().getPathToRoot(node);
        TreePath tpath = new TreePath(nodes);
        treeSessionFolders.scrollPathToVisible(tpath);
        treeSessionFolders.setSelectionPath(tpath);
    }

    private PathObjectTreeNode getNodeByUserObject(PathObjectTreeNode startNode, Object nodeObject) {
        Enumeration<TreeNode> nodesEnum = startNode.breadthFirstEnumeration();
        while (nodesEnum.hasMoreElements()) {
            PathObjectTreeNode node = (PathObjectTreeNode) nodesEnum.nextElement();
            if (node.getNodeObject().equals(nodeObject)) {
                return node;
            }
        }
        return null;
    }

    private void expandNodesToLevel(JTree tree, DefaultMutableTreeNode node, int level) {
        int nodeLevel = node.getLevel() + 1;
        if (nodeLevel <= level) {
            tree.expandPath(new TreePath(node.getPath()));
            if (nodeLevel < level) {
                Enumeration<TreeNode> children = node.children();
                while (children.hasMoreElements()) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                    expandNodesToLevel(tree, child, level);
                }
            }
        }
    }

    public Skymap getSkymap() {
        if (skymap == null) {
            skymap = new Skymap();
        }
        return skymap;
    }

    private JDialog getSkymapDialog() {
        if (skymapDialog == null) {
            skymapDialog = new JDialog(this.getMainFrame());
            getSkymap().setParentDialog(skymapDialog);
            skymapDialog.setSize(820, 860);
            skymapDialog.setTitle("SkyMap");
            skymapDialog.setContentPane(getSkymap().getTopPanel());
            skymapDialog.pack();
            skymapDialog.setLocationRelativeTo(null);
            skymapDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
        return skymapDialog;
    }


    public void showSkymapDialog(AstroObject astroObject) {
        if (this.dataModel != null)
            getSkymap().updateDataModel(this.dataModel);
        // select given AstroObject
        if (astroObject != null) {
            getSkymap().selectAstroObject(astroObject);
        }
        getSkymapDialog().setVisible(true);
    }

    /**
     * This handler is valid for all types of trees that we have in the Navigation panel, since all nodes are TypedNodes
     * There are more specific node types for each type of tree. All of them can be handled here
     *
     * @param tree ... the tree that the current selection belongs to
     */
    private void handleTreeSelectionChange(JTree tree) {
        // all nodes in all our trees are "TypedNodes"
        TypedNode typedNode = (TypedNode) tree.getLastSelectedPathComponent();
        if (typedNode == null) return;
        System.out.printf("tree selection: %s%n", typedNode.getNodeType());

        // some nodes may be also PathObjectTreeNodes
        PathObjectTreeNode pathNode = null;
        if (typedNode instanceof PathObjectTreeNode) {
            pathNode = (PathObjectTreeNode) typedNode;
        }

        panelContent.removeAll();
        switch (typedNode.getNodeType()) {
            case Model.ASTRO_OBJECT: {
                AstroObject ao = (AstroObject) pathNode.getNodeObject();
                // if astroobject has assigned sessions, show a session overview with preview pictures
                if (ao.hasSessions()) {
                    panelContent.add(new ObjectLinksOverviewPanel(ao).getTopPanel());
                }
                // if it is something else check what we want to display
                else if (ao.isText()) {
                    panelContent.add(new EditorPanel(ao.getPath()).getTopPanel());
                }
                break;
            }
            // details for overall session
            case Model.SESSION: {
                panelContent.add(new SessionInfoPanel((Session) pathNode.getNodeObject()).getTopPanel());
                break;
            }
            // details for lists of images
            case Model.DARKS:
            case Model.FLATS:
            case Model.BIASES:
            case Model.LIGHTS: {
                panelImageMetadataList.initialize((Session) pathNode.getNodeObject(), pathNode.getNodeType(), "");
                panelContent.add(panelImageMetadataList.getTopPanel());
                break;
            }
            case Model.FILTER: {
                // TODO: this is kind of a hack - take the filter from the first part of the node label
                String f = pathNode.getUserObject().toString();
                String filter = f.substring(0, f.indexOf('(') - 1);
                panelImageMetadataList.initialize((Session) pathNode.getNodeObject(), Model.LIGHTS, filter);
                panelContent.add(panelImageMetadataList.getTopPanel());
                break;
            }
            case Model.RESULTS: {
                panelContent.add(new ObjectLinksOverviewPanel((Session) pathNode.getNodeObject()).getTopPanel());
                break;
            }

            // details for individual image
            case Model.RESULT:
            case Model.DARK:
            case Model.FLAT:
            case Model.BIAS:
            case Model.LIGHT: {
                panelImage.initialize((Image) pathNode.getNodeObject());
                panelContent.add(panelImage.getTopPanel());
                System.out.println(pathNode.getChildCount());
                break;
            }
        }
        panelContent.repaint();
        panelContent.revalidate();
    }

    class PopupMouseAdapter extends MouseAdapter {
        private final JTree tree;
        private final boolean limitedFlag;

        public PopupMouseAdapter(JTree tree, boolean limitedFlag) {
            this.tree = tree;
            this.limitedFlag = limitedFlag;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // force selection also on right mouse button
            if (SwingUtilities.isRightMouseButton(e)) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                tree.setSelectionPath(selPath);
                if (selRow > -1) {
                    tree.setSelectionRow(selRow);
                }
            }
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                PathObjectTreeNode node = (PathObjectTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) return;
                JPopupMenu pop = popupMenuHandler.createPopup(node.getNodeObject(), limitedFlag);
                if (pop != null) {
                    System.out.printf("show popup for node: %s%n", node.getNodeObject());
                    pop.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }
}
