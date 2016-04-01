package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class PrimMazeInfo {

    public boolean seen[][];
    public int costs[][];
    public int timesVisited[][];
    private int width, height;
    private int startM, startN;
    private int targetY, targetX;
    private int playerlastknownY, playerlastknownX;
    public Cell[][] maze;
    
    public int patrolcosts[][];

    // Constructor
    public PrimMazeInfo(Cell[][] maze, int startM, int startN, int targetY, int targetX) {
        this.maze = maze;
        this.startM = startM;
        this.startN = startN;
        this.targetY = targetY;
        this.targetX = targetX;
        height = maze.length;
        width = maze[0].length;
        seen = new boolean[height][width];
        costs = new int[height][width];
        timesVisited = new int [height][width];
        patrolcosts = new int[height][width];
        
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                seen[j][i] = false;
            }
        }
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                costs[j][i] = 0;
            }
        }
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                patrolcosts[j][i] = 0;
            }
        }
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                timesVisited[j][i] = 0;
            }
        }
    }
    
    public void resetSeen(){
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                seen[j][i] = false;
            }
        }
    }
    public void resetCosts(){
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                costs[j][i] = 0;
            }
        }
    }
    public void resetPatrolCosts(){
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                patrolcosts[j][i] = 0;
            }
        }
    }
    public void resetTimesVisited(){
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                timesVisited[j][i] = 0;
            }
        }
    }
    public void updateCells(){ // Update cells (costs) for A*
        
    }
    public void setStartM(int newM){
        startM = newM;
    }
    public void setStartN(int newN){
        startN = newN;
    }
    public int getWidth()
    {
        return this.width;
    }
    public int getHeight()
    {
        return this.height;
    }
    public int getStartM()
    {
        return this.startM;
    }
    public int getStartN()
    {
        return this.startN;
    }
    public int getTargetY()
    {
        return this.targetY;
    }
    public int getTargetX()
    {
        return this.targetX;
    }
    public void setPlayerLastKnownY(int y)
    {
        this.playerlastknownY = y;
    }
    public void setPlayerLastKnownX(int x)
    {
        this.playerlastknownX = x;
    }
    public int getPlayerLastKnownY()
    {
        return playerlastknownY;
    }
    public int getPlayerLastKnownX()
    {
        return playerlastknownX;
    }
}