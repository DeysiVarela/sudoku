package com.univalle.sudoku.model;

/**
 * Represents the state flags of a board cell.
 */
public class CellState {

    private final int row;
    private final int col;
    private int value;
    private final int solutionValue;
    private final boolean fixed;

    /**
     * Creates an immutable-position cell state.
     *
     * @param row row index
     * @param col column index
     * @param value current value, 0 means empty
     * @param solutionValue expected solution value
     * @param fixed true if the value cannot be edited
     */
    public CellState(int row, int col, int value, int solutionValue, boolean fixed) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.solutionValue = solutionValue;
        this.fixed = fixed;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    public int value() {
        return value;
    }

    public int solutionValue() {
        return solutionValue;
    }

    public boolean fixed() {
        return fixed;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
