package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.api.BaseXSource;
import de.axxepta.oxygen.rest.BaseXRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.sync.document.DocumentPositionedInfo;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblems;
import ro.sync.exml.workspace.api.editor.validation.ValidationProblemsFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ArgonValidationProblemsFilter extends ValidationProblemsFilter {

    private static final Logger logger = LogManager.getLogger(ArgonValidationProblemsFilter.class);

    private final WSEditor editorAccess;

    ArgonValidationProblemsFilter(WSEditor editorAccess) {
        super();
        this.editorAccess = editorAccess;
    }

    @Override
    public void filterValidationProblems(ValidationProblems validationProblems) {
/*      // get content of editor window
        String editorContent;
        logger.debug("filter validation problems");
        try {
            InputStream editorStream = editorAccess.createContentInputStream();
            Scanner s = new java.util.Scanner(editorStream, "UTF-8").useDelimiter("\\A");
            editorContent = s.hasNext() ? s.next() : "";
            editorStream.close();
        } catch (IOException er) {
            logger.error(er);
            editorContent = "";
        }
        // pass content of editor window to BaseXRequest with queryTest
        ArrayList<String> valProbStr;
        try {
            BaseXRequest testQuery = new BaseXRequest("parse",
                    BaseXSource.DATABASE, editorContent);
            valProbStr = testQuery.getResult();
        } catch (Exception er) {
            logger.error("query to BaseX failed");
            valProbStr = new ArrayList<>();
            valProbStr.add("1");
            valProbStr.add("1");
            valProbStr.add("Fatal BaseX request error: "+er.getMessage());
            er.printStackTrace();
        }
        // build DocumentPositionedInfo list from query return;
        List<DocumentPositionedInfo> problemList = new ArrayList<>();
        if (valProbStr.size() > 0) {
            DocumentPositionedInfo dpi =
                    new DocumentPositionedInfo(DocumentPositionedInfo.SEVERITY_ERROR, valProbStr.get(2), "",
                            Integer.parseInt(valProbStr.get(0)), Integer.parseInt(valProbStr.get(1)), 0);
            problemList.add(dpi);
        }*/

        /*try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    problemList = isQueryValid(editorAccess);
                }
            });
        } catch (InvocationTargetException ite) {
            logger.error(ite);
        } catch (InterruptedException ie) {
            logger.error(ie);
        }*/

        final List<DocumentPositionedInfo> problemList = new ArrayList<>();
        Thread validatorThread = new Thread(new QueryValidator(editorAccess, problemList));
        validatorThread.run();
        try {
            validatorThread.join();
        } catch (InterruptedException ie) {
            // ToDo:
        }

        //The DocumentPositionInfo represents an error with location in the document and has a constructor like:
        //  public DocumentPositionedInfo(int severity, String message, String systemID, int line, int column, int length)
        validationProblems.setProblemsList(problemList);
        super.filterValidationProblems(validationProblems);
    }

    private List<DocumentPositionedInfo> isQueryValid(WSEditor editorAccess) {
        // get content of editor window
        String editorContent;
        logger.debug("filter validation problems");
        try {
            InputStream editorStream = editorAccess.createContentInputStream();
            Scanner s = new java.util.Scanner(editorStream, "UTF-8").useDelimiter("\\A");
            editorContent = s.hasNext() ? s.next() : "";
            editorStream.close();
        } catch (IOException er) {
            logger.error(er);
            editorContent = "";
        }
        // pass content of editor window to BaseXRequest with queryTest
        List<String> valProbStr;
        try {
            BaseXRequest testQuery = new BaseXRequest("parse",
                    BaseXSource.DATABASE, editorContent);
            valProbStr = testQuery.getResult();
        } catch (Exception er) {
            logger.error("query to BaseX failed");
            valProbStr = new ArrayList<>();
            valProbStr.add("1");
            valProbStr.add("1");
            valProbStr.add("Fatal BaseX request error: "+er.getMessage());
            er.printStackTrace();
        }
        // build DocumentPositionedInfo list from query return;
        List<DocumentPositionedInfo> problemList = new ArrayList<>();
        if (valProbStr.size() > 0) {
            DocumentPositionedInfo dpi =
                    new DocumentPositionedInfo(DocumentPositionedInfo.SEVERITY_ERROR, valProbStr.get(2), "",
                            Integer.parseInt(valProbStr.get(0)), Integer.parseInt(valProbStr.get(1)), 0);
            problemList.add(dpi);
        }
        return problemList;
    }

    private static class QueryValidator implements Runnable {

        private final WSEditor editorAccess;
        private final List<DocumentPositionedInfo> problemList;

        QueryValidator(WSEditor editorAccess, List<DocumentPositionedInfo> problemList) {
            this.editorAccess = editorAccess;
            this.problemList = problemList;
        }

        public void run() {
            // get content of editor window
            String editorContent;
            logger.debug("filter validation problems");
            try {
                InputStream editorStream = editorAccess.createContentInputStream();
                Scanner s = new java.util.Scanner(editorStream, "UTF-8").useDelimiter("\\A");
                editorContent = s.hasNext() ? s.next() : "";
                editorStream.close();
            } catch (IOException er) {
                logger.error(er);
                editorContent = "";
            }
            // pass content of editor window to BaseXRequest with queryTest
            List<String> valProbStr;
            try {
                BaseXRequest testQuery = new BaseXRequest("parse",
                        BaseXSource.DATABASE, editorContent);
                valProbStr = testQuery.getResult();
            } catch (Exception er) {
                logger.error("query to BaseX failed");
                valProbStr = new ArrayList<>();
                valProbStr.add("1");
                valProbStr.add("1");
                valProbStr.add("Fatal BaseX request error: " + er.getMessage());
                er.printStackTrace();
            }
            // build DocumentPositionedInfo list from query return;
            if (valProbStr.size() > 0) {
                DocumentPositionedInfo dpi =
                        new DocumentPositionedInfo(DocumentPositionedInfo.SEVERITY_ERROR, valProbStr.get(2), "",
                                Integer.parseInt(valProbStr.get(0)), Integer.parseInt(valProbStr.get(1)), 0);
                problemList.add(dpi);
            }
/*            try {
                wait(5000);
            } catch (InterruptedException ie) {}*/
        }

    }

}
