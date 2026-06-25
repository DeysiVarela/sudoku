package com.univalle.sudoku.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Generates random valid 6x6 Sudoku boards and initial puzzle masks.
 */
public class SudokuGenerator {

    private static final int SIZE = 6;
    private static final int BLOCK_ROWS = 2;
    private static final int BLOCK_COLS = 3;
    private static final int FIXED_PER_BLOCK = 2;

    private final Random random = new Random();

    /**
     * Generates a fully solved random Sudoku board.
     *
     * @return solved board matrix
     */
    public int[][] generateSolvedBoard() {
        // Tablero vacio que se llenara con backtracking.
        int[][] board = new int[SIZE][SIZE];
        fillBoard(board, 0, 0);
        return board;
    }

    /**
     * Creates a puzzle mask with exactly two fixed values per 2x3 block.
     *
     * @return true for fixed cells, false for editable cells
     */
    public boolean[][] createFixedMask() {
        // true = celda fija, false = celda editable por el jugador.
        boolean[][] fixedMask = new boolean[SIZE][SIZE];

        for (int blockRow = 0; blockRow < SIZE / BLOCK_ROWS; blockRow++) {
            for (int blockCol = 0; blockCol < SIZE / BLOCK_COLS; blockCol++) {
                // Vector para ilustrar estructura clasica sincronizada.
                Vector<int[]> cells = blockCells(blockRow, blockCol);
                // Mezcla posiciones para elegir celdas fijas aleatorias por bloque.
                Collections.shuffle(cells, random);
                for (int i = 0; i < FIXED_PER_BLOCK; i++) {
                    int[] cell = cells.get(i);
                    fixedMask[cell[0]][cell[1]] = true;
                }
            }
        }
        return fixedMask;
    }

    private boolean fillBoard(int[][] board, int row, int col) {
        // Caso base: si se completo la fila SIZE, el tablero quedo resuelto.
        if (row == SIZE) {
            return true;
        }

        // Avance secuencial celda por celda.
        int nextRow = col == SIZE - 1 ? row + 1 : row;
        int nextCol = col == SIZE - 1 ? 0 : col + 1;

        // Construye lista 1..6 y la aleatoriza para generar tableros distintos.
        List<Integer> numbers = new ArrayList<>();
        for (int n = 1; n <= SIZE; n++) {
            numbers.add(n);
        }
        Collections.shuffle(numbers, random);

        for (int value : numbers) {
            if (isSafe(board, row, col, value)) {
                // Paso de decision: coloca valor candidato.
                board[row][col] = value;
                if (fillBoard(board, nextRow, nextCol)) {
                    return true;
                }
                // Paso de retroceso: deshace decision si no llevo a solucion.
                board[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isSafe(int[][] board, int row, int col, int value) {
        // Regla 1 y 2: valor unico en fila y columna.
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == value || board[i][col] == value) {
                return false;
            }
        }

        // Regla 3: valor unico dentro del bloque 2x3.
        int startRow = (row / BLOCK_ROWS) * BLOCK_ROWS;
        int startCol = (col / BLOCK_COLS) * BLOCK_COLS;
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                if (board[startRow + r][startCol + c] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    private Vector<int[]> blockCells(int blockRow, int blockCol) {
        // Devuelve coordenadas [fila,columna] de las 6 celdas del bloque indicado.
        Vector<int[]> cells = new Vector<>();
        int rowStart = blockRow * BLOCK_ROWS;
        int colStart = blockCol * BLOCK_COLS;
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                cells.add(new int[]{rowStart + r, colStart + c});
            }
        }
        return cells;
    }
}
