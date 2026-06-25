# Sudoku 6x6 - JavaFX MVC

Mini project for a 6x6 Sudoku game built with Java 17+, JavaFX, and MVC.

## Features

- Dynamic 6x6 board generation on every app start.
- Sudoku rules validation in real time (rows, columns, and 2x3 blocks).
- Keyboard and mouse interaction for cell editing.
- Fixed initial values (2 per 2x3 block), highlighted and non-editable.
- Hint system that inserts a valid value into an empty editable cell.
- Visual feedback for invalid cells and status messages.

## Project Structure (MVC)

- `model`: game state, generator, hint logic, and validation.
- `controller`: JavaFX event handling and UI orchestration.
- `resources`: FXML view and CSS theme.

## Requirements

- Java SE 17 or higher
- Maven 3.9+

## Run

```bash
mvn clean javafx:run
```

## Build

```bash
mvn clean package
```

## Generate Javadoc (English comments)

```bash
mvn javadoc:javadoc
```

Output location:

- `target/site/apidocs/index.html`

## Suggested Git Workflow

```bash
git init
git add .
git commit -m "Initial Sudoku 6x6 JavaFX MVC project"
git branch -M main
git remote add origin <your-repository-url>
git push -u origin main
```

## User Stories Coverage Summary

- HU-1: Grid, dynamic puzzle, fixed highlighted cells, and clear controls.
- HU-2: Cell selection, keyboard input 1-6, and deletion support.
- HU-3: Real-time validation with visual conflict feedback.
- HU-4: Unlimited hint button with valid suggestion insertion.
