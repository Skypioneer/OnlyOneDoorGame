import java.awt.*;

/**
 * Triggered spike (U) that appears after 'u' trigger is touched.
 */
public class TriggeredSpike {
    public final int id;
    public final Rectangle rect;
    public boolean active = false;

    public TriggeredSpike(int id, int col, int row, int tile){
        this.id = id;
        this.rect = new Rectangle(col*tile, row*tile, tile, tile);
    }

    public void setActive(boolean a){ active = a; }
    public void reset(){ active = false; }

    public void draw(Graphics2D g2){
        if (!active) return;
        drawSpike(g2, rect);
    }

    /**
     * Draws a spike trap with 4 triangular spikes.
     */
    public static void drawSpike(Graphics2D g2, Rectangle r){
        int x = r.x, y = r.y, tile = r.width;
        g2.setColor(new Color(60,35,20));
        g2.fillRect(x, y+tile-6, tile, 6);
        g2.setColor(new Color(200,200,200));
        int spikes = 4;
        for(int i=0; i<spikes; i++){
            int sx = x + i*(tile/spikes);
            Polygon tri = new Polygon();
            tri.addPoint(sx, y+tile-6);
            tri.addPoint(sx+tile/spikes, y+tile-6);
            tri.addPoint(sx+tile/(2*spikes), y+8);
            g2.fillPolygon(tri);
        }
    }
}