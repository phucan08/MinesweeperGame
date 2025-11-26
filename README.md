# MinesweeperGame
🎮 Minesweeper DSA – JavaFX + AI Solver + Undo/Redo + Ranking System
An advanced Minesweeper game built in Java 17 + JavaFX, showcasing Data Structures & Algorithms (DSA), clean architecture, and interactive GUI features.
This project includes AI-assisted gameplay, Undo/Redo system, persistent Top-10 rankings, and fully dynamic board logic.

📌 Features
✔ Classic Minesweeper Gameplay
Reveal cells, place/remove flags
Auto-expand empty regions (0-cells)

✔ Multiple Difficulties
EASY, MEDIUM, HARD, EXTREME

✔ AI Solver (Deterministic Logic)
Deduces safe cells and mines using reasoning
Falls back to a safe random guess when uncertain
Assists beginners and demonstrates logical algorithms

✔ Undo / Redo System
Uses two stacks to fully store and restore moves
Supports infinite backward/forward actions

✔ Real-Time Timer & Mine Counter
Timer starts on first reveal
Auto stops when winning or losing

✔ Persistent Top-10 Record Times
Saves/loads best times for each difficulty
Uses PriorityQueue + Serialization

✔ GUI Built with JavaFX
Clean, modern interface
Includes pop-up windows:
How to Play
Best Times (Top 10)

✔ DSA Concepts Applied
BFS (flood-fill)
Stack (Undo/Redo)
PriorityQueue (Top-10 ranking)
2D arrays (board model)
Command Pattern (Action history)

* Project Structure
minesweeper-dsa/
│
├── src/
│   ├── ui/
│   │   └── MinesweeperApp.java
│   ├── model/
│   │   ├── Board.java
│   │   ├── Cell.java
│   │   ├── Difficulty.java
│   │   └── GameState.java
│   ├── service/
│   │   ├── GameService.java
│   │   ├── StatisticsService.java
│   │   └── AISolver.java
│   ├── utils/
│       ├── BoardGenerator.java
│       └── FloodFill.java
│
├── resources/
├── best_times.dat       # auto-generated
└── README.md

🚀 Running the Project (VS Code + JavaFX)
1️⃣ Install Requirements
JDK 17
JavaFX 17 SDK
VS Code extensions: Java Extension Pack
JavaFX Support (optional)

* Configure JavaFX Path
📌 In .vscode/settings.json:
{
  "java.project.referencedLibraries": [
    "C:/Users/PC/Downloads/openjfx-17.0.17_windows-x64_bin-sdk/javafx-sdk-17.0.17/lib/*.jar"
  ]
}

📌 In .vscode/launch.json:
{
  "type": "java",
  "name": "Launch MinesweeperApp",
  "request": "launch",
  "mainClass": "ui.MinesweeperApp",
  "vmArgs": "--module-path \"C:/Users/PC/Downloads/openjfx-17.0.17_windows-x64_bin-sdk/javafx-sdk-17.0.17/lib\" --add-modules javafx.controls,javafx.graphics,javafx.fxml"
}

🧠 AI Solver Logic (Summary)
The AI evaluates each numbered cell:
If hidden neighbors = number - flagged neighbors → all hidden = mines  
If flagged neighbors = number → all hidden = safe  
Else → no deterministic move   
If no logical conclusion is possible → performs a safe random guess.

🔄 Undo/Redo System
- Powered by two stacks:
  + undoStack
  + redoStack
- Each move is stored as a GameAction, containing:
  + type of action
  + affected cells
  + previous state
  + new state
Implements Command Pattern for full state restoration.

🏆 Top-10 Ranking System
Stores best 10 times per difficulty
Uses a max-heap PriorityQueue
Saves data to best_times.dat using serialization
Auto-loads on game start

🧪 Testing Strategy
Manual testing for all difficulty levels
AI tested with ambiguous vs deterministic patterns
Undo/Redo stress-tested (50+ continuous operations)
Persistence tested across multiple program restarts
BFS flood-fill validated for edge cases

🧰 DSA Techniques Used
| Feature              | Data Structure / Algorithm |
| -------------------- | -------------------------- |
| Flood-fill expansion | BFS (Queue)                |
| Undo/Redo            | Stack (Command Pattern)    |
| Top-10 Ranking       | PriorityQueue (max-heap)   |
| Game board           | 2D array                   |
| AI Solver            | Logical inference engine   |

🏁 Conclusion
This project showcases how classical Data Structures and Algorithms can be applied to build a fully interactive, polished game application.
Combining JavaFX with solid architectural design results in a modular, scalable, and visually intuitive Minesweeper implementation.

📄 License
This project is created for educational purposes (DSA course).
You may modify or extend it freely.
