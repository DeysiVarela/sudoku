package com.univalle.sudoku.model;

/**
 * Represents a hint suggested to the player.
 *
 * @param row row index in the board
 * @param col column index in the board
 * @param value value suggested for the cell
 */
public record Hint(int row, int col, int value) {
}
