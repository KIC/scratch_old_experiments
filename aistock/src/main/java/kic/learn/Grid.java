package kic.learn;

import javax.swing.*;
import java.awt.*;

/**
 * Created by kic on 28.03.15.
 */
public class Grid extends JPanel {
    private final int width, height, nrOfQuaniles, cellSize;
    private final Color[][] cells;
    public final int margin = 3;

    public Grid(int width, int height, int nrOfQuaniles) {
        this.width = width;
        this.height = height;
        this.nrOfQuaniles = nrOfQuaniles;
        this.cellSize = height / nrOfQuaniles;

        cells = new Color[width/cellSize][nrOfQuaniles];
        reset();
    }

    public int gridWidth() {
        return width;
    }

    public int gridHeight() {
        return height;
    }

    public void reset() {
        for (int x=0; x<cells.length; x++) {
            for (int y=0;y<cells[x].length; y++) {
                cells[x][y] = Color.WHITE;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        synchronized (this) {
            super.paintComponent(g);

            for (int x=0; x<cells.length; x++) {
                for (int y=0;y<cells[x].length; y++) {
                    g.setColor(cells[x][y]);
                    g.fillRect(margin + x*cellSize, height - (y * cellSize), cellSize, cellSize);

                    g.setColor(Color.GRAY);
                    g.drawRect(margin + x*cellSize, height - (y * cellSize), cellSize, cellSize);
                }
            }
        }
    }


    public void fillCell(int x, int y, int r, int g, int b) {
        synchronized (this) {
            cells[x][y] = new Color(Math.max(0, Math.min(r, 254)), Math.max(0, Math.min(g, 254)), Math.max(0, Math.min(b, 254)));
            repaint();
        }
    }
}
