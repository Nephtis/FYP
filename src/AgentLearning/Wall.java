package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class Wall {
    private int row;    // m
    private int col;    // n
    private char direction;
    private boolean edge; // An edge cannot be 'broken down'
    private boolean broken;   // Broken = traversable
    
    // Constructor
    public Wall(int m, int n, char dir)
    {
        edge = false;
        broken = false;
        row = m;
        col = n;
        direction = dir;
    }
    
    // Set a wall to be an edge and therefore unbreakable
    public void setEdge()
    {
        this.edge = true;
    }
    
    public boolean isEdge()
    {
        return this.edge;
    }
    
    public void breakWall()
    {
        this.broken = true;
    }
    
    public char getDirection()
    {
        return this.direction;
    }
    public boolean isBroken()
    {
        return this.broken;
    }
    public int getRow()
    {
        return this.row;
    }
    public int getCol()
    {
        return this.col;
    }
            
}