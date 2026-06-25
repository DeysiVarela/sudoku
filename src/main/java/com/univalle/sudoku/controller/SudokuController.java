package com.univalle.sudoku.controller;

import com.univalle.sudoku.model.CellState;
import com.univalle.sudoku.model.Hint;
import com.univalle.sudoku.model.SudokuBoard;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.Set;

/**
 * JavaFX controller responsible for UI rendering and event handling.
 */
public class SudokuController {

    private static final int SIZE = SudokuBoard.SIZE;
    private static final PseudoClass INVALID_CLASS = PseudoClass.getPseudoClass("invalid");
    private static final PseudoClass FIXED_CLASS = PseudoClass.getPseudoClass("fixed");
    private static final PseudoClass HINTED_CLASS = PseudoClass.getPseudoClass("hinted");

    @FXML
    private GridPane boardGrid;

    @FXML
    private Label statusLabel;

    @FXML
    private Button newGameButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button checkButton;

    private final SudokuBoard board = new SudokuBoard();
    private final TextField[][] cells = new TextField[SIZE][SIZE];

    /**
     * Defines a tiny interface to isolate cell input processing.
     */
    private interface CellInputHandler {
        void onInput(int row, int col, String text);
    }

    /**
     * Adapter from text change events to board updates.
     */
    private class CellTextChangeAdapter implements ChangeListener<String> {

        private final int row;
        private final int col;
        private final CellInputHandler inputHandler;

        CellTextChangeAdapter(int row, int col, CellInputHandler inputHandler) {
            this.row = row;
            this.col = col;
            this.inputHandler = inputHandler;
        }

        @Override
        public void changed(javafx.beans.value.ObservableValue<? extends String> observable, String oldValue, String newValue) {
            inputHandler.onInput(row, col, newValue);
        }
    }

    @FXML
    private void initialize() {
        // 1) Construye la grilla visual (TextField por celda).
        setupGrid();
        // 2) Conecta eventos de botones con acciones del juego.
        bindButtons();
        // 3) Inicia una partida para mostrar un estado listo para jugar.
        startNewGame();
    }

    private void bindButtons() {
        newGameButton.setOnAction(event -> startNewGame());
        helpButton.setOnAction(event -> useHint());
        checkButton.setOnAction(event -> checkProgress());
    }

    private void setupGrid() {
        // Reinicia la grilla para evitar nodos duplicados al reconstruir UI.
        boardGrid.getChildren().clear();
        boardGrid.getColumnConstraints().clear();
        boardGrid.getRowConstraints().clear();

        for (int i = 0; i < SIZE; i++) {
            // Distribuye columnas y filas de forma uniforme (6x6).
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / SIZE);
            column.setHalignment(HPos.CENTER);
            boardGrid.getColumnConstraints().add(column);

            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / SIZE);
            row.setValignment(VPos.CENTER);
            boardGrid.getRowConstraints().add(row);
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Crea, registra y ubica cada celda en su coordenada [fila, columna].
                TextField textField = buildCell(row, col);
                cells[row][col] = textField;
                boardGrid.add(textField, col, row);
            }
        }
    }

    private TextField buildCell(int row, int col) {
        TextField textField = new TextField();
        textField.getStyleClass().add("sudoku-cell");
        textField.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        textField.textProperty().addListener(new CellTextChangeAdapter(row, col, this::onCellInput));
        textField.addEventFilter(KeyEvent.KEY_TYPED, this::allowOnlyValidTypedChar);
        textField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleDeleteKey);

        textField.setOnMouseClicked(event -> {
            textField.requestFocus();
            textField.selectAll();
        });

        if (col % 3 == 0) {
            // Marca borde grueso izquierdo de cada bloque 2x3.
            textField.getStyleClass().add("block-left-border");
        }
        if (row % 2 == 0) {
            // Marca borde grueso superior de cada bloque 2x3.
            textField.getStyleClass().add("block-top-border");
        }
        if (col == SIZE - 1) {
            textField.getStyleClass().add("block-right-border");
        }
        if (row == SIZE - 1) {
            textField.getStyleClass().add("block-bottom-border");
        }

        return textField;
    }

    private void allowOnlyValidTypedChar(KeyEvent event) {
        String character = event.getCharacter();
        if (character == null || character.isBlank()) {
            return;
        }
        // Solo admite digitos 1..6 para mantener reglas del Sudoku 6x6.
        if (!character.matches("[1-6]")) {
            event.consume();
        }
    }

    private void handleDeleteKey(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            TextField selected = (TextField) event.getSource();
            if (!selected.isEditable()) {
                event.consume();
            }
        }
    }

    private void startNewGame() {
        // Solicita un tablero nuevo al modelo y luego lo pinta en pantalla.
        board.newGame();
        renderBoard();
        statusLabel.setText("Se inicio un nuevo juego. Completa el tablero con numeros del 1 al 6.");
    }

    private void renderBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                CellState cellState = board.cellAt(row, col);
                TextField field = cells[row][col];

                field.setText(cellState.value() == 0 ? "" : String.valueOf(cellState.value()));
                // Las celdas fijas vienen del tablero inicial y no se pueden editar.
                field.setEditable(!cellState.fixed());
                field.pseudoClassStateChanged(FIXED_CLASS, cellState.fixed());
                field.pseudoClassStateChanged(INVALID_CLASS, false);
                field.pseudoClassStateChanged(HINTED_CLASS, false);
            }
        }
        // Recalcula conflictos para que el estado visual sea consistente.
        refreshValidation();
    }

    private void onCellInput(int row, int col, String text) {
        CellState cellState = board.cellAt(row, col);
        if (cellState.fixed()) {
            // Si la celda era fija, restaura su valor original.
            cells[row][col].setText(String.valueOf(cellState.value()));
            return;
        }

        if (text == null || text.isBlank()) {
            // Entrada vacia equivale a limpiar la celda.
            board.setValue(row, col, 0);
            refreshValidation();
            return;
        }

        String clean = text.substring(text.length() - 1);
        if (!clean.matches("[1-6]")) {
            // Si llega un valor invalido por pegado o entrada no controlada, se descarta.
            cells[row][col].setText("");
            board.setValue(row, col, 0);
            refreshValidation();
            return;
        }

        cells[row][col].setText(clean);
        board.setValue(row, col, Integer.parseInt(clean));
        refreshValidation();
    }

    private void refreshValidation() {
        // El modelo devuelve coordenadas codificadas de celdas con conflicto.
        Set<Integer> invalid = board.invalidCells();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                TextField field = cells[row][col];
                boolean isInvalid = invalid.contains(row * 10 + col);
                field.pseudoClassStateChanged(INVALID_CLASS, isInvalid);
            }
        }

        if (board.isSolved()) {
            statusLabel.setText("Felicidades, resolviste el Sudoku.");
        } else if (!invalid.isEmpty()) {
            statusLabel.setText("Hay conflictos de reglas en las celdas resaltadas.");
        } else {
            // Estado intermedio: no esta resuelto, pero tampoco hay conflictos actuales.
            statusLabel.setText("El tablero es valido hasta ahora. Continua.");
        }
    }

    private void useHint() {
        // El modelo decide una celda vacia y coloca el valor correcto de la solucion.
        board.applyHint().ifPresentOrElse(this::applyHintVisuals,
                () -> statusLabel.setText("No hay celdas editables vacias disponibles para pistas."));
    }

    private void applyHintVisuals(Hint hint) {
        TextField field = cells[hint.row()][hint.col()];
        field.setText(String.valueOf(hint.value()));
        field.pseudoClassStateChanged(HINTED_CLASS, true);
        refreshValidation();
        statusLabel.setText("Pista usada en fila " + (hint.row() + 1) + ", columna " + (hint.col() + 1) + ".");
    }

    private void checkProgress() {
        if (board.isSolved()) {
            statusLabel.setText("Tablero perfecto. Ganaste.");
        } else if (!board.invalidCells().isEmpty()) {
            statusLabel.setText("Corrige las celdas invalidas antes de terminar.");
        } else {
            statusLabel.setText("Buen progreso. Continua llenando celdas vacias.");
        }
    }
}
