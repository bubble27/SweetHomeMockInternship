/*
 * ViewableLevelsPrinter.java
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.sweethome3d.swing;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;

/**
 * Prints the pages of every viewable level of a home one level after the other,
 * in the order of home levels. Each level is printed by temporarily selecting it in home,
 * and the level selected before printing is always restored.
 */
class ViewableLevelsPrinter {
  /**
   * Prints the pages of the level currently selected in home.
   */
  interface SelectedLevelPrinter {
    /**
     * Returns the count of pages needed to print the selected level, possibly 0.
     */
    int getPageCount(Graphics g, PageFormat pageFormat);

    /**
     * Prints the page at <code>pageIndex</code> of the selected level
     * and returns {@link Printable#PAGE_EXISTS} or {@link Printable#NO_SUCH_PAGE}.
     */
    int print(Graphics g, PageFormat pageFormat, int pageIndex);
  }

  private final Home                 home;
  private final SelectedLevelPrinter selectedLevelPrinter;

  ViewableLevelsPrinter(Home home, SelectedLevelPrinter selectedLevelPrinter) {
    this.home = home;
    this.selectedLevelPrinter = selectedLevelPrinter;
  }

  /**
   * Prints the page at <code>pageIndex</code> among the pages of all the viewable levels,
   * or among the pages of the selected level if home doesn't contain any viewable level.
   */
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    List<Level> printedLevels = getViewableLevels();
    if (printedLevels.isEmpty()) {
      return this.selectedLevelPrinter.print(g, pageFormat, pageIndex);
    }

    Level selectedLevel = this.home.getSelectedLevel();
    try {
      int levelPageIndex = pageIndex;
      for (Level level : printedLevels) {
        // Temporarily select printed level to print its items
        this.home.setSelectedLevel(level);
        int levelPageCount = this.selectedLevelPrinter.getPageCount(g, pageFormat);
        if (levelPageIndex < levelPageCount) {
          return this.selectedLevelPrinter.print(g, pageFormat, levelPageIndex);
        }
        levelPageIndex -= levelPageCount;
      }
      return Printable.NO_SUCH_PAGE;
    } finally {
      // Restore selected level even if a page couldn't be printed
      this.home.setSelectedLevel(selectedLevel);
    }
  }

  /**
   * Returns the viewable levels of home in their order.
   */
  private List<Level> getViewableLevels() {
    List<Level> viewableLevels = new ArrayList<Level>();
    for (Level level : this.home.getLevels()) {
      if (level.isViewable()) {
        viewableLevels.add(level);
      }
    }
    return viewableLevels;
  }
}
