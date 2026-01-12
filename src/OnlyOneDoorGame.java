import javax.swing.*;

/**
 * Only One Door — Trap Platformer
 *
 * A challenging 2D platformer where players must navigate through dangerous levels
 * filled with traps and obstacles to reach the only door that leads to victory.
 *
 * Features:
 * - Window auto-sizes to match level dimensions
 * - Complex trigger system with ID-based activation (lowercase triggers -> UPPERCASE targets)
 * - Multiple trap types: spikes, saws, collapsing blocks, moving platforms
 * - Door relocation mechanic: D (initial door) can move to G (new position) via d trigger
 * - Statistics tracking: deaths, time, completion status per level
 * - Death explosion animation with particle system
 * - 4 progressively challenging levels
 *
 * Controls:
 * - A/D or Arrow Keys: Move left/right
 * - W/Space: Jump
 * - R: Restart current level
 * - N: Skip to next level
 *
 * Trigger System:
 * - tN -> TN: Activate horizontal saw
 * - kN -> KN: Activate vertical saw
 * - uN -> UN: Spawn spikes
 * - wN -> WN: Spawn walls
 * - pN -> PN: Remove walls
 * - jN -> JN: Remove spikes
 * - fN -> FN: Activate moving platform
 */
public class OnlyOneDoorGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Only One Door — Trap Platformer");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GamePanel panel = new GamePanel(new LevelSet());
            f.setContentPane(panel);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}