package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class PrimMazeInfo {

    public boolean seen[][];
    private int width, height;
    private int startM, startN;
    private int targetM, targetN;
    public Cell[][] maze;

    // Constructor
    public PrimMazeInfo(Cell[][] maze, int startM, int startN, int targetM, int targetN) {
        this.maze = maze;
        this.startM = startM;
        this.startN = startN;
        this.targetM = targetM;
        this.targetN = targetN;
        height = maze.length;
        width = maze[0].length;
        seen = new boolean[height][width];
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                seen[j][i] = false;
            }
        }
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
    public int getTargetM()
    {
        return this.targetM;
    }
    public int getTargetN()
    {
        return this.targetN;
    }
}