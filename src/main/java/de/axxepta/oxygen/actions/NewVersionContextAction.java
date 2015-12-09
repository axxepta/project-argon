package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.customprotocol.BaseXByteArrayOutputStream;
import de.axxepta.oxygen.tree.BasexTree;
import de.axxepta.oxygen.tree.TreeListener;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Markus on 04.12.2015.
 * This action should be called from a context menu in the database tzee.
 * It initiates an update of the version (and revision) number and storage of the updated file to BaseX.
 * If the selected file is opened in an editor window (not necessarily the current one), the
 */
public class NewVersionContextAction extends AbstractAction {

    final BasexTree tree;
    final DefaultTreeModel treeModel;
    final TreeListener treeListener;

    public NewVersionContextAction(String name, Icon icon, BasexTree tree, TreeListener treeListener){
        super(name, icon);
        this.tree = tree;
        this.treeModel = (DefaultTreeModel) tree.getModel();
        this.treeListener = treeListener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
/*
        // get URL from click
        // check file type (exit, if not XML or xquery)
        // check whether URL is opened in any editor
        // if necessary, change in editor text mode
        // get document either from editor or as ByteInputStream from database
        VersionRevisionUpdater updater;
        // update version and revision
        updater = new VersionRevisionUpdater(doc, "XML");
        updater = new VersionRevisionUpdater(inputStream, "XML");
        byte[] outputArray = updater.updateVersion();

        try (ByteArrayOutputStream os = new BaseXByteArrayOutputStream(source, url)) {
            os.write(outputArray);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't store transferred object\n" + file.toString()
                    + "\nto database.", "Drag&Drop Error", JOptionPane.PLAIN_MESSAGE);
        }
        // [write changed document back into editor window if open] executed in updater
        // if necessary, change back to previously selected editor window
        */
    }

}
