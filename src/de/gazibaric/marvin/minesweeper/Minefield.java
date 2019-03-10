package de.gazibaric.marvin.minesweeper;

// This file is part of Minesweeper, created on 17.12.2016 by (c) Marvin Gazibarić.
// You are generally not allowed to modify, distribute or use this code,
// when you do not have the explicit permission to do so.
// For questions about this, refer to the LICENSE.md file probably provided with the project files.

import java.util.List;
import java.util.Vector;

public interface Minefield extends RenderCanvas {

    class Field {

        private List<Field> adjacent;

        private boolean isMine;
        private boolean revealed;
        private boolean marked;

        public Field() {
            adjacent = new Vector<>();
            this.isMine = false;
            this.revealed = false;
        }

        public int getAdajacentMineCount() {
            int count = 0;
            for (Field f : adjacent)
                if (f.isMine) count++;
            return count;
        }

        public boolean isRevealed() {
            return revealed;
        }

        public boolean reveal() {
            this.revealed = true;
            if (!this.isMine() && getAdajacentMineCount() == 0) {
                for (Field f : adjacent)
                    if (!f.isRevealed())
                        f.reveal();
            }
            return this.isMine();
        }

        public boolean isMarked() {
            return marked;
        }

        public void toggleMarked() {
            this.marked = !this.marked;
        }

        public boolean isMine() {
            return isMine;
        }

        public void setMine(boolean mine) {
            isMine = mine;
        }

        public void addAdjacent(Field adjacent) {
            this.adjacent.add(adjacent);
        }
    }
}
