package AgentLearning;

import java.util.Random;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class PlayerMazeMove {

    private TrackInfo playermovelist;
    private TrackInfo onebehindplayermovelist;
    private Cell[][] maze;
    private PrimMazeInfo mazeinfo;
    private boolean done;
    Random randomGenerator = new Random();
    int playerstarty, playerstartx;

    public PlayerMazeMove(Cell[][] maze, PrimMazeInfo mazeinfo) {
        this.mazeinfo = mazeinfo;
        this.maze = maze;
        do {
            playerstarty = randomGenerator.nextInt(mazeinfo.getHeight());
            playerstartx = randomGenerator.nextInt(mazeinfo.getWidth());
        } while (playerstarty == mazeinfo.getStartM() && playerstartx == mazeinfo.getStartN());
        playermovelist = new TrackInfo();
        onebehindplayermovelist = new TrackInfo();
        done = false;
        playermovelist.Push(playerstarty, playerstartx, MoveInfo.NONE);  // Push start loc onto stack
        onebehindplayermovelist.Push(playerstarty, playerstartx, MoveInfo.NONE);
    }

    public boolean HasMoreMoves() {
        return !playermovelist.IsEmpty();
    }

    public boolean CaughtPrisoner() {
        return done;
    }
    
    public MoveInfo GetPreviousLocation(){
        return onebehindplayermovelist.Peek();
    }

    public MoveInfo GetLocation() {
        return playermovelist.Peek();
    }

    public void setDone() {
        done = true;
    }

    public void Move(int move) {
        if (!done && !playermovelist.IsEmpty()) {
            MoveInfo playercell = playermovelist.Pop();
            int xcoord = playercell.x;
            int ycoord = playercell.y;
            if (!(playermovelist.length > 1)){ // only record previous moves if the player has moved at least once (not counting the start 'move')
                onebehindplayermovelist.Push(ycoord, xcoord, move); // Push the unchanged values
            }
            // Then update
            if ((move == 1) && (maze[playercell.y][playercell.x].northwall.isBroken())) {
                --ycoord;   // Up
            } else if ((move == 2) && (maze[playercell.y][playercell.x].eastwall.isBroken())) {
                ++xcoord;   // Right
            } else if ((move == 3) && (maze[playercell.y][playercell.x].southwall.isBroken())) {
                ++ycoord;   // Down
            } else if ((move == 4) && (maze[playercell.y][playercell.x].westwall.isBroken())) {
                --xcoord;   // Left
            }
            // And push the current values
            playermovelist.Push(ycoord, xcoord, move);
            }            
    }
}