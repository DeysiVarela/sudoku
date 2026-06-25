module com.univalle.sudoku {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.univalle.sudoku to javafx.fxml;
    opens com.univalle.sudoku.controller to javafx.fxml;

    exports com.univalle.sudoku;
    exports com.univalle.sudoku.controller;
    exports com.univalle.sudoku.model;
}
