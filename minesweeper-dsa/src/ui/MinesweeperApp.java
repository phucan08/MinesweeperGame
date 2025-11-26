package ui;

import ai.AISolver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Board;
import model.Cell;
import model.Difficulty;
import model.GameState;
import service.GameService;
import service.StatisticsService;

import java.util.List;

public class MinesweeperApp extends Application {

    private static final String STATS_FILE = "best_times.dat";

    private GameService gameService;
    private AISolver aiSolver;
    private Button[][] buttons;
    private Label statusLabel;
    private ComboBox<Difficulty> difficultyCombo;

    // MINE COUNTER + TIMER
    private Label mineLabel;
    private Label timeLabel;
    private Timeline timer;
    private long startMillis;
    private long lastElapsedMillis;
    private boolean timerRunning = false;

    // BEST TIMES (persistent)
    private StatisticsService statisticsService;

    @Override
    public void start(Stage primaryStage) {
        // load statistics từ file (nếu có)
        statisticsService = StatisticsService.loadFromFile(STATS_FILE);

        gameService = new GameService(Difficulty.MEDIUM);
        aiSolver = new AISolver(gameService);

        BorderPane root = new BorderPane();

        // TOP: controls
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // CENTER: board
        GridPane boardPane = createBoardPane();
        root.setCenter(boardPane);

        // BOTTOM: status
        statusLabel = new Label("Playing...");
        statusLabel.setPadding(new Insets(5));
        root.setBottom(statusLabel);

        // Timer setup
        setupTimer();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Minesweeper DSA + AI + Undo/Redo");
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshBoardView();
    }

    // ----------------- TOP BAR -----------------

    private HBox createTopBar() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(10));
        box.setAlignment(Pos.CENTER_LEFT);

        difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll(
                Difficulty.EASY,
                Difficulty.MEDIUM,
                Difficulty.HARD,
                Difficulty.EXTREME
        );
        difficultyCombo.setValue(Difficulty.MEDIUM);

        Button newGameBtn = new Button("New Game");
        newGameBtn.setOnAction(e -> {
            gameService = new GameService(difficultyCombo.getValue());
            aiSolver = new AISolver(gameService);
            rebuildBoard();
            resetTimer();
            updateMineLabel();
            setStatus("New game: " + difficultyCombo.getValue());
        });

        Button aiMoveBtn = new Button("AI Move");
        aiMoveBtn.setOnAction(e -> {
            if (!timerRunning && gameService.getState() == GameState.PLAYING) {
                startTimer();
            }
            boolean moved = aiSolver.makeOneMove();
            refreshBoardView();
            if (!moved) setStatus("AI: no move (stuck or finished)");
            updateGameStateLabel();
        });

        Button undoBtn = new Button("Undo");
        undoBtn.setOnAction(e -> {
            if (gameService.undo()) {
                refreshBoardView();
                updateGameStateLabel();
                updateMineLabel();
                setStatus("Undo successful");
            } else {
                setStatus("Nothing to undo");
            }
        });

        Button redoBtn = new Button("Redo");
        redoBtn.setOnAction(e -> {
            if (gameService.redo()) {
                refreshBoardView();
                updateGameStateLabel();
                updateMineLabel();
                setStatus("Redo successful");
            } else {
                setStatus("Nothing to redo");
            }
        });

        Button helpBtn = new Button("How to Play");
        helpBtn.setOnAction(e -> showTutorial());

        Button bestBtn = new Button("Best Times");
        bestBtn.setOnAction(e -> showBestTimes());

        mineLabel = new Label();
        updateMineLabel();

        timeLabel = new Label("Time: 0s");

        box.getChildren().addAll(
                new Label("Difficulty:"),
                difficultyCombo,
                newGameBtn,
                aiMoveBtn,
                undoBtn,
                redoBtn,
                helpBtn,
                bestBtn,
                new Label(" | "),
                mineLabel,
                timeLabel
        );
        return box;
    }

    // ----------------- BOARD UI -----------------

    private GridPane createBoardPane() {
        Board board = gameService.getBoard();
        int rows = board.getRows();
        int cols = board.getCols();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(1);
        grid.setVgap(1);
        grid.setAlignment(Pos.CENTER);

        buttons = new Button[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Button btn = new Button();
                btn.setPrefSize(30, 30);
                final int rr = r, cc = c;
                btn.setOnMouseClicked(e -> {
                    if (gameService.getState() != GameState.PLAYING) return;

                    if (!timerRunning) {
                        startTimer();
                    }

                    if (e.getButton() == MouseButton.PRIMARY) {
                        gameService.reveal(rr, cc);
                    } else if (e.getButton() == MouseButton.SECONDARY) {
                        gameService.toggleFlag(rr, cc);
                    }
                    refreshBoardView();
                    updateGameStateLabel();
                });
                buttons[r][c] = btn;
                grid.add(btn, c, r);
            }
        }

        return grid;
    }

    private void rebuildBoard() {
        BorderPane root = (BorderPane) statusLabel.getScene().getRoot();
        GridPane newPane = createBoardPane();
        root.setCenter(newPane);
        refreshBoardView();
    }

    private void refreshBoardView() {
        Board board = gameService.getBoard();
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                Button btn = buttons[r][c];

                if (cell.isRevealed()) {
                    if (cell.isMine()) {
                        btn.setText("💣");
                        btn.setDisable(true);
                    } else {
                        int n = cell.getAdjacentMines();
                        btn.setText(n == 0 ? "" : String.valueOf(n));
                        btn.setDisable(true);
                    }
                } else {
                    if (cell.isFlagged()) {
                        btn.setText("🚩");
                    } else {
                        btn.setText("");
                    }
                    btn.setDisable(false);
                }
            }
        }
        updateMineLabel();
    }

    private void updateGameStateLabel() {
        if (gameService.getState() == GameState.WON) {
            setStatus("YOU WIN! 🎉");
            stopTimer();
            // lưu best time cho độ khó hiện tại
            if (lastElapsedMillis > 0) {
                statisticsService.addRecord(gameService.getDifficulty(), lastElapsedMillis);
                statisticsService.saveToFile(STATS_FILE);
            }
        } else if (gameService.getState() == GameState.LOST) {
            setStatus("YOU LOST 💥");
            stopTimer();
            revealAllMines();
            refreshBoardView();
        }
    }

    private void revealAllMines() {
        Board b = gameService.getBoard();
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                Cell cell = b.getCell(r, c);
                if (cell.isMine()) {
                    cell.reveal();
                }
            }
        }
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    // ----------------- MINE LABEL + TIMER -----------------

    private void updateMineLabel() {
        if (mineLabel != null) {
            mineLabel.setText("Mines left: " + gameService.getRemainingMines());
        }
    }

    private void setupTimer() {
        timer = new Timeline(new KeyFrame(Duration.millis(200), e -> updateTimeLabel()));
        timer.setCycleCount(Timeline.INDEFINITE);
    }

    private void startTimer() {
        if (timerRunning) return;
        timerRunning = true;
        startMillis = System.currentTimeMillis();
        lastElapsedMillis = 0;
        timer.playFromStart();
    }

    private void stopTimer() {
        if (!timerRunning) return;
        timerRunning = false;
        timer.stop();
    }

    private void resetTimer() {
        stopTimer();
        lastElapsedMillis = 0;
        if (timeLabel != null) {
            timeLabel.setText("Time: 0s");
        }
    }

    private void updateTimeLabel() {
        if (!timerRunning) return;
        long elapsed = System.currentTimeMillis() - startMillis;
        lastElapsedMillis = elapsed;
        long seconds = elapsed / 1000;
        if (timeLabel != null) {
            timeLabel.setText("Time: " + seconds + "s");
        }
    }

    // ----------------- TUTORIAL POPUP -----------------

    private void showTutorial() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("How to Play Minesweeper");

        Label title = new Label("How to Play Minesweeper");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        title.setPadding(new Insets(0, 0, 10, 0));

        Label goal = new Label(
                "🎯 Goal\n" +
                "• Do NOT click on any mine.\n" +
                "• Open all safe cells to win.\n"
        );
        goal.setWrapText(true);

        Label controls = new Label(
                "🖱 Controls\n" +
                "• Left click: open a cell.\n" +
                "• Right click: place/remove a flag (🚩) on a suspected mine.\n"
        );
        controls.setWrapText(true);

        Label numbers = new Label(
                "🔢 Numbers\n" +
                "• Each number indicates how many mines are in the 8 surrounding cells.\n" +
                "• If a cell is 0, a large empty area will automatically be opened.\n"
        );
        numbers.setWrapText(true);

        Label ai = new Label(
                "🤖 AI Move\n" +
                "• The AI will try to deduce safe cells and mines using logic.\n" +
                "• If it cannot be sure, it may guess a random cell.\n"
        );
        ai.setWrapText(true);

        Label undoRedo = new Label(
                "↩ Undo / Redo\n" +
                "• Undo: go back one move (player or AI).\n" +
                "• Redo: go forward again.\n"
        );
        undoRedo.setWrapText(true);

        Label tips = new Label(
                "💡 Tips\n" +
                "• Work from opened numbers, especially corners and edges.\n" +
                "• Use flags to avoid mis-clicking on known mines.\n" +
                "• If you're stuck, let the AI suggest a move.\n"
        );
        tips.setWrapText(true);

        VBox content = new VBox(8,
                title,
                goal,
                controls,
                numbers,
                ai,
                undoRedo,
                tips
        );
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.TOP_LEFT);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);

        Scene scene = new Scene(scroll, 420, 480);
        popup.setScene(scene);
        popup.show();
    }

    // ----------------- BEST TIMES POPUP -----------------

    private void showBestTimes() {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Best Times (Top 10)");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Best Times (Top 10 per Difficulty)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        root.getChildren().add(title);

        for (Difficulty diff : Difficulty.values()) {
            VBox section = new VBox(4);
            Label diffLabel = new Label("• " + diff.name());
            diffLabel.setStyle("-fx-font-weight: bold;");

            List<Long> times = statisticsService.getTopTimes(diff);
            if (times.isEmpty()) {
                section.getChildren().addAll(diffLabel, new Label("   (No records yet)"));
            } else {
                VBox listBox = new VBox(2);
                int rank = 1;
                for (Long t : times) {
                    double seconds = t / 1000.0;
                    Label line = new Label(String.format("   %d) %.3f s", rank++, seconds));
                    listBox.getChildren().add(line);
                }
                section.getChildren().addAll(diffLabel, listBox);
            }

            root.getChildren().add(section);
        }

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);

        Scene scene = new Scene(scroll, 400, 450);
        popup.setScene(scene);
        popup.show();
    }

    // ----------------- MAIN -----------------

    public static void main(String[] args) {
        launch(args);
    }
}
