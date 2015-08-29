package de.axxepta.oxygen.tree;

import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Created by daltiparmak on 14.04.15.
 */
public class BasexTreeUI extends BasicTreeUI {
	@Override
	protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets,
			Rectangle bounds, TreePath path, int row, boolean isExpanded,
			boolean hasBeenExpanded, boolean isLeaf) {
		boolean isRowSelected = tree.isRowSelected(row);

		if (isRowSelected && isLeaf) {
			Graphics g2 = g.create();

			g2.setColor(Color.RED);
			g2.fillRect(0, bounds.y, tree.getWidth(), bounds.height);

			g2.dispose();
		}

		if (!isRowSelected && isLeaf) {
			Graphics g2 = g.create();

			if (row % 2 == 0) {
				g2.setColor(Color.WHITE);
			} else {
				g2.setColor(new Color(230, 230, 230));
			}
			g2.fillRect(0, bounds.y, tree.getWidth(), bounds.height);

			g2.dispose();
		}
		super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded,
				hasBeenExpanded, isLeaf);
	}

}
