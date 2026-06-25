package com.univalle.sudoku.model;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map;

/**
 * Holds puzzle state and rule validation for the 6x6 Sudoku game.
 */
public class SudokuBoard {

    public static final int SIZE = 6;
    public static final int BLOCK_ROWS = 2;
    public static final int BLOCK_COLS = 3;

    private final CellState[][] cells = new CellState[SIZE][SIZE];
    private final Random random = new Random();
    private final SudokuGenerator generator = new SudokuGenerator();
    // Historial LIFO de pistas usadas; util para analizar la secuencia de ayuda.
    private final Stack<Hint> hintHistory = new Stack<>();
    // Cola FIFO de pistas, util para estudiar orden de entrega durante la partida.
    private final Queue<Hint> hintQueue = new ArrayDeque<>();

    private record HintCandidate(CellState cell, int candidateCount, int randomTieBreaker) {
    }

    /**
     * Creates a new random game state.
     */
    public void newGame() {
        // Genera una solucion completa y luego una mascara para decidir celdas fijas.
        int[][] solution = generator.generateSolvedBoard();
        boolean[][] fixedMask = generator.createFixedMask();
        // Reinicia trazas de pistas para que no contaminen una nueva partida.
        hintHistory.clear();
        hintQueue.clear();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int initial = fixedMask[r][c] ? solution[r][c] : 0;
                // Cada celda guarda valor actual, valor solucion y si es fija/editable.
                cells[r][c] = new CellState(r, c, initial, solution[r][c], fixedMask[r][c]);
            }
        }
    }

    /**
     * Attempts to set a value in an editable cell.
     *
     * @param row row index
     * @param col column index
     * @param value 0 clears the cell, otherwise values 1 to 6
     */
    public void setValue(int row, int col, int value) {
        CellState cell = cells[row][col];
        if (cell.fixed()) {
            // Regla de negocio: las celdas iniciales no se pueden modificar.
            return;
        }
        if (value < 0 || value > SIZE) {
            // Corta valores fuera del dominio permitido (0..6).
            return;
        }
        cell.setValue(value);
    }

    /**
     * Computes invalid user-entered cells according to Sudoku rules.
     *
     * @return set of coordinates encoded as row * 10 + col
     */
    public Set<Integer> invalidCells() {
        // Acumula coordenadas en conflicto detectadas por fila, columna y bloque.
        Set<Integer> invalid = new HashSet<>();
        markDuplicatesByRow(invalid);
        markDuplicatesByColumn(invalid);
        markDuplicatesByBlock(invalid);
        return invalid;
    }

    /**
     * Returns all valid candidates for an empty editable cell.
     *
     * @param row row index
     * @param col column index
     * @return list of candidate values
     */
    public List<Integer> candidatesFor(int row, int col) {
        if (cells[row][col].fixed() || cells[row][col].value() != 0) {
            return List.of();
        }

        // TreeSet mantiene los candidatos ordenados y sin duplicados.
        TreeSet<Integer> candidates = new TreeSet<>();
        for (int value = 1; value <= SIZE; value++) {
            if (isPlacementValid(row, col, value)) {
                candidates.add(value);
            }
        }
        // Convierte al tipo de retorno requerido por la API publica.
        return new ArrayList<>(candidates);
    }

    /**
     * Provides and applies one hint by filling one empty cell with the solution value.
     *
     * @return hint information, or empty if there are no empty editable cells
     */
    public Optional<Hint> applyHint() {
        // Deque para recorrer celdas vacias en orden de descubrimiento.
        Deque<CellState> empties = new ArrayDeque<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                CellState cell = cells[r][c];
                if (!cell.fixed() && cell.value() == 0) {
                    empties.offerLast(cell);
                }
            }
        }

        if (empties.isEmpty()) {
            return Optional.empty();
        }

        // Prioriza celdas con menos candidatos (estrategia "mas restringida primero").
        PriorityQueue<HintCandidate> rankedHints = new PriorityQueue<>(
                Comparator.comparingInt(HintCandidate::candidateCount)
                        .thenComparingInt(HintCandidate::randomTieBreaker));

        while (!empties.isEmpty()) {
            CellState candidateCell = empties.pollFirst();
            int candidateCount = candidatesFor(candidateCell.row(), candidateCell.col()).size();
            // randomTieBreaker evita sesgo fijo cuando dos celdas tienen misma prioridad.
            rankedHints.offer(new HintCandidate(candidateCell, candidateCount, random.nextInt(10_000)));
        }

        CellState selected = rankedHints.poll().cell();
        selected.setValue(selected.solutionValue());

        Hint hint = new Hint(selected.row(), selected.col(), selected.solutionValue());
        // Registra la pista en dos estructuras para fines didacticos de LIFO/FIFO.
        hintHistory.push(hint);
        hintQueue.offer(hint);
        return Optional.of(hint);
    }

    /**
     * Checks if the puzzle is completely and correctly solved.
     *
     * @return true when all cells are filled and valid
     */
    public boolean isSolved() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                CellState cell = cells[r][c];
                if (cell.value() == 0 || cell.value() != cell.solutionValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public CellState cellAt(int row, int col) {
        return cells[row][col];
    }

    private boolean isPlacementValid(int row, int col, int value) {
        // Verifica unicidad en fila y columna.
        for (int i = 0; i < SIZE; i++) {
            if (i != col && cells[row][i].value() == value) {
                return false;
            }
            if (i != row && cells[i][col].value() == value) {
                return false;
            }
        }

        // Verifica unicidad dentro del bloque 2x3 correspondiente.
        int startRow = (row / BLOCK_ROWS) * BLOCK_ROWS;
        int startCol = (col / BLOCK_COLS) * BLOCK_COLS;
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                int currentRow = startRow + r;
                int currentCol = startCol + c;
                if (currentRow == row && currentCol == col) {
                    continue;
                }
                if (cells[currentRow][currentCol].value() == value) {
                    return false;
                }
            }
        }
        return true;
    }

    private int code(int row, int col) {
        // Codificacion compacta de coordenadas para usar Set<Integer>.
        return row * 10 + col;
    }

    private void markDuplicatesByRow(Set<Integer> invalid) {
        for (int row = 0; row < SIZE; row++) {
            // value -> lista de posiciones donde aparece ese valor en la fila.
            Map<Integer, List<Integer>> positionsByValue = new HashMap<>();
            for (int col = 0; col < SIZE; col++) {
                int value = cells[row][col].value();
                if (value == 0) {
                    continue;
                }
                positionsByValue.computeIfAbsent(value, key -> new ArrayList<>()).add(code(row, col));
            }
            collectDuplicatedPositions(positionsByValue, invalid);
        }
    }

    private void markDuplicatesByColumn(Set<Integer> invalid) {
        for (int col = 0; col < SIZE; col++) {
            // value -> lista de posiciones donde aparece ese valor en la columna.
            Map<Integer, List<Integer>> positionsByValue = new HashMap<>();
            for (int row = 0; row < SIZE; row++) {
                int value = cells[row][col].value();
                if (value == 0) {
                    continue;
                }
                positionsByValue.computeIfAbsent(value, key -> new ArrayList<>()).add(code(row, col));
            }
            collectDuplicatedPositions(positionsByValue, invalid);
        }
    }

    private void markDuplicatesByBlock(Set<Integer> invalid) {
        for (int blockRow = 0; blockRow < SIZE / BLOCK_ROWS; blockRow++) {
            for (int blockCol = 0; blockCol < SIZE / BLOCK_COLS; blockCol++) {
                int startRow = blockRow * BLOCK_ROWS;
                int startCol = blockCol * BLOCK_COLS;
                // value -> lista de posiciones donde aparece ese valor en el bloque.
                Map<Integer, List<Integer>> positionsByValue = new HashMap<>();

                for (int rowOffset = 0; rowOffset < BLOCK_ROWS; rowOffset++) {
                    for (int colOffset = 0; colOffset < BLOCK_COLS; colOffset++) {
                        int row = startRow + rowOffset;
                        int col = startCol + colOffset;
                        int value = cells[row][col].value();
                        if (value == 0) {
                            continue;
                        }
                        positionsByValue.computeIfAbsent(value, key -> new ArrayList<>()).add(code(row, col));
                    }
                }
                collectDuplicatedPositions(positionsByValue, invalid);
            }
        }
    }

    private void collectDuplicatedPositions(Map<Integer, List<Integer>> positionsByValue, Set<Integer> invalid) {
        for (List<Integer> positions : positionsByValue.values()) {
            if (positions.size() > 1) {
                // Si un valor aparece 2+ veces, todas esas posiciones son invalidas.
                invalid.addAll(positions);
            }
        }
    }
}
