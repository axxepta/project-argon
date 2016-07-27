package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.ArgonEntity;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 27.07.2016.
 */
class ArgonChooserListModel extends AbstractListModel {


    private List<Element> data;

    ArgonChooserListModel(List<Element> data) {
        this.data = new ArrayList<>();
        if (data != null)
            this.data.addAll(data);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public Object getElementAt(int index) {
        if (index > getSize())
            return null;
        else
            return data.get(index);
    }

    void setData(List<Element> newData) {
        int oldSize = getSize();
        data.clear();
        fireIntervalRemoved(this, 0, oldSize - 1);
        data.addAll(newData);
        fireIntervalAdded(this, 0, getSize() - 1);
    }


    static class Element {

        private ArgonEntity type;
        private String name;

        Element(ArgonEntity type, String name) {
            this.type = type;
            this.name = name;
        }

        public ArgonEntity getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }
}
