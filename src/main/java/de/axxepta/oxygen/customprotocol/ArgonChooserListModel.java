package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.ArgonEntity;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus on 27.07.2016.
 */
public class ArgonChooserListModel extends AbstractListModel {


    private final List<Element> data;

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

    ArgonEntity getTypeAt(int index) {
        if (index > getSize())
            return null;
        else
            return data.get(index).getType();
    }

    String getNameAt(int index) {
        if (index > getSize())
            return null;
        else
            return data.get(index).getName();
    }

    void setData(List<Element> newData) {
        int oldSize = getSize();
        data.clear();
        fireIntervalRemoved(this, 0, oldSize - 1);
        data.addAll(newData);
        fireIntervalAdded(this, 0, getSize() - 1);
    }

    void insertElement(Element newElement) {
        int oldSize = getSize();
        boolean inserted = false;
        for (int index = 1; index < oldSize; index++) {
            if ((newElement.getName().compareTo(data.get(index).getName()) < 0) ||
                    data.get(index).getType().equals(ArgonEntity.FILE)) {
                data.add(index, newElement);
                fireIntervalAdded(this, index, index);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            data.add(newElement);
            fireIntervalAdded(this, oldSize, oldSize);
        }
    }


    public static class Element {

        private final ArgonEntity type;
        private final String name;

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
