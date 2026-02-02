# Only One Door – Trap Platformer

A challenging 2D Java platformer where players navigate through dangerous levels filled with deadly traps and obstacles to reach the only door that leads to victory.

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-blue)

## 🎮 Game Overview

"Only One Door" is a precision platformer featuring intricate level design, a sophisticated trigger system, and progressively challenging obstacles. Each level contains a single door that serves as the exit, but reaching it requires careful navigation through collapsing blocks, moving saws, triggered spikes, and relocating doors.

## ✨ Key Features

### Core Gameplay
- **4 Progressive Levels**: Increasingly complex challenges with unique obstacle combinations
- **Dynamic Window Sizing**: Game window automatically adjusts to match level dimensions
- **Death & Respawn System**: Instant respawn with particle explosion effects on death
- **Statistics Tracking**: Deaths, completion time, and success status per level

### Advanced Mechanics
- **Trigger System**: Lowercase letters activate corresponding uppercase objects with matching IDs
- **Door Relocation**: Doors can move from initial position (D) to alternate location (G) via trigger (d)
- **Platform Riding**: Player velocity inherits from moving platforms
- **Collapsing Blocks**: Shake briefly then fall away when stepped on

### Obstacle Types
- **Static Hazards**: Spikes (^), walls (#)
- **Moving Hazards**: Horizontal/vertical saws, moving platforms
- **Triggered Elements**: Spawned walls, spikes, platforms, and saws
- **Removable Objects**: Walls and spikes that disappear when triggered
- **Collapsing Blocks**: Time-delayed falling platforms

## 🎯 Controls

| Key | Action |
|-----|--------|
| `A` / `←` | Move left |
| `D` / `→` | Move right |
| `W` / `Space` | Jump |
| `R` | Restart current level |
| `N` | Skip to next level |

## 🔧 Trigger System

The game uses a sophisticated ID-based trigger system where lowercase letters activate uppercase objects:

| Trigger | Target | Effect |
|---------|--------|--------|
| `tN` | `TN` | Activate horizontal moving saw |
| `kN` | `KN` | Activate vertical moving saw |
| `uN` | `UN` | Spawn spike trap |
| `wN` | `WN` | Spawn solid wall |
| `pN` | `PN` | Remove wall |
| `jN` | `JN` | Remove spike |
| `fN` | `FN` | Activate moving platform |
| `d` | `D→G` | Relocate door from D to G position |

*N represents any numeric ID (e.g., t1→T1, u5→U5)*

## 🏗️ Level Design Format

Levels are created using ASCII art with the following symbols:
```
S    - Player spawn point
#    - Solid wall
^    - Static spike
-    - Collapsing block
=    - Moving platform (always active)
o    - Moving saw (always active)
D    - Initial door position
G    - Alternate door position
d    - Door relocation trigger
[letter][ID] - Trigger or triggered object
```

### Example Level Snippet
```
#############################
#  S              tN TN    #
#  ##    uN       ###      #
#  UN    ###      D        #
#############################
```

## 📁 Project Structure
```
OnlyOneDoorGame/
├── OnlyOneDoorGame.java      # Main entry point
├── GamePanel.java            # Game loop, rendering, input handling
├── Player.java               # Player physics and collision
├── Level.java                # Level parser and object manager
├── LevelSet.java             # Container for all 4 levels
├── CollapsingBlock.java      # Time-delayed falling blocks
├── MovingPlatform.java       # Horizontal bouncing platforms
├── MovingSaw.java            # Always-active moving hazards
├── TriggeredSaw.java         # Trigger-activated saws (H/V)
├── TriggeredSpike.java       # Trigger-spawned spikes
├── TriggeredWall.java        # Trigger-spawned walls
├── TriggeredPlatform.java    # Trigger-activated platforms
├── RemovableWall.java        # Walls removed by triggers
└── RemovableSpike.java       # Spikes removed by triggers
```

## 🚀 How to Run

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Java Swing (included in standard JDK)

### Compilation
```bash
javac *.java
```

### Execution
```bash
java OnlyOneDoorGame
```

## 🎨 Technical Implementation

### Physics Engine
- **Gravity**: 0.65 pixels/frame²
- **Jump Velocity**: -14 pixels/frame
- **Ground Friction**: 0.75 multiplier
- **Air Control**: 0.88 multiplier
- **Move Speed**: 6.5 pixels/frame

### Collision Detection
- **AABB (Axis-Aligned Bounding Box)** collision system
- **Sweep-and-prune** approach for efficient detection
- **Corner correction** to prevent wall climbing
- **Platform snap** detection (2-pixel tolerance)

### Rendering
- **60 FPS** target via `Timer` with 16ms delay
- **Double buffering** through Swing's built-in support
- **Custom Graphics2D** rendering for all game objects
- **Particle system** for death explosions

## 🐛 Known Issues & Fixes

### Fixed in Current Version
- ✅ **Triggered saw direction bug**: Saws now properly reset velocity on level restart (previously retained last direction)

### Potential Improvements
- Add sound effects and background music
- Implement level editor
- Add checkpoint system for longer levels
- Include speedrun timer display
- Add more obstacle variety

## 📊 Game Statistics

The game tracks:
- **Deaths per level**: Cumulative death count
- **Completion time**: Time taken to complete each level
- **Completion status**: Visual indicators (✓/✗) for completed levels

## 🎓 Educational Value

This project demonstrates:
- **Object-Oriented Design**: Inheritance, encapsulation, polymorphism
- **Game Loop Architecture**: Fixed timestep update/render cycle
- **Collision Detection**: AABB intersection and response
- **State Management**: Level state, trigger activation, object lifecycle
- **Event-Driven Programming**: Keyboard input handling
- **2D Graphics**: Custom rendering with Java Graphics2D
- **Data Structures**: Lists, Maps for efficient object management

## 📝 Code Highlights

### Intelligent Level Parsing
Supports compact ID notation (e.g., `t1`, `U5`) for cleaner ASCII art level design.

### Flexible Trigger System
HashMap-based trigger management allows unlimited trigger-object pairs with O(1) lookup.

### Platform Inheritance
Player velocity inherits from moving platforms for smooth riding mechanics.

### Collision Optimization
Separate collision checks for walls, hazards, and platforms reduce computational overhead.

## 🤝 Contributing

This is a student project, but suggestions for improvements are welcome! Consider:
- Additional obstacle types
- New level designs
- Performance optimizations
- Code refactoring suggestions

## 📜 License

Academic/Educational use - Part of graduate coursework at Northeastern University

## 👨‍💻 Author

Created as part of Object-Oriented Programming coursework (INFO 6205)

---

**Note**: This game is designed to be challenging! Multiple attempts per level are expected and tracked as part of the gameplay experience.
