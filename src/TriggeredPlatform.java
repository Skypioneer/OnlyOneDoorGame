import java.awt.*;
import java.util.List;

/**
 * Triggered horizontal moving platform (F) that appears after 'f' trigger is touched.
 */
public class TriggeredPlatform {
    public final int id;
    public Rectangle rect;
    public float x, y, vx = 2.0f;
    public boolean active = false;
    private final float startX, startY, startVx;   // remembered for reset()

    public TriggeredPlatform(int id, int col, int row, int tile){
        this.id = id;
        this.x = col*tile;
        this.y = row*tile;
        this.rect = new Rectangle((int)x, (int)y, tile, tile);
        this.startX = x; this.startY = y; this.startVx = vx;
    }

    public void setActive(boolean a){ active = a; }
    public void reset(){
        active = false;
        x = startX; y = startY; vx = startVx;
        rect.x = (int)x; rect.y = (int)y;
    }

    public void updateWithSolids(List<Rectangle> solids){
        if (!active) return;
        float nextX = x + vx;
        Rectangle future = new Rectangle((int)nextX, (int)y, rect.width, rect.height);
        Rectangle hit = firstIntersection(future, solids);
        if (hit != null) {
            if (vx > 0) { x = hit.x - rect.width; vx = -Math.abs(vx); }
            else        { x = hit.x + hit.width;  vx =  Math.abs(vx); }
        } else x = nextX;
        rect.x = (int)x;
    }

    private Rectangle firstIntersection(Rectangle a, List<Rectangle> rects){
        for (Rectangle r : rects) if (a.intersects(r)) return r;
        return null;
    }

    public void draw(Graphics2D g2){
        if (!active) return;
        g2.setColor(new Color(80,55,30));
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(new Color(120,90,50));
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
    }
}