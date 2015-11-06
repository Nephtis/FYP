
package AgentLearning;

import java.util.Random;

/**
 *
 * @author b2026323 - Dave Halperin
 */
// ENEMY maze move
public class MazeMove {

    private TrackInfo movelist;
    private TrackInfo onebehindmovelist;
    private TrackInfo patrolmovelist; // Stores the moves to do with patrolling
    private TrackInfo onebehindpatrolmovelist;
    private Cell[][] maze;
    private PrimMazeInfo mazeinfo;
    private boolean done;

    public MazeMove(Cell[][] maze, PrimMazeInfo mazeinfo) {
        this.mazeinfo = mazeinfo;
        this.maze = maze;

        movelist = new TrackInfo();
        onebehindmovelist = new TrackInfo();
        done = false;
        movelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);  // Push start loc onto stack
        onebehindmovelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);
        mazeinfo.seen[mazeinfo.getStartM()][mazeinfo.getStartN()] = true;    // Enemy has seen this cell
        
        patrolmovelist = new TrackInfo();
        onebehindpatrolmovelist = new TrackInfo();
        patrolmovelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);  // Push start loc onto stack (Arbitrary North?)
        onebehindpatrolmovelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);
    }

    // Are there more moves?
    public boolean HasMoreMoves() {
        return !patrolmovelist.IsEmpty();
    }

    // Have we found the exit? 
    // don't need this but can adapt it to some other 'done' condition?
    public boolean ExitFound() {
        return done;
    }

    public MoveInfo GetLocation() {
        return patrolmovelist.Peek();
    }
    
    public MoveInfo GetPreviousLocation(){
        return onebehindpatrolmovelist.Peek();
    }
    
    // For agent...
    public int getXCoord(){
        if (patrolmovelist.Peek() != null){
            return patrolmovelist.Peek().x;
        }
        return 0;
    }
    public int getYCoord(){
        if (patrolmovelist.Peek() != null){
            return patrolmovelist.Peek().y;
        }
        return 0;
    }

    public void setDone() {
        done = true;
    }

    // Moves at random, going wherever it can that it hasn't already seen (or been on)
    // This probably needs to be a behaviour inside enemy1... or does it? Since they will all need to know about "the world" anyway, 
    // is it worth giving them all their own versions of this? Why not just make it common?
    public void SearchForExit() {
        if (!done && !movelist.IsEmpty()) {
            MoveInfo cell = movelist.Pop();    // Where Enemy is currently (and what direction)
            if (cell.y == mazeinfo.getTargetM() && cell.x == mazeinfo.getTargetN()) // If at the exit
            {   // This shouldn't ever execute, but just in case it somehow gets here...
                movelist.Push(cell.y, cell.x, MoveInfo.NONE);
            } else {
                if (movelist.length >= 0){ // only record previous moves if the Enemy has moved at least once (not counting the start 'move')...why does >= seem to work?
                    onebehindmovelist.Push(cell.y, cell.x, cell.move); // Push the unchanged values
                }                  
                    for (int i = 1; i < 5; i++) {
                        int move = 0;
                        int xcoord = cell.x;
                        int ycoord = cell.y;
                        // Where can we move?
                        if ((i == MoveInfo.NORTH) && (maze[cell.y][cell.x].northwall.isBroken())) {
                            --ycoord;   // Up
                            move = 1;
                        } else if ((i == MoveInfo.EAST) && (maze[cell.y][cell.x].eastwall.isBroken())) {
                            ++xcoord;   // Right
                            move = 2;
                        } else if ((i == MoveInfo.SOUTH) && (maze[cell.y][cell.x].southwall.isBroken())) {
                            ++ycoord;   // Down
                            move = 3;
                        } else if ((i == MoveInfo.WEST) && (maze[cell.y][cell.x].westwall.isBroken())) {
                            --xcoord;   // Left
                            move = 4;
                        }
                        // If it's a valid location and Enemy hasn't seen it:
                        if (!(mazeinfo.maze[ycoord][xcoord].isExplored() && mazeinfo.seen[ycoord][xcoord])) {
                            mazeinfo.seen[ycoord][xcoord] = true;
                            movelist.Push(ycoord, xcoord, move);
                        }
                    }
                }
            }       
    }
    
    // Move at random within a certain area
    public void PatrolArea(int x, int y, int m, int n){
        // patrol (x,y) to (m,n) - I guess this will get generated depending on what mazeinfo is (i.e. give them an in-bounds area of at least this size)
        // worth keeping a 'master' movelist? or just pass the last location from the movelist we 'came from' when the current behaviour is over?
        if (!done && !patrolmovelist.IsEmpty())
        {
            Random randomGenerator = new Random();
            MoveInfo currentCell = patrolmovelist.Pop();
            System.out.println("Popped " + currentCell.y + " " + currentCell.x + " " + currentCell.move); 
            System.out.println("Stack length is: " + patrolmovelist.length);
            if (patrolmovelist.length >= 0){ // only record previous moves if the Enemy has moved at least once (not counting the start 'move')
                onebehindpatrolmovelist.Push(currentCell.y, currentCell.x, currentCell.move); // Push the unchanged values
            }
            int i = 1;
            while (i < 5){
                // select a random direction to move...
                int rand = randomGenerator.nextInt((4)+1); // +1 so it can't be 0 - therefore it's in the range 1-4
                int xcoord = currentCell.x;
                int ycoord = currentCell.y;
                int move = currentCell.move;
                // Where can we move?
                System.out.println("rand is " + rand);
                if ((rand == MoveInfo.NORTH) && (maze[ycoord][xcoord].northwall.isBroken())) {
                    System.out.println("move is " + move);
                    --ycoord;   // Up
                    move = 1;
                    i++;
                } else if ((rand == MoveInfo.EAST) && maze[ycoord][xcoord].eastwall.isBroken()) {
                    System.out.println("move is " + move);
                    ++xcoord;   // Right
                    move = 2;
                    i++;
                } else if ((rand == MoveInfo.SOUTH) && maze[ycoord][xcoord].southwall.isBroken()) {
                    System.out.println("move is " + move);
                    ++ycoord;   // Down
                    move = 3;
                    i++;
                } else if ((rand == MoveInfo.WEST) && maze[ycoord][xcoord].westwall.isBroken()) {
                    System.out.println("move is " + move);
                    --xcoord;   // Left
                    move = 4;
                    i++;
                }
                System.out.println("Pushing " + ycoord + " " + xcoord + " " + move);                
                patrolmovelist.Push(ycoord, xcoord, move);
                System.out.println("Stack length is: " + patrolmovelist.length);
                i=5;
            }
        }
        System.out.println("Stack is empty!");
    }
}