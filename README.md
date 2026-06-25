# Sudoku 6x6 - JavaFX MVC

Proyecto educativo de Sudoku 6x6 implementado con JavaFX, patron MVC y documentacion Javadoc.

## Estado actual del proyecto

- Interfaz traducida al espanol.
- Generacion dinamica del tablero 6x6 al iniciar o crear nuevo juego.
- Validacion en tiempo real por fila, columna y bloque 2x3.
- Celdas fijas (2 por bloque) resaltadas y no editables.
- Sistema de pistas con insercion de un valor valido.
- Mensajes de estado y realimentacion visual para conflictos.

## Arquitectura MVC implementada

- model: logica del juego, generador de tablero, validacion y pistas.
- controller: manejo de eventos JavaFX e integracion UI-modelo.
- resources (vista): FXML y estilos CSS.

Clases clave:

- src/main/java/com/univalle/sudoku/model/SudokuBoard.java
- src/main/java/com/univalle/sudoku/model/SudokuGenerator.java
- src/main/java/com/univalle/sudoku/controller/SudokuController.java
- src/main/resources/com/univalle/sudoku/sudoku-view.fxml

## Estructuras de datos usadas

Ademas de arreglos y listas, se usan estructuras vistas en clase:

- List / ArrayList
- Set / HashSet / TreeSet
- Map / HashMap
- Queue / Deque (ArrayDeque)
- PriorityQueue
- Stack
- Vector

## Requisitos

- Java 25 LTS
- Maven 3.9+ (se recomienda usar Maven Wrapper del proyecto)

## Ejecutar

Windows PowerShell:

```bash
.\mvnw.cmd clean javafx:run
```

Linux/macOS:

```bash
./mvnw clean javafx:run
```

## Compilar

Windows:

```bash
.\mvnw.cmd -DskipTests compile
```

Linux/macOS:

```bash
./mvnw -DskipTests compile
```

## Documentacion Javadoc

El proyecto esta documentado con comentarios Javadoc en clases y metodos principales del modelo/controlador.

Generar Javadoc:

```bash
.\mvnw.cmd -DskipTests javadoc:javadoc
```

Salida:

- target/reports/apidocs/index.html

## Cobertura de historias de usuario

- HU-1: tablero, generacion dinamica, celdas fijas y controles.
- HU-2: seleccion de celda, entrada 1-6 y borrado.
- HU-3: validacion en tiempo real con resaltado de conflictos.
- HU-4: boton de pista ilimitado con insercion valida.
