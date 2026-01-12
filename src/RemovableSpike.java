import java.awt.*;

/**
 * Removable spike (J) that is present initially and disappears when 'j' trigger is touched.
 */
public class RemovableSpike {
    public final int id;
    public final Rectangle rect;
    public boolean present = true;

    public RemovableSpike(int id, int col, int row, int tile){
        this.id = id;
        this.rect = new Rectangle(col*tile, row*tile, tile, tile);
    }

    public void remove(){ present = false; }
    public void reset(){ present = true; }

    public void draw(Graphics2D g2){
        if (present) TriggeredSpike.drawSpike(g2, rect);
    }
}