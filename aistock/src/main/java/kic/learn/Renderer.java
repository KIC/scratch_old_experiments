package kic.learn;

import javax.swing.*;
import java.awt.*;

/**
 * Created by kic on 27.03.15.
 */
public class Renderer {
    private final Grid grid;
    private final int bars;

    public Renderer(int bars, int qunatiles) {
        this.grid = new Grid(500, 500, qunatiles);
        this.bars = bars;
    }

    public void draw(int[][][] matrix) {
        draw(matrix, null);
    }

    public void draw(int[][][] matrix, double[][][] reconstructed) {
        grid.reset();

        int offset=1;
        for (int bar=0; bar<matrix.length; bar++) {

            // get some space
            if (bar>bars) offset +=3;

            for (int x=0; x<matrix[bar].length; x++) {
                for (int y=0; y<matrix[bar][x].length; y++) {
                    if (matrix[bar][x][y] >= 1) {
                        int r = x == 2 ? 255 : 0;
                        int g = x == 0 ? 255 : 0;
                        int b = 0;

                        grid.fillCell(x + offset, y, r, g, b);
                    }
                }
            }

            offset+=4;
        }

        // predictions
        if (reconstructed == null) return;

        offset+=3;
        double satt = 255d;
        double gain = 1.1;

        for (int bar=bars; bar < reconstructed.length; bar++) {
            for (int x = 0; x < reconstructed[bar].length; x++) {
                for (int y = 0; y < reconstructed[bar][x].length; y++) {
                    double v = reconstructed[bar][x][y];

                    if (x == 0) {
                        // green
                        grid.fillCell(x + offset, y,
                                255 - (int) (v * gain * satt),
                                255,
                                255 - (int) (v * gain * satt)
                        );
                    } else if (x == 1) {
                        // black
                        grid.fillCell(x + offset, y,
                                255 - (int) (v * satt),
                                255 - (int) (v * satt),
                                255 - (int) (v * satt)
                        );
                    } else if (x == 2) {
                        // red
                        grid.fillCell(x + offset, y,
                                255,
                                255 - (int) (v * gain * satt),
                                255 - (int) (v * gain * satt)
                        );
                    }
                }
            }
        }
    }

    public void pause(){
        JOptionPane.showMessageDialog(null, "Check Chart");
    }

    public void show() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                JFrame window = new JFrame();
                window.setSize(grid.gridWidth() + 2 * grid.margin, grid.gridHeight() + 3 * grid.margin);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.add(grid);
                window.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        Renderer r = new Renderer(30, 60);
        r.grid.fillCell(1,1, 255, 0, 0);
        r.grid.fillCell(0,1, 255, 0, 0);
        r.show();
    }
}
