
package AgentLearning;

import java.util.Random;
import java.util.Vector;

/**
 *
 * @author b2026323 - Dave Halperin
 */
// ENEMY maze move
public class MazeMove {

    private TrackInfo movelist;
    private TrackInfo onebehindmovelist;
    private TrackInfo lineofsight; // For enemies 'seeing' a certain distance ahead (e.g. 3 Cells)
    private Cell[][] maze;
    private PrimMazeInfo mazeinfo;
    private boolean done;

    public MazeMove(Cell[][] maze, PrimMazeInfo mazeinfo) {
        System.out.println("mazemove, start m is " + mazeinfo.getStartM() + " start n is " + mazeinfo.getStartN());
        this.mazeinfo = mazeinfo;
        this.maze = maze;

        movelist = new TrackInfo();
        onebehindmovelist = new TrackInfo();
        lineofsight = new TrackInfo();
        done = false;
        movelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);  // Push start loc onto stack
        onebehindmovelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);
        mazeinfo.seen[mazeinfo.getStartM()][mazeinfo.getStartN()] = true;    // Enemy has seen this cell
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
    
    public MoveInfo[] getLineOfSight(){
        MoveInfo res[] = new MoveInfo[3];

            for (int i=0; i<=lineofsight.length; i++){
                res[i] = lineofsight.Pop();
            }
            return res; // return an array containing the line of sight coords
    }

    // Moves at random, going wherever it can that it hasn't already seen (or been on)
    // This probably needs to be a behaviour inside enemy1... or does it? Since they will all need to know about "the world" anyway, 
    // is it worth giving them all their own versions of this? Why not just make it common?
    public void MoveToCoords(int x, int y) {
        if (!done && !movelist.IsEmpty()) {
            MoveInfo cell = movelist.Pop();    // Where Enemy is currently (and what direction)
            if (cell.y == mazeinfo.getTargetM() && cell.x == mazeinfo.getTargetN()) // If at the exit
            {   
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
    public void PatrolArea(){
            this.LookAhead();
            Random randomGenerator = new Random();
            MoveInfo currentCell = movelist.Pop();
            int[] brokenWalls; // Stores ints corresponding to only the possible Walls (broken) which we will select from (i.e. ignoring unbroken ones)
            boolean lookAround = false;
            
            if (movelist.length >= 0){ // only record previous moves if the Enemy has moved at least once (not counting the start 'move')
                onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move); // Push the unchanged values
            }
            
            brokenWalls = FindBrokenWalls(currentCell.y, currentCell.x); // Find what Walls are currently broken in our Cell
            int rand = 0;
            rand = randomGenerator.nextInt(brokenWalls.length); // Select one of the possile directions we can move - this will be the INDEX we select a dir from
            int direction = brokenWalls[rand]; // Select a VALID direction using the random index we generated
            
            int move = 0;
            int xcoord = currentCell.x;
            int ycoord = currentCell.y;
            // Where can we move?
            if ((direction == MoveInfo.NORTH) && (maze[ycoord][xcoord].northwall.isBroken())) {
                --ycoord;   // Up
                move = 1;
            } else if ((direction == MoveInfo.EAST) && (maze[ycoord][xcoord].eastwall.isBroken())) {
                ++xcoord;   // Right
                move = 2;
            } else if ((direction == MoveInfo.SOUTH) && (maze[ycoord][xcoord].southwall.isBroken())) {
                ++ycoord;   // Down
                move = 3;
            } else if ((direction == MoveInfo.WEST) && (maze[ycoord][xcoord].westwall.isBroken())) {
                --xcoord;   // Left
                move = 4;
            }
            // Only push if we have moved
            if (ycoord != currentCell.y || xcoord != currentCell.x) {
                System.out.println("Moved, pushing");
                //mazeinfo.seen[ycoord][xcoord] = true;
                movelist.Push(ycoord, xcoord, move);
            }
    }
    
    public void PursuePlayer(int playerY, int playerX){
        // Head for the player
        // Potentially we might get 'stuck', so have a check to see if we haven't moved in the past iteration or something
        // otherwise just blindly rush towards the player?
        int badCellX = -1; // initial "impossible" values for a bad cell (i.e. one where we get stuck)
        int badCellY = -1;
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
        
        movelist.Push(ycoord, xcoord, move); // always have to push moves...
        
        /*if (currentCell.y == ycoord && currentCell.x == xcoord){ // If we haven't moved
            badCellY = ycoord;
            badCellX = xcoord;
            System.out.println("Haven't moved, bad cell is " + badCellX + ", " + badCellY);
            currentCell = movelist.Pop();
            xcoord = currentCell.x;
            ycoord = currentCell.y;
            move = currentCell.move;
            // Move somewhere that isn't the bad cell
            if (!((--ycoord == badCellY) && (xcoord == badCellX))){ // If we move North and it's NOT the bad cell
                --ycoord; // Ok to move North
                move = 1;
            }
            if (!((++ycoord == badCellY) && (xcoord == badCellX))){ // If we move South and it's NOT the bad cell
                ++ycoord; // Ok to move South
                move = 3;
            }
            if (!((ycoord == badCellY) && (++xcoord == badCellX))){ // If we move East and it's NOT the bad cell
                ++xcoord; // Ok to move East
                move = 2;
            }
            if (!((ycoord == badCellY) && (--xcoord == badCellX))){ // If we move West and it's NOT the bad cell
                --xcoord; // Ok to move West
                move = 4;
            }
            movelist.Push(ycoord, xcoord, move);
        }     */
    }
    
    // Implementation of the A* algorithm for pursuing the player.
    public void AStarPursuePlayer(int playerY, int playerx){
        
    }
    
    public void LookAhead(){
        // Add the next (maximum of 3 if we don't encounter any Walls) Cells ahead to the lineofsight TrackInfo
        // then remove/overwrite when we change loc/direction - so we're only looking in one direction at a time

        MoveInfo currentCell = movelist.Peek(); // Only peek because we're not actually moving here, we just need to know where we are
        // Don't need to 'pop' here - done in GetLineOfSight()
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int move = currentCell.move;
        if ((move == MoveInfo.NORTH) && (maze[ycoord][xcoord].northwall.isBroken())) { // If there is a clear path to the NORTH
            //System.out.println("Look up");
            --ycoord;   // We can look up
            lineofsight.Push(ycoord, xcoord, move); // OR instead of pushing every time, just push the final (max) coord and say 'from here to the max coord, is player in here?'
            System.out.println("LookAhead: NORTH pushed y "+ ycoord +" x "+xcoord);
            if (maze[ycoord][xcoord].northwall.isBroken()){ // Can we look 'further' up?       
                --ycoord;
                lineofsight.Push(ycoord, xcoord, move);
                System.out.println("LookAhead: NORTH inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].northwall.isBroken()){ // Once more...
                    --ycoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    System.out.println("LookAhead: NORTH inside if2, pushed y "+ ycoord +" x "+xcoord);
                }
            }
        }
        else if ((move == MoveInfo.SOUTH) && (maze[ycoord][xcoord].southwall.isBroken())) { // If there is a clear path to the SOUTH
            //System.out.println("Look down");
            ++ycoord;   // We can look up
            lineofsight.Push(ycoord, xcoord, move);
            System.out.println("LookAhead: SOUTH pushed y "+ ycoord +" x "+xcoord);
            if (maze[ycoord][xcoord].southwall.isBroken()){  
                ++ycoord;
                lineofsight.Push(ycoord, xcoord, move);
                System.out.println("LookAhead: SOUTH inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].southwall.isBroken()){
                    ++ycoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    System.out.println("LookAhead: SOUTH inside if2, pushed y "+ ycoord +" x "+xcoord);
                }
            }
        }
        else if ((move == MoveInfo.EAST) && (maze[ycoord][xcoord].eastwall.isBroken())) { // If there is a clear path to the EAST
            //System.out.println("Look right");
            ++xcoord;   // We can look right
            lineofsight.Push(ycoord, xcoord, move);
            System.out.println("LookAhead: EAST pushed y "+ ycoord +" x "+xcoord);
            if (maze[ycoord][xcoord].eastwall.isBroken()){  
                ++xcoord;
                lineofsight.Push(ycoord, xcoord, move);
                System.out.println("LookAhead: EAST inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].eastwall.isBroken()){
                    ++xcoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    System.out.println("LookAhead: EAST inside if2, pushed y "+ ycoord +" x "+xcoord);
                }
            }
        }
        else if ((move == MoveInfo.WEST) && (maze[ycoord][xcoord].westwall.isBroken())) { // If there is a clear path to the WEST
            //System.out.println("Look left");
            --xcoord;   // We can look left
            lineofsight.Push(ycoord, xcoord, move);
            System.out.println("LookAhead: WEST pushed y "+ ycoord +" x "+xcoord);
            if (maze[ycoord][xcoord].westwall.isBroken()){  
                --xcoord;
                lineofsight.Push(ycoord, xcoord, move);
                System.out.println("LookAhead: WEST inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].westwall.isBroken()){
                    --xcoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    System.out.println("LookAhead: WEST inside if2, pushed y "+ ycoord +" x "+xcoord);
                }
            }
        }
}
//        if (!(lineofsight.length == 0)){
//            for (int i=0; i<lineofsight.length; i++){
//                //System.out.println("LookAhead: lineofsight["+i+"]" + lineofsight.Peek().y + ", x " + lineofsight.Peek().x);
//                //lineofsight.Pop();
//            }
//        }
        //lineofsight.Push(ycoord, xcoord, move); // Push the furthest value we got to (1-3 Cells ahead)
    
    public void SearchArea(){
        // y, x, m and n will be calculated outside depending on Enemy's current location, i.e. search within a 1-2 block radius of their current pos
        // covering EVERY cell,or just hang around there
        MoveInfo currentCell = movelist.Pop(); // Add old pos to onebehind movelist so it gets 'cleaned' on next paint
        if (movelist.length >= 0){
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        // Add 2 to each dir of current pos, and search ALL CELLS within that area
        // Use the 'seen' array
        // If we can't move anywhere (i.e. we're surrounded either by walls or the next cell is out of range), then backtrack
    }
    
    // Find a radius that the agent can patrol
    // e.g. if they spawn in a corner or in the middle somewhere, make sure they still have a radius of size x to patrol
    public void FindRadius(){ // but this will have to return something... array of size 4, holding coords?
        MoveInfo currentCell = movelist.Peek(); // Peek because we're not actually moving
        // For simplicity, we'll say each agent patrols an area of 4x4 cells. So we need to find a total area (or radius) of size 16 cells for each agent
        // (note these can overlap)
        
        // currentCell should be the start pos for patrolling, and perhaps a smaller area for searching
        
        // Find the closest edges on two sides of either (up, left) or (down, right)
        // We want to generate an area in the opposite direction to those edges to maximize the size
        // Look up
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int northval = 0; // Keeps track of how 'big' the 4 values are - at the end, we want the biggest 2 to be the direction we make our area
        int eastval = 0;
        int southval = 0;
        int westval = 0;
        // However, if they're all the same size (e.g. if an agent spawns in the middle), just generate randomly either 1 (down and right) or 2 (up and left) by default
        if (!(maze[ycoord][xcoord].northwall.isEdge())) { // If there is a clear path (i.e. not an edge) to the NORTH
            --ycoord;   // We can look up
            northval++;
            if (!(maze[ycoord][xcoord].northwall.isEdge())){ // Can we look 'further' up?       
                --ycoord; 
                northval++;
                if (!(maze[ycoord][xcoord].northwall.isEdge())){ // Once more...
                    --ycoord; 
                    northval++;
                    if (!(maze[ycoord][xcoord].northwall.isEdge())){ // Once more...
                        --ycoord; 
                        northval++;
                    }
                }
            }
        }
        // Pick the size and direction of the patro area
        
    }
    
    // Find what Walls are broken (i.e. traversable) in our current position
    // so as to avoid having to constantly generate random numbers and potentially sit there until the timer runs out
    // because we keep generating a number that corresponds to an unbroken Wall (especially prevalent in dead-ends where there's only one way out)
    public int[] FindBrokenWalls(int ycoord, int xcoord){
        Vector<Integer> vct = new Vector<Integer>(); // Vector to store the broken Wall reference ints (variable size so easier to do this than an array whilst "growing")
        int brokenWalls[]; // Array to be copied into and returned at the end so we can access it normally elsewhere
        
        if (maze[ycoord][xcoord].northwall.isBroken()){
            vct.add(1); // Add a reference to the Wall that's broken
        }
        if (maze[ycoord][xcoord].eastwall.isBroken()){
            vct.add(2);
        }
        if (maze[ycoord][xcoord].southwall.isBroken()){
            vct.add(3);
        }
        if (maze[ycoord][xcoord].westwall.isBroken()){
            vct.add(4);
        }
        
        brokenWalls = new int[vct.size()]; // Initialize the brokenWalls array to be the size of the vector (i.e. how many Walls are broken)
        for (int i=0; i<brokenWalls.length; i++){ 
            brokenWalls[i] = vct.get(i); // Copy the elements in
        }
        
        return brokenWalls;
    }
    
    // Force enemy to be at a certain position (used in reset)
    public void ResetPosition(){
        MoveInfo currentCell = movelist.Pop(); // Add old pos to onebehind movelist so it gets 'cleaned' on next paint
        if (movelist.length >= 0){
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        System.out.println("Resetting pos to x " + mazeinfo.getStartM() + " y " + mazeinfo.getStartN());
        movelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);  // re-push start loc onto stack so it will be the next thing popped off
    }
}