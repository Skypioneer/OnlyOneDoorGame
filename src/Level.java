import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Level class that parses ASCII art level design and manages all game objects.
 *
 * Supports:
 * - Static tiles: # (wall), ^ (spike), space (empty)
 * - Dynamic objects: - (collapsing), = (platform), o (saw)
 * - Trigger system: lowercase letters trigger UPPERCASE objects with matching IDs
 * - Door system: D (initial), G (relocated position), d (trigger)
 */
public class Level {
    final char[][] grid;
    final int tile;
    int spawnX, spawnY;

    public final List<CollapsingBlock> collapsingBlocks = new ArrayList<>();
    final List<CollapsingBlock> collapsed = new ArrayList<>();
    final List<MovingPlatform> platforms = new ArrayList<>();
    final List<TriggeredPlatform> triggeredPlatforms = new ArrayList<>();
    final List<MovingSaw> saws = new ArrayList<>();
    final List<TriggeredSaw> triggeredSaws = new ArrayList<>();
    final List<TriggeredSpike> triggeredSpikes = new ArrayList<>();
    final List<TriggeredWall> triggeredWalls = new ArrayList<>();
    final List<RemovableWall> removableWalls = new ArrayList<>();
    final List<RemovableSpike> removableSpikes = new ArrayList<>();

    final Map<Integer, List<Rectangle>> trigT = new HashMap<>();
    final Map<Integer, List<Rectangle>> trigK = new HashMap<>();
    final Map<Integer, List<Rectangle>> trigU = new HashMap<>();
    final Map<Integer, List<Rectangle>> trigW = new HashMap<>();
    final Map<Integer, List<Rectangle>> trigP = new HashMap<>();
    final Map<Integer, List<Rectangle>> trigJ = new HashMap<>();
    final Map<Integer, List<Rectangle>> trigF = new HashMap<>();

    final List<Rectangle> doorD = new ArrayList<>();
    final List<Rectangle> doorG = new ArrayList<>();
    final List<Rectangle> doorTrig = new ArrayList<>();
    boolean doorRelocated = false;

    /**
     * Constructs a level from ASCII art string array.
     * @param rows Array of strings representing the level layout
     * @param tileSize Pixel size of each tile
     */
    public Level(String[] rows, int tileSize) {
        this.tile = tileSize;

        int srcH = rows.length;
        int srcW = 0;
        for (String line : rows) srcW = Math.max(srcW, countTiles(line));

        int h = srcH;
        int w = srcW;

        grid = new char[h][w];
        for (int r=0; r<h; r++) Arrays.fill(grid[r], ' ');

        // Parse level string
        for (int r=0; r<srcH; r++){
            String line = rows[r];
            int colT = 0;
            for (int i=0; i<line.length() && colT<w;) {
                char ch = line.charAt(i);
                if (isIdHead(ch)) {
                    int[] parsed = parseId(line, i+1);
                    int id = parsed[0]; i = parsed[1];
                    switch (ch) {
                        case 't': addTrigger(trigT, id, colT, r); break;
                        case 'k': addTrigger(trigK, id, colT, r); break;
                        case 'u': addTrigger(trigU, id, colT, r); break;
                        case 'w': addTrigger(trigW, id, colT, r); break;
                        case 'p': addTrigger(trigP, id, colT, r); break;
                        case 'j': addTrigger(trigJ, id, colT, r); break;
                        case 'f': addTrigger(trigF, id, colT, r); break;
                        case 'T': triggeredSaws.add(new TriggeredSaw(id, TriggeredSaw.Axis.H, colT, r, tile)); break;
                        case 'K': triggeredSaws.add(new TriggeredSaw(id, TriggeredSaw.Axis.V, colT, r, tile)); break;
                        case 'U': triggeredSpikes.add(new TriggeredSpike(id, colT, r, tile)); break;
                        case 'W': triggeredWalls.add(new TriggeredWall(id, colT, r, tile)); break;
                        case 'P': removableWalls.add(new RemovableWall(id, colT, r, tile)); break;
                        case 'J': removableSpikes.add(new RemovableSpike(id, colT, r, tile)); break;
                        case 'F': triggeredPlatforms.add(new TriggeredPlatform(id, colT, r, tile)); break;
                    }
                    colT++; continue;
                }
                switch (ch) {
                    case 'S':
                        spawnX = colT*tile + (tile-22)/2;
                        spawnY = r*tile + (tile-26)/2;
                        break;
                    case '-': collapsingBlocks.add(new CollapsingBlock(colT, r, tile)); break;
                    case '=': platforms.add(new MovingPlatform(colT, r, tile)); break;
                    case 'o': saws.add(new MovingSaw(colT, r, tile)); break;
                    case '#': case '^': grid[r][colT] = ch; break;
                    case 'D': doorD.add(new Rectangle(colT*tile, r*tile, tile, tile)); break;
                    case 'G': doorG.add(new Rectangle(colT*tile, r*tile, tile, tile)); break;
                    case 'd': doorTrig.add(new Rectangle(colT*tile, r*tile, tile, tile)); break;
                    default: ;
                }
                colT++; i++;
            }
        }

        // Seal outer border
        for (int c=0; c<w; c++){ grid[0][c] = '#'; grid[h-1][c] = '#'; }
        for (int r=0; r<h; r++){ grid[r][0] = '#'; grid[r][w-1] = '#'; }
    }

    private boolean isIdHead(char ch){
        return ch=='t'||ch=='k'||ch=='u'||ch=='w'||ch=='p'||ch=='j'||ch=='f'
                || ch=='T'||ch=='K'||ch=='U'||ch=='W'||ch=='P'||ch=='J'||ch=='F';
    }

    private int[] parseId(String s, int startIdx){
        int i=startIdx, val=0; boolean any=false;
        while(i<s.length() && Character.isDigit(s.charAt(i))){
            any=true; val=val*10+(s.charAt(i)-'0'); i++;
        }
        if(!any) val=0;
        return new int[]{val,i};
    }

    private int countTiles(String line){
        int tiles=0;
        for(int i=0; i<line.length();){
            char ch=line.charAt(i);
            if(isIdHead(ch)){
                i++;
                while(i<line.length() && Character.isDigit(line.charAt(i))) i++;
                tiles++;
            } else {
                tiles++; i++;
            }
        }
        return tiles;
    }

    private void addTrigger(Map<Integer, List<Rectangle>> map, int id, int col, int row){
        Rectangle r = new Rectangle(col*tile, row*tile, tile, tile);
        map.computeIfAbsent(id, k -> new ArrayList<>()).add(r);
    }

    /**
     * Applies all trigger logic when player collides with trigger zones.
     */
    public void applyTriggers(Rectangle player){
        if (!doorRelocated && !doorTrig.isEmpty() && intersectsAny(player, doorTrig) && !doorG.isEmpty()) {
            doorRelocated = true;
        }

        for (Map.Entry<Integer,List<Rectangle>> e : trigT.entrySet())
            if (intersectsAny(player, e.getValue())) {
                int id=e.getKey();
                for (TriggeredSaw ts:triggeredSaws)
                    if (ts.id==id && ts.axis==TriggeredSaw.Axis.H) ts.setActive(true);
            }
        for (Map.Entry<Integer,List<Rectangle>> e : trigK.entrySet())
            if (intersectsAny(player, e.getValue())) {
                int id=e.getKey();
                for (TriggeredSaw ts:triggeredSaws)
                    if (ts.id==id && ts.axis==TriggeredSaw.Axis.V) ts.setActive(true);
            }
        for (Map.Entry<Integer,List<Rectangle>> e : trigU.entrySet())
            if (intersectsAny(player, e.getValue())) {
                int id=e.getKey();
                for (TriggeredSpike sp:triggeredSpikes) if (sp.id==id) sp.setActive(true);
            }
        for (Map.Entry<Integer,List<Rectangle>> e : trigW.entrySet())
            if (intersectsAny(player, e.getValue())) {
                int id=e.getKey();
                for (TriggeredWall w:triggeredWalls) if (w.id==id) w.setActive(true);
            }
        for (Map.Entry<Integer,List<Rectangle>> e : trigP.entrySet())
            if (intersectsAny(player, e.getValue())) {
                int id=e.getKey();
                for (RemovableWall w:removableWalls) if (w.id==id) w.remove();
            }
        for (Map.Entry<Integer,List<Rectangle>> e : trigJ.entrySet())
            if (intersectsAny(player, e.getValue())) {
                int id=e.getKey();
                for (RemovableSpike s:removableSpikes) if (s.id==id) s.remove();
            }
        for (Map.Entry<Integer,List<Rectangle>> e : trigF.entrySet())
            if (intersectsAny(player, e.getValue())) {
                int id=e.getKey();
                for (TriggeredPlatform tp:triggeredPlatforms) if (tp.id==id) tp.setActive(true);
            }
    }

    private boolean intersectsAny(Rectangle a, List<Rectangle> rects){
        for (Rectangle r:rects) if (a.intersects(r)) return true;
        return false;
    }

    List<Rectangle> getRects(char tileType){
        List<Rectangle> list = new ArrayList<>();
        for (int r=0; r<grid.length; r++)
            for (int c=0; c<grid[r].length; c++)
                if (grid[r][c]==tileType)
                    list.add(new Rectangle(c*tile, r*tile, tile, tile));
        return list;
    }

    public List<Rectangle> getActiveDoorRects(){
        if (doorRelocated && !doorG.isEmpty()) return doorG;
        return doorD;
    }

    public List<Rectangle> getHazardRects(){
        List<Rectangle> list = getRects('^');
        for (TriggeredSpike sp : triggeredSpikes) if (sp.active) list.add(sp.rect);
        for (RemovableSpike sp : removableSpikes) if (sp.present) list.add(sp.rect);
        for (MovingSaw s : saws) list.add(s.rect);
        for (TriggeredSaw s : triggeredSaws) if (s.active) list.add(s.rect);
        return list;
    }

    List<Rectangle> getActiveCollapsers(){
        List<Rectangle> list = new ArrayList<>();
        for (CollapsingBlock b: collapsingBlocks){
            Rectangle solid = b.currentSolidRect();
            if (solid!=null && !collapsed.contains(b)) list.add(solid);
        }
        return list;
    }

    List<Rectangle> getPlatformRects(){
        List<Rectangle> list=new ArrayList<>();
        for (MovingPlatform p: platforms) list.add(p.rect);
        for (TriggeredPlatform tp: triggeredPlatforms) if (tp.active) list.add(tp.rect);
        return list;
    }

    List<Rectangle> getSolidTriggeredWalls(){
        List<Rectangle> list=new ArrayList<>();
        for (TriggeredWall w:triggeredWalls) if (w.active) list.add(w.rect);
        for (RemovableWall w:removableWalls) if (w.present) list.add(w.rect);
        return list;
    }

    public List<Rectangle> getSolidRectsWithDynamics(){
        List<Rectangle> list = getRects('#');
        list.addAll(getSolidTriggeredWalls());
        list.addAll(getActiveCollapsers());
        list.addAll(getPlatformRects());
        return list;
    }

    public void markCollapsed(CollapsingBlock b){
        if(!collapsed.contains(b)) collapsed.add(b);
    }

    public void updateDynamics(){
        List<Rectangle> walls = getRects('#');
        walls.addAll(getSolidTriggeredWalls());
        for (MovingPlatform p: platforms) p.updateWithSolids(walls);
        for (TriggeredPlatform tp: triggeredPlatforms) tp.updateWithSolids(walls);
        for (MovingSaw s: saws) s.updateWithSolids(walls);
        for (TriggeredSaw s: triggeredSaws) s.updateWithSolids(walls);
    }

    public MovingPlatform platformAtY(Rectangle groundHit){
        for (MovingPlatform p: platforms) if (groundHit==p.rect) return p;
        return null;
    }

    public TriggeredPlatform triggeredPlatformAtY(Rectangle groundHit){
        for (TriggeredPlatform p: triggeredPlatforms) if (p.active && groundHit==p.rect) return p;
        return null;
    }

    public void resetDynamics(){
        collapsed.clear();
        for (CollapsingBlock c:collapsingBlocks) c.reset();
        for (TriggeredSaw s:triggeredSaws) s.reset();
        for (TriggeredSpike s:triggeredSpikes) s.reset();
        for (TriggeredWall w:triggeredWalls) w.reset();
        for (RemovableWall w:removableWalls) w.reset();
        for (RemovableSpike s:removableSpikes) s.reset();
        for (TriggeredPlatform tp:triggeredPlatforms) tp.reset();
        doorRelocated = false;
    }

    public int pixelWidth()  { return grid[0].length * tile; }
    public int pixelHeight() { return grid.length * tile; }

    public void draw(Graphics2D g2){
        for (int r=0; r<grid.length; r++){
            for (int c=0; c<grid[r].length; c++){
                char ch=grid[r][c]; int x=c*tile, y=r*tile;
                switch (ch){
                    case '#':
                        TriggeredWall.drawWall(g2, new Rectangle(x,y,tile,tile));
                        break;
                    case '^':
                        TriggeredSpike.drawSpike(g2, new Rectangle(x,y,tile,tile));
                        break;
                    default: ;
                }
            }
        }
        for (CollapsingBlock b: collapsingBlocks) if (!collapsed.contains(b)) b.draw(g2);
        for (TriggeredWall w: triggeredWalls) w.draw(g2);
        for (RemovableWall w: removableWalls) w.draw(g2);
        for (MovingPlatform p: platforms) p.draw(g2);
        for (TriggeredPlatform p: triggeredPlatforms) p.draw(g2);
        for (TriggeredSpike s: triggeredSpikes) s.draw(g2);
        for (RemovableSpike s: removableSpikes) s.draw(g2);
        for (MovingSaw s: saws) s.draw(g2);
        for (TriggeredSaw s: triggeredSaws) s.draw(g2);

        for (Rectangle r : getActiveDoorRects()) {
            int x=r.x, y=r.y, tile=r.width;
            g2.setColor(new Color(210,210,220));
            g2.fillRoundRect(x+6, y+4, tile-12, tile-6, 8, 8);
            g2.setColor(new Color(120,110,100));
            g2.drawRoundRect(x+6, y+4, tile-12, tile-6, 8, 8);
        }
    }
}