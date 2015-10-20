package de.axxepta.oxygen.workspace;

import ro.sync.exml.workspace.api.options.WSOptionChangedEvent;
import ro.sync.exml.workspace.api.options.WSOptionListener;

import javax.swing.*;

/**
 * @author Markus on 20.10.2015.
 */
public class BaseXOptionListener extends WSOptionListener {

    public BaseXOptionListener(String s) {
        super(s);
    }

    @Override
    public void optionValueChanged(WSOptionChangedEvent wsOptionChangedEvent) {
        BaseXOptionPage.refreshFromOptions();
    }

}
