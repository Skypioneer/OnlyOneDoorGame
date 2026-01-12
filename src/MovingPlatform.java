import java.awt.*;
import java.util.List;

/**
 * Horizontal moving platform that bounces off walls.
 */
public class MovingPlatform {
    Rectangle rect;
    float x, y, vx = 2.0f;

    public MovingPlatform(int col, int row, int tile){
        this.x = col*tile;
        this.y = row*tile;
        this.rect = new Rectangle((int)x, (int)y, tile, tile);
    }

    public void updateWithSolids(List<Rectangle> solids){
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
        g2.setColor(new Color(80,55,30));
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(new Color(120,90,50));
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);
    }
}