import java.awt.*;

/**
 * Player character with position, velocity, and collision box.
 */
public class Player {
    float x, y, vx, vy;
    int w = 22, h = 28;
    boolean onGround = false;

    public Player(int startX, int startY){
        this.x = startX;
        this.y = startY;
    }

    public Rectangle getBounds(){
        return new Rectangle(Math.round(x), Math.round(y), w, h);
    }

    public void draw(Graphics2D g2){
        Rectangle r = getBounds();
        // Draw black body
        g2.setColor(new Color(30,30,30));
        g2.fillRect(r.x, r.y, r.width, r.height);
        // Draw white outline
        g2.setColor(Color.WHITE);
        g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
    }
}