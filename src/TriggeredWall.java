import java.awt.*;

/**
 * Spawned wall (W) that becomes solid after 'w' trigger is touched.
 */
public class TriggeredWall {
    public final int id;
    public final Rectangle rect;
    public boolean active = false;

    public TriggeredWall(int id, int col, int row, int tile){
        this.id = id;
        this.rect = new Rectangle(col*tile, row*tile, tile, tile);
    }

    public void setActive(boolean a){ active = a; }
    public void reset(){ active = false; }

    public void draw(Graphics2D g2){
        if (!active) return;
        drawWall(g2, rect);
    }

    public static void drawWall(Graphics2D g2, Rectangle r){
        g2.setColor(new Color(85,55,30));
        g2.fillRect(r.x, r.y, r.width, r.height);
        g2.setColor(new Color(70,45,25));
        g2.drawRect(r.x, r.y, r.width, r.height);
    }
}