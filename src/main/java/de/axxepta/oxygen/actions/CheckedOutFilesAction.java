package de.axxepta.oxygen.actions;

import de.axxepta.oxygen.api.*;
import de.axxepta.oxygen.utils.DialogTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.ecss.extensions.api.component.AuthorComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 11.07.2016.
 */
public class CheckedOutFilesAction extends AbstractAction {

    private static final Logger logger = LogManager.getLogger(CheckedOutFilesAction.class);

    public CheckedOutFilesAction() {
        super();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<BaseXResource> files = getCheckedOutFiles();
        if (files.size() > 0) {
            showDialog(files);
        }
    }

    private void showDialog(List<BaseXResource> files) {
        JFrame parentFrame = (JFrame) ((new AuthorComponentFactory()).getWorkspaceUtilities().getParentFrame());

        final ResourceListModel listModel = new ResourceListModel(files);
        final JList resultList = new JList(listModel);

        final JDialog resultsDialog = DialogTools.getOxygenDialog(parentFrame, "Checked out files");

        JPanel content = SearchInPathAction.createSelectionListPanel("Choose files to check in.", resultList);
        resultList.setSelectionInterval(0, resultList.getModel().getSize() - 1);

        JPanel buttonsPanel = new JPanel();
        JButton checkInButton = new JButton(new AbstractAction("Check in selected files") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int fileIndex : resultList.getSelectedIndices()) {
                    try {
                        URL url = new URL((String) listModel.getElementAt(fileIndex));
                        CheckInAction.checkIn(url);
                    } catch (MalformedURLException mue) {
                        logger.debug("Failed to get URL from selected item. ", mue.getMessage());
                    }
                }
                resultsDialog.dispose();
            }
        });
        buttonsPanel.add(checkInButton);
        JButton exitButton = new JButton(new CloseDialogAction("Exit", resultsDialog));
        buttonsPanel.add(exitButton);
        content.add(buttonsPanel, BorderLayout.SOUTH);

        DialogTools.wrapAndShow(resultsDialog, content, parentFrame, 500, 300);
    }

    private static List<BaseXResource> getCheckedOutFiles() {
        List<BaseXResource> fileList = new ArrayList<>();
        String checkedOut;
        try (Connection connection = BaseXConnectionWrapper.getConnection()) {
            checkedOut = connection.xquery(ConnectionUtils.getQuery("checked-out-files"));
        } catch (IOException ioe) {
            logger.debug("Failed to get list of checked out files. ", ioe.getMessage());
            return fileList;
        }
        if (!checkedOut.equals("")) {
            final String[] results = checkedOut.split("\r?\n");
            for(int r = 0, rl = results.length; r < rl; r += 2) {
                fileList.add(new BaseXResource(results[r + 1], BaseXType.RESOURCE, BaseXSource.get(results[r])));
            }
        }
        return fileList;
    }


    private class ResourceListModel extends AbstractListModel {

        private List<BaseXResource> data;

        ResourceListModel(List<BaseXResource> data) {
            this.data = data;
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int index) {
            return data.get(index).getURLString();
        }
    }

}
