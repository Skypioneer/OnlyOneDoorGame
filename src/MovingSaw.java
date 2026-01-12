import java.awt.*;
import java.util.List;

/**
 * Always-active moving saw that bounces off walls horizontally.
 */
public class MovingSaw {
    public Rectangle rect;
    public float x, y, vx = 3.0f;
    public int size;

    public MovingSaw(int col, int row, int tile){
        size = Math.round(tile*0.8f);
        this.x = col*tile + (tile - size)/2f;
        this.y = row*tile + (tile - size)/2f;
        this.rect = new Rectangle((int)x, (int)y, size, size);
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
        g2.setColor(new Color(210,210,215));
        g2.fillOval(rect.x, rect.y, rect.width, rect.height);
        g2.setColor(new Color(140,140,150));
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