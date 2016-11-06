package de.axxepta.oxygen.workspace;

import de.axxepta.oxygen.api.BaseXConnectionWrapper;
import ro.sync.exml.workspace.api.options.WSOptionChangedEvent;
import ro.sync.exml.workspace.api.options.WSOptionListener;


/**
 * @author Markus on 20.10.2015.
 */
public class ArgonOptionListener extends WSOptionListener {

    public ArgonOptionListener(String s) {
        super(s);
    }

    @Override
    public void optionValueChanged(WSOptionChangedEvent wsOptionChangedEvent) {
        BaseXConnectionWrapper.refreshFromOptions(false);
    }

}
