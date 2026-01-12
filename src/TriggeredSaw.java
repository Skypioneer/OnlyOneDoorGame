import java.awt.*;
import java.util.List;

/**
 * Triggered moving saw: supports horizontal (T) and vertical (K) axes.
 * Starts moving when player steps on corresponding trigger zone.
 *
 * BUG FIX: Now properly resets velocity on level restart to prevent
 * random direction behavior.
 */
public class TriggeredSaw {
    public enum Axis { H, V }

    public final int id;
    public final Axis axis;
    public Rectangle rect;
    public float x, y;
    public float vx = 3.2f, vy = 3.2f;
    public int size;

    final float startX, startY;
    final float startVx = 3.2f;  // Remember initial velocity
    final float startVy = 3.2f;  // Remember initial velocity
    public boolean active = false;

    public TriggeredSaw(int id, Axis axis, int col, int row, int tile){
        this.id = id;
        this.axis = axis;
        size = Math.round(tile*0.8f);
        this.x = col*tile + (tile - size)/2f;
        this.y = row*tile + (tile - size)/2f;
        this.rect = new Rectangle((int)x, (int)y, size, size);
        this.startX = x;
        this.startY = y;
    }

    public void setActive(boolean a){ active = a; }

    /**
     * Resets saw to initial state including position and velocity.
     */
    public void reset(){
        active = false;
        x = startX;
        y = startY;
        vx = startVx;  // Reset velocity to fix random direction
        vy = startVy;  // Reset velocity to fix random direction
        rect.x = (int)x;
        rect.y = (int)y;
    }

    public void updateWithSolids(List<Rectangle> solids){
        if (!active) return;

        if (axis == Axis.H) {
            float nextX = x + vx;
            Rectangle future = new Rectangle((int)nextX, (int)y, rect.width, rect.height);
            Rectangle hit = firstIntersection(future, solids);
            if (hit != null) {
                if (vx > 0) { x = hit.x - rect.width; vx = -Math.abs(vx); }
                else        { x = hit.x + hit.width;  vx =  Math.abs(vx); }
            } else x = nextX;
            rect.x = (int)x;
        } else {
            float nextY = y + vy;
            Rectangle future = new Rectangle((int)x, (int)nextY, rect.width, rect.height);
            Rectangle hit = firstIntersection(future, solids);
            if (hit != null) {
                if (vy > 0) { y = hit.y - rect.height; vy = -Math.abs(vy); }
                else        { y = hit.y + hit.height;  vy =  Math.abs(vy); }
            } else y = nextY;
            rect.y = (int)y;
        }
    }

    private Rectangle firstIntersection(Rectangle a, List<Rectangle> rects){
        for (Rectangle r : rects) if (a.intersects(r)) return r;
        return null;
    }

    public void draw(Graphics2D g2){
        if (!active) return;
        g2.setColor(new Color(220,220,230));
        g2.fillOval(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(new Color(150,150,160));
        g2.drawOval(rect.x, rect.y, rect.width, rect.height);
        for(int i=0; i<8; i++){
            double ang = i*Math.PI/4;
            int cx = rect.x+rect.width/2, cy = rect.y+rect.height/2;
            int r1 = rect.width/2, r2 = rect.width/3;
            int x1 = cx+(int)(Math.cos(ang)*r1), y1 = cy+(int)(Math.sin(ang)*r1);
            int x2 = cx+(int)(Math.cos(ang)*r2), y2 = cy+(int)(Math.sin(ang)*r2);
            g2.drawLine(x2, y2, x1, y1);
        }
    }
}