import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Main game panel that handles game logic, rendering, and input.
 * Implements a 60 FPS game loop using Swing Timer.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    static final int TILE_SIZE = 32;

    // Physics constants
    static final float GRAVITY = 0.55f;
    static final float MOVE_SPEED = 2.8f;
    static final float JUMP_VY = -10.4f;
    static final float MAX_FALL = 14f;

    private javax.swing.Timer timer;
    private final LevelSet levelSet;
    private Player player;
    private int currentLevelIndex = 0;

    // Input state
    private boolean left, right, jump;

    // Statistics tracking
    private int deaths = 0;
    private int[] deathsPerLevel;
    private long[] timeMsPerLevel;
    private long levelStartTimeMs = 0L;
    private boolean levelCompleted = false;
    private char[] outcomePerLevel;

    // Window size tracking
    private boolean windowManuallyResized = false;

    // Death explosion system
    private boolean exploding = false;
    private int explosionFramesLeft = 0;
    private final java.util.List<Particle> particles = new ArrayList<>();
    private static final int EXPLOSION_DURATION_FRAMES = 36;
    private static final int PARTICLE_COUNT = 40;

    /**
     * Constructs the game panel and initializes game state.
     * @param levelSet The set of levels to play through
     */
    public GamePanel(LevelSet levelSet) {
        this.levelSet = levelSet;
        setFocusable(true);
        addKeyListener(this);
        setBackground(new Color(210, 150, 80));

        int n = levelSet.levels.size();
        deathsPerLevel = new int[n];
        timeMsPerLevel = new long[n];
        outcomePerLevel = new char[n];

        loadLevel(0);

        timer = new javax.swing.Timer(16, this);
        timer.start();

        // Add component listener to detect manual window resizing
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Level level = levelSet.levels.get(currentLevelIndex);
                if (getWidth() != level.pixelWidth() || getHeight() != level.pixelHeight()) {
                    windowManuallyResized = true;
                }
            }
        });
    }

    /**
     * Resizes the window to match the current level's pixel dimensions and centers it.
     * Only resizes if the window hasn't been manually resized by the user.
     * @param level The level to fit the window to
     */
    private void fitWindowToLevel(Level level) {
        if (windowManuallyResized) {
            return;
        }

        int w = level.pixelWidth();
        int h = level.pixelHeight();
        setPreferredSize(new Dimension(w, h));
        setMinimumSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
        revalidate();
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win instanceof JFrame) {
            JFrame frame = (JFrame) win;
            frame.pack();
            frame.setLocationRelativeTo(null);
        }
    }

    /**
     * Loads a level by index and resets all game state.
     * @param idx Level index to load
     */
    private void loadLevel(int idx) {
        currentLevelIndex = Math.max(0, Math.min(idx, levelSet.levels.size() - 1));
        Level level = levelSet.levels.get(currentLevelIndex);
        fitWindowToLevel(level);

        player = new Player(level.spawnX, level.spawnY);
        level.resetDynamics();
        levelCompleted = false;
        exploding = false;
        particles.clear();
        explosionFramesLeft = 0;

        levelStartTimeMs = System.currentTimeMillis();
        requestFocusInWindow();
        repaint();
    }

    /**
     * Game loop callback (called ~60 times per second).
     * Handles input, physics, collisions, and rendering.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Level level = levelSet.levels.get(currentLevelIndex);

        if (exploding) {
            updateParticles();
            explosionFramesLeft--;
            if (explosionFramesLeft <= 0) {
                doRespawn(level);
            }
            repaint();
            return;
        }

        // Process input
        float ax = 0;
        if (left && !right) ax = -MOVE_SPEED;
        if (right && !left) ax = MOVE_SPEED;
        player.vx = ax;

        if (jump && player.onGround) {
            player.vy = JUMP_VY;
            player.onGround = false;
        }

        // Apply gravity
        player.vy += GRAVITY;
        if (player.vy > MAX_FALL) player.vy = MAX_FALL;

        // Update triggers and dynamic objects
        level.applyTriggers(player.getBounds());
        level.updateDynamics();

        // Move player and handle collisions
        moveAndCollide(player, level);

        // Check hazards
        if (collidesWith(player.getBounds(), level.getHazardRects())) {
            startExplosion(level);
        }

        // Update collapsing blocks
        for (CollapsingBlock c : level.collapsingBlocks) {
            c.update(player, level);
        }

        // Check win condition
        if (collidesWith(player.getBounds(), level.getActiveDoorRects())) {
            levelCompleted = true;
        }
        if (levelCompleted) {
            finishCurrentLevel(false);
        }

        repaint();
    }

    /**
     * Finishes the current level and proceeds to next or shows summary.
     * @param skipped Whether the level was skipped or completed
     */
    private void finishCurrentLevel(boolean skipped) {
        long now = System.currentTimeMillis();
        timeMsPerLevel[currentLevelIndex] += (now - levelStartTimeMs);
        outcomePerLevel[currentLevelIndex] = skipped ? 'S' : 'C';

        int next = currentLevelIndex + 1;
        if (next < levelSet.levels.size()) {
            loadLevel(next);
        } else {
            showSummaryAndReset();
        }
    }

    /**
     * Displays a summary dialog with per-level and total statistics,
     * then resets the game to level 1.
     */
    private void showSummaryAndReset() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='padding: 8px;'>");
        sb.append("<div style='text-align: center; font-size: 18px; font-weight: bold; margin-bottom: 10px;'>Run Summary</div>");
        sb.append("<table cellspacing='0' cellpadding='2' style='font-family: Courier New, monospace; font-size: 14px;'>");

        int n = levelSet.levels.size();
        long totalMs = 0;
        int totalDeaths = 0;

        for (int i = 0; i < n; i++) {
            totalMs += timeMsPerLevel[i];
            totalDeaths += deathsPerLevel[i];
            double timeSec = timeMsPerLevel[i] / 1000.0;

            String avgStr;
            if (deathsPerLevel[i] == 0) {
                avgStr = String.format("%6s", "\u221E");
            } else {
                avgStr = String.format("%6.2f", timeSec / deathsPerLevel[i]);
            }

            String color = (outcomePerLevel[i] == 'C') ? "green" : "red";
            String iconChar = (outcomePerLevel[i] == 'C') ? "\u2713" : "\u2717";

            sb.append(String.format(Locale.US,
                    "<tr>" +
                            "<td><font color='%s'><b>%s</b></font></td>" +
                            "<td>&nbsp;L%d — Deaths:</td>" +
                            "<td align='right'>%3d</td>" +
                            "<td>&nbsp;&nbsp;Time:</td>" +
                            "<td align='right'>%7.2fs</td>" +
                            "<td>&nbsp;&nbsp;Avg:</td>" +
                            "<td align='right'>%s</td>" +
                            "<td>&nbsp;s/death</td>" +
                            "</tr>",
                    color, iconChar, (i + 1), deathsPerLevel[i], timeSec, avgStr));
        }

        double totalSec = totalMs / 1000.0;
        String totalAvgStr;
        if (totalDeaths == 0) {
            totalAvgStr = String.format("%6s", "\u221E");
        } else {
            totalAvgStr = String.format("%6.2f", totalSec / totalDeaths);
        }

        sb.append("<tr><td colspan='8' height='8'></td></tr>");
        sb.append(String.format(Locale.US,
                "<tr>" +
                        "<td></td>" +
                        "<td><b>Total — Deaths:</b></td>" +
                        "<td align='right'><font color='red'><b>%3d</b></font></td>" +
                        "<td>&nbsp;&nbsp;<b>Time:</b></td>" +
                        "<td align='right'><font color='blue'><b>%7.2fs</b></font></td>" +
                        "<td>&nbsp;&nbsp;<b>Avg:</b></td>" +
                        "<td align='right'><font color='purple'><b>%s</b></font></td>" +
                        "<td>&nbsp;<b>s/death</b></td>" +
                        "</tr>",
                totalDeaths, totalSec, totalAvgStr));

        sb.append("</table></body></html>");

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Results", JOptionPane.INFORMATION_MESSAGE);

        Arrays.fill(deathsPerLevel, 0);
        Arrays.fill(timeMsPerLevel, 0);
        Arrays.fill(outcomePerLevel, (char)0);
        deaths = 0;
        windowManuallyResized = false;
        loadLevel(0);
    }

    /**
     * Initiates death explosion animation with particle effects.
     * @param level Current level
     */
    private void startExplosion(Level level) {
        deaths++;
        deathsPerLevel[currentLevelIndex]++;

        exploding = true;
        explosionFramesLeft = EXPLOSION_DURATION_FRAMES;
        particles.clear();

        Rectangle pb = player.getBounds();
        float cx = pb.x + pb.width / 2f;
        float cy = pb.y + pb.height / 2f;
        Random rnd = new Random();

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            double spd = 2.0 + rnd.nextDouble() * 4.0;
            float vx = (float)(Math.cos(ang) * spd);
            float vy = (float)(Math.sin(ang) * spd) - 1.0f;
            int life = EXPLOSION_DURATION_FRAMES - rnd.nextInt(10);
            particles.add(new Particle(cx, cy, vx, vy, life));
        }
    }

    /**
     * Updates all explosion particles (position and lifetime).
     */
    private void updateParticles() {
        for (Particle p : particles) {
            if (p.life <= 0) continue;
            p.vy += 0.15f;
            p.x += p.vx;
            p.y += p.vy;
            p.life--;
        }
    }

    /**
     * Respawns the player at the level's starting position.
     * @param level Current level
     */
    private void doRespawn(Level level) {
        player.x = level.spawnX;
        player.y = level.spawnY;
        player.vx = player.vy = 0;
        player.onGround = false;
        level.resetDynamics();
        exploding = false;
        particles.clear();
        explosionFramesLeft = 0;
    }

    /**
     * Moves the player and resolves collisions with solid objects.
     * Uses separate horizontal and vertical collision checks.
     * @param p Player to move
     * @param level Current level
     */
    private void moveAndCollide(Player p, Level level) {
        List<Rectangle> solids = level.getSolidRectsWithDynamics();

        // Horizontal movement
        float newX = p.x + p.vx;
        Rectangle futureX = new Rectangle(Math.round(newX), Math.round(p.y), p.w, p.h);
        Rectangle hitX = firstIntersection(futureX, solids);
        if (hitX != null) {
            if (p.vx > 0) newX = hitX.x - p.w;
            else if (p.vx < 0) newX = hitX.x + hitX.width;
            p.vx = 0;
        }
        p.x = newX;

        // Vertical movement
        float newY = p.y + p.vy;
        Rectangle futureY = new Rectangle(Math.round(p.x), Math.round(newY), p.w, p.h);
        Rectangle hitY = firstIntersection(futureY, solids);
        if (hitY != null) {
            if (p.vy > 0) {
                newY = hitY.y - p.h;
                p.onGround = true;
                MovingPlatform touched = level.platformAtY(hitY);
                if (touched != null) p.x += touched.vx;
                TriggeredPlatform tp = level.triggeredPlatformAtY(hitY);
                if (tp != null) p.x += tp.vx;
            } else if (p.vy < 0) {
                newY = hitY.y + hitY.height;
            }
            p.vy = 0;
        } else {
            p.onGround = false;
        }
        p.y = newY;
    }

    /**
     * Checks if rectangle a intersects with any rectangle in the list.
     * @param a Rectangle to check
     * @param rects List of rectangles
     * @return true if collision found
     */
    private boolean collidesWith(Rectangle a, List<Rectangle> rects) {
        for (Rectangle r : rects) if (a.intersects(r)) return true;
        return false;
    }

    /**
     * Finds the first rectangle that intersects with rectangle a.
     * @param a Rectangle to check
     * @param rects List of rectangles
     * @return First intersecting rectangle or null
     */
    private Rectangle firstIntersection(Rectangle a, List<Rectangle> rects) {
        for (Rectangle r : rects) if (a.intersects(r)) return r;
        return null;
    }

    /**
     * Renders the game (background, level, player, particles, HUD).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Level level = levelSet.levels.get(currentLevelIndex);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(new Color(210,150,80));
        g2.fillRect(0,0,getWidth(),getHeight());

        // Calculate offset to center the level in the panel
        int levelWidth = level.pixelWidth();
        int levelHeight = level.pixelHeight();
        int offsetX = (getWidth() - levelWidth) / 2;
        int offsetY = (getHeight() - levelHeight) / 2;

        // Translate graphics context to center the level
        g2.translate(offsetX, offsetY);

        // Level and player
        level.draw(g2);

        if (!exploding) {
            player.draw(g2);
        } else {
            for (Particle p : particles) {
                if (p.life <= 0) continue;
                int alpha = Math.max(0, Math.min(255, (int)(255f * p.life / EXPLOSION_DURATION_FRAMES)));
                g2.setColor(new Color(0, 0, 0, alpha));
                int size = 4;
                g2.fillRect(Math.round(p.x - size/2f), Math.round(p.y - size/2f), size, size);
            }
        }

        // HUD (positioned relative to level area)
        g2.setColor(new Color(220, 220, 220));
        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        long elapsed = (System.currentTimeMillis() - levelStartTimeMs)/1000;
        long totalElapsed = 0;
        for (long t : timeMsPerLevel) totalElapsed += t;
        totalElapsed = totalElapsed/1000 + elapsed;

        g2.drawString("LEVEL: " + (currentLevelIndex+1) + "/" + levelSet.levels.size(), 32,22);
        g2.drawString("DEATHS: " + deathsPerLevel[currentLevelIndex] + "/" + deaths, 190,22);
        g2.drawString("TIME: " + elapsed + "/" + totalElapsed + "s", 362,22);

        g2.setColor(new Color(230, 220, 150));
        g2.drawString("N=SKIP", levelWidth - 243, 22);
        g2.drawString("R=RESTART", levelWidth - 123, 22);

        g2.dispose();
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  left = true;  break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: right = true; break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP:
            case KeyEvent.VK_SPACE:                     jump = true;  break;
            case KeyEvent.VK_R:
                timeMsPerLevel[currentLevelIndex] += (System.currentTimeMillis() - levelStartTimeMs);
                loadLevel(currentLevelIndex);
                break;
            case KeyEvent.VK_N:
                finishCurrentLevel(true);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  left = false; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: right = false; break;
            case KeyEvent.VK_W: case KeyEvent.VK_UP:
            case KeyEvent.VK_SPACE:                     jump = false; break;
        }
    }

    /**
     * Simple particle used for death explosion effect.
     */
    static class Particle {
        float x, y, vx, vy;
        int life;

        Particle(float x, float y, float vx, float vy, int life){
            this.x=x; this.y=y; this.vx=vx; this.vy=vy; this.life=life;
        }
    }
}