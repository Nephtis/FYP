
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
    // Wait... do I need this? Why not just use one movelist? It will just behave differently under different circumstances
    private TrackInfo pursuemovelist; // Stores the moves to do with pursuing the player
    private TrackInfo onebehindpursuemovelist;
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
        return !movelist.IsEmpty();
    }

    // Have we found the exit? 
    // don't need this but can adapt it to some other 'done' condition?
    public boolean ExitFound() {
        return done;
    }

    public MoveInfo GetLocation() {
        return movelist.Peek();
    }
    
    public MoveInfo GetPreviousLocation(){
        return onebehindmovelist.Peek();
    }
    
    // For agent...
    public int getXCoord(){
        if (movelist.Peek() != null){
            return movelist.Peek().x;
        }
        return 0;
    }
    public int getYCoord(){
        if (movelist.Peek() != null){
            return movelist.Peek().y;
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
        if (!done && !movelist.IsEmpty())
        {
            Random randomGenerator = new Random();
            MoveInfo currentCell = movelist.Pop();
            boolean lookAround = false;
            //System.out.println("Popped " + currentCell.y + " " + currentCell.x + " " + currentCell.move); 
            //System.out.println("Stack length is: " + movelist.length);
            if (movelist.length >= 0){ // only record previous moves if the Enemy has moved at least once (not counting the start 'move')
                onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move); // Push the unchanged values
            }
            int i = 1;
            int rand = 0;
            rand = randomGenerator.nextInt((5)+1); // 0 1 2 3 4
            if (rand == 0){ // 1 in 5 chance of wanting to look around
                System.out.println("Just looking around...");
                lookAround = true; // We won't move, but we will "look around" (turning on the spot)
            }
            while (i < 5){
                // select a random direction to move/look...
                while (rand == 0){ // Because Random() includes 0 which we don't want here
                    rand = randomGenerator.nextInt((4)+1); // Keep generating until it's not 0 (i.e. 1-4)
                }
                int xcoord = currentCell.x;
                int ycoord = currentCell.y;
                int move = currentCell.move;
                // Where can we move?
                //System.out.println("rand is " + rand);               
                if ((rand == MoveInfo.NORTH) && (maze[ycoord][xcoord].northwall.isBroken())) {
                    //System.out.println("move is " + move);
                    if (!lookAround){
                        --ycoord;   // Up
                    }
                    move = 1;
                    i++;
                } else if ((rand == MoveInfo.EAST) && maze[ycoord][xcoord].eastwall.isBroken()) {
                    //System.out.println("move is " + move);
                    if (!lookAround){
                        ++xcoord;   // Right
                    }
                    move = 2;
                    i++;
                } else if ((rand == MoveInfo.SOUTH) && maze[ycoord][xcoord].southwall.isBroken()) {
                    //System.out.println("move is " + move);
                    if (!lookAround){
                        ++ycoord;   // Down
                    }
                    move = 3;
                    i++;
                } else if ((rand == MoveInfo.WEST) && maze[ycoord][xcoord].westwall.isBroken()) {
                    //System.out.println("move is " + move);
                    if (!lookAround){
                        --xcoord;   // Left
                    }
                    move = 4;
                    i++;
                }
                if (!lookAround){
                    // if we supposed to move and we haven't (e.g. we might be stuck)
                    if (currentCell.y == onebehindmovelist.Peek().y && currentCell.x == onebehindmovelist.Peek().x){
                        System.out.println("Haven't moved - might be stuck, moving to previous loc");
                        // 'undo' the previous move
                        if (currentCell.y < onebehindmovelist.Peek().y){
                            ++ycoord;
                            move = 3;
                            i++;
                        }
                        else if (currentCell.y > onebehindmovelist.Peek().y){
                            --ycoord;
                            move = 1;
                            i++;
                        }
                        else if (currentCell.x < onebehindmovelist.Peek().x){
                            ++xcoord;
                            move = 2;
                            i++;
                        }
                        else if (currentCell.x > onebehindmovelist.Peek().x){
                            --xcoord;
                            move = 4;
                            i++;
                        }
                    }
                }
                //System.out.println("Pushing " + ycoord + " " + xcoord + " " + move);                
                movelist.Push(ycoord, xcoord, move);
                //System.out.println("Stack length is: " + movelist.length);
                i=5;
            }
        }
        //System.out.println("Stack is empty!");
    }
    
    public void PursuePlayer(int playerY, int playerX){
        // Head for the player
        System.out.println("Heading for player at y: " + playerY + " x: " + playerX + "!");
        // Potentially we might get 'stuck', so have a check to see if we haven't moved for the past 3 iterations or something
        // otherwise just blindly rush towards the player?
        MoveInfo currentCell = movelist.Pop();
        if (movelist.length >= 0){
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int move = currentCell.move;
        if (currentCell.y < playerY && maze[ycoord][xcoord].southwall.isBroken()){ // We are above player and can move down (SOUTH)
            ++ycoord;
            move = 3;
        }
        else if (currentCell.y > playerY && maze[ycoord][xcoord].northwall.isBroken()){ // We are below player and can move up (NORTH)
            --ycoord;
            move = 1;
        }
        else if (currentCell.x < playerX && maze[ycoord][xcoord].eastwall.isBroken()){ // We are to the left of the left of the player and can move right (EAST)
            ++xcoord;
            move = 2;
        }
        else if (currentCell.x > playerX && maze[ycoord][xcoord].westwall.isBroken()){ // We are to the right of the player and can move left (WEST)
            --xcoord;
            move = 4;
        }
        movelist.Push(ycoord, xcoord, move);
    }
}