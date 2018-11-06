package de.axxepta.oxygen.customprotocol;

import de.axxepta.oxygen.api.ArgonEntity;
import de.axxepta.oxygen.customprotocol.ArgonChooserListModel.Element;
import org.junit.Test;

import java.util.Arrays;

import static de.axxepta.oxygen.api.ArgonEntity.DB;
import static org.junit.Assert.assertEquals;

public class ArgonChooserDialogTest {

    @Test
    public void getResourceString() throws Exception {
        assertEquals("", ArgonChooserDialog.getResourceString(Arrays.asList(
                new Element(DB, "foo"))));
        assertEquals("bar", ArgonChooserDialog.getResourceString(Arrays.asList(
                new Element(DB, "foo"), new Element(ArgonEntity.DIR, "bar"))));
        assertEquals("bar/baz", ArgonChooserDialog.getResourceString(Arrays.asList(
                new Element(DB, "foo"),
                new Element(ArgonEntity.DIR, "bar"),
                new Element(ArgonEntity.FILE, "baz"))));
    }

}