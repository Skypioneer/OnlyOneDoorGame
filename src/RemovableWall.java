import java.awt.*;

/**
 * Removable wall (P) that is present initially and disappears when 'p' trigger is touched.
 */
public class RemovableWall {
    public final int id;
    public final Rectangle rect;
    public boolean present = true;

    public RemovableWall(int id, int col, int row, int tile){
        this.id = id;
        this.rect = new Rectangle(col*tile, row*tile, tile, tile);
    }

    public void remove(){ present = false; }
    public void reset(){ present = true; }

    public void draw(Graphics2D g2){
        if (present) TriggeredWall.drawWall(g2, rect);
    }
}