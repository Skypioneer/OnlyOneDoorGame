import java.awt.*;

/**
 * Collapsing block that shakes briefly then falls away when stepped on.
 */
public class CollapsingBlock {
    Rectangle rect;
    boolean triggered = false;
    int delayFrames = 18, fallFrames = 60, counter = 0;
    float fallY = 0;

    public CollapsingBlock(int col, int row, int tile){
        rect = new Rectangle(col*tile, row*tile, tile, tile);
    }

    /**
     * Updates the collapsing block state.
     * Triggers when player stands on top of the block.
     */
    public void update(Player p, Level level){
        if(!triggered){
            Rectangle playerRect = p.getBounds();

            // Check if player is standing on the block
            boolean onTop = Math.abs((p.y + p.h) - rect.y) <= 2;
            boolean xOverlap = playerRect.x < rect.x + rect.width &&
                    playerRect.x + playerRect.width > rect.x;

            if(onTop && xOverlap){
                triggered = true;
                counter = 0;
            }
        } else {
            counter++;
            if(counter > delayFrames) fallY += 3.5f;
            if(counter > delayFrames + fallFrames) level.markCollapsed(this);
        }
    }

    public void draw(Graphics2D g2){
        int dy = (triggered && counter < delayFrames) ? ((counter%4<2)?1:-1) : Math.round(fallY);
        g2.setColor(new Color(85,55,30));
        g2.fillRect(rect.x, rect.y+dy, rect.width, rect.height);
        g2.setColor(new Color(70,45,25));
        g2.drawRect(rect.x, rect.y+dy, rect.width, rect.height);
    }

    public Rectangle currentSolidRect(){
        if(!triggered) return rect;
        if(triggered && counter <= delayFrames) return rect;
        return null;
    }

    public void reset(){
        triggered = false;
        counter = 0;
        fallY = 0;
    }
}