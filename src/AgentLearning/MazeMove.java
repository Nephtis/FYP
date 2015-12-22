
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
    private TrackInfo senselist; // For "sensing" if player is nearby
    private Cell[][] maze;
    private PrimMazeInfo mazeinfo;
    private boolean done;
    
    //public int seenCost = 0; // test

    public MazeMove(Cell[][] maze, PrimMazeInfo mazeinfo) {
        System.out.println("mazemove, start m is " + mazeinfo.getStartM() + " start n is " + mazeinfo.getStartN());
        this.mazeinfo = mazeinfo;
        this.maze = maze;

        movelist = new TrackInfo();
        onebehindmovelist = new TrackInfo();
        lineofsight = new TrackInfo();
        senselist = new TrackInfo();
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
        return -1;
    }
    public int getYCoord(){
        if (movelist.Peek() != null){
            return movelist.Peek().y;
        }
        return -1;
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
    
    public MoveInfo[] getSenseList(){
        MoveInfo res[] = new MoveInfo[4];
        for (int i=0; i<=senselist.length; i++){
            res[i] = senselist.Pop();
        }
        return res;
    }

    // Moves at random, going wherever it can that it hasn't already seen (or been on)
    // This probably needs to be a behaviour inside enemy1... or does it? Since they will all need to know about "the world" anyway, 
    // is it worth giving them all their own versions of this? Why not just make it common?
    public void MoveToCoords(int x, int y) {
        if (!done && !movelist.IsEmpty()) {
            MoveInfo cell = movelist.Pop();    // Where Enemy is currently (and what direction)
           /* if (cell.y == mazeinfo.getTargetM() && cell.x == mazeinfo.getTargetN()) // If at the exit
            {   
                movelist.Push(cell.y, cell.x, MoveInfo.NONE);
            } else { */
                if (movelist.length >= 0){ // only record previous moves if the Enemy has moved at least once (not counting the start 'move')...why does >= seem to work?
                    onebehindmovelist.Push(cell.y, cell.x, cell.move); // Push the unchanged values
                }                  
                for (int i = 1; i < 5; i++) {
                    int move = 0;
                    int xcoord = cell.x;
                    int ycoord = cell.y;
                    if (mazeinfo.seen[ycoord][xcoord]){ // If we've been here before
                        System.out.println("Been here before");
                    }   
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
                    else {
                        System.out.println("IDK");
                    }
                    // If it's a valid location and Enemy hasn't seen it:
                    if (!(mazeinfo.maze[ycoord][xcoord].isExplored() && mazeinfo.seen[ycoord][xcoord])) {
                        mazeinfo.seen[ycoord][xcoord] = true;
                        movelist.Push(ycoord, xcoord, move);
                    } 
                    // ...
                    
                }
            }
        //}       
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
            // Only push if we have moved (which is ensured by findBrokenWalls() logic above)
            if (ycoord != currentCell.y || xcoord != currentCell.x) {
                //System.out.println("Moved, pushing");
                //mazeinfo.seen[ycoord][xcoord] = true;
                movelist.Push(ycoord, xcoord, move);
            }
    }
    
    public void PursuePlayer(int playerY, int playerX){
        // Head for the player
        // Potentially we might get 'stuck', so have a check to see if we haven't moved in the past iteration or something
        // otherwise just blindly rush towards the player?
        MoveInfo currentCell = movelist.Pop();
        if (movelist.length >= 0){
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int move = currentCell.move;
        //mazeinfo.seen[ycoord][xcoord] = true;
        // For some reason, putting "seen" stuff here causes null exceptions... or does it?
        if (currentCell.y < playerY && maze[ycoord][xcoord].southwall.isBroken() && !(mazeinfo.seen[ycoord + 1][xcoord])){ // We are above player and can move down (SOUTH)
            ++ycoord;
            move = 3;
        }
        else if (currentCell.y > playerY && maze[ycoord][xcoord].northwall.isBroken() && !(mazeinfo.seen[ycoord - 1][xcoord])){ // We are below player and can move up (NORTH)
            --ycoord;
            move = 1;
        }
        else if (currentCell.x < playerX && maze[ycoord][xcoord].eastwall.isBroken() && !(mazeinfo.seen[ycoord][xcoord + 1])){ // We are to the left of the left of the player and can move right (EAST)
            ++xcoord;
            move = 2;
        }
        else if (currentCell.x > playerX && maze[ycoord][xcoord].westwall.isBroken() && !(mazeinfo.seen[ycoord][xcoord - 1])){ // We are to the right of the player and can move left (WEST)
            --xcoord;
            move = 4;
        } 
        else { // If we haven't moved
            mazeinfo.seen[ycoord][xcoord] = true; // Treat this like a "dead end" and block it off
            System.out.println("Haven't moved, bad cell is X " + xcoord + ", Y " + ycoord);
            // Move somewhere that isn't the bad cell (BUT how do we avoid blocking ourselves/other agents off? say you CAN go to a 'blocked' cell, but try not to?)
            // have separate mazeinfos for all agents? could also keep a "main" one as well since it's still passed as a param to agents upon creation?
            // This needs refining... NEED some sort of "heuristic" here (player pos?) or else enemy will just roam randomly
            // HOWEVER we may run into the problem of none of these conditions being executed if there are too many conditions...
            // one of them HAS to be executed or this block is mostly pointless
            // Get best possible movement each time based on player pos, walls etc?
            if (maze[ycoord][xcoord].northwall.isBroken() && !(mazeinfo.seen[ycoord - 1][xcoord])) { // Remember, can't "look ahead" (e.g. ycoord + 1) to a cell that's outside an edge (doesn't exist - array out of bounds exception), so check if wall is broken FIRST inside if logic
                --ycoord;   // Up
                move = 1;
            } else if (maze[ycoord][xcoord].eastwall.isBroken() && !(mazeinfo.seen[ycoord][xcoord + 1])) {
                ++xcoord;   // Right
                move = 2;
            } else if (maze[ycoord][xcoord].southwall.isBroken() && !(mazeinfo.seen[ycoord + 1][xcoord])) {
                ++ycoord;   // Down
                move = 3;
            } else if (maze[ycoord][xcoord].westwall.isBroken() && !(mazeinfo.seen[ycoord][xcoord - 1])) {
                --xcoord;   // Left
                move = 4;
            }
            //movelist.Pop();
        }
        
        movelist.Push(ycoord, xcoord, move);
    }
    
    // DOESN'T WORK FULLY
    // Implementation of the A* algorithm for pursuing the player. Decides the best cell to move to each time.
    public void AStarPursuePlayer(int playerY, int playerX){
        // Work out the cost of moving to whatever cells we have available
        MoveInfo currentCell = movelist.Pop();
        if (movelist.length >= 0){
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int move = currentCell.move;
        int costNORTH = 0;
        int costEAST = 0;
        int costSOUTH = 0;
        int costWEST = 0;
        int tempX = xcoord;
        int tempY = ycoord;
        int[] brokenWalls = new int[3];
        int[] costs = new int[4];
        int leastCostIndex = 0;
        int dist = 0; // Distance between where we are and where player is
        
        if (mazeinfo.seen[ycoord][xcoord]){ // Only increase the cost of a seen cell ONCE...
            // and to increase cost of where agent is NOW (to check next time) instead of increasing 
            // cost of potential locs and getting stuck because you'll trap yourself with higher cost potential locs
            mazeinfo.costs[ycoord][xcoord]++;
        }
        //while (ycoord != playerY && xcoord != playerX){ // Work out the entire route each time?
            if (maze[ycoord][xcoord].northwall.isBroken()){ // If we can move North, how much does North cost?
                // If we move North, how many walls does that cell have?
                tempY--;
                dist = (tempY - playerY) + (tempX - playerX);
                if (dist < 0){ // e.g. if the distance is the "other way around" and negative, we just want the actual number of cells between us and player
                    dist = dist + (-dist * 2); // e.g. if dist is -3, we just want 3, so: -3 + (--3 * 2), -- turns into +, so this ends up as -3 + 6 = 3
                }
                costNORTH = dist; 
                //brokenWalls = FindBrokenWalls(tempY, tempX);
                //costNORTH = costNORTH + brokenWalls.length; // More walls = worse?
                if (mazeinfo.seen[tempY][xcoord]){
                    costNORTH = costNORTH + mazeinfo.costs[tempY][xcoord];
                }
                System.out.println("North cost is " + costNORTH);
                tempY = ycoord; // Reset tempY
            } else { 
                costNORTH = 100; // Assign an abnormally large cost to show we don't want to go through the wall - otherwise when we skip a wall because it's not broken, the "cost" will remain at 0 (which is "less" than the others and so "better")
            }
            costs[0] = costNORTH; // Add to costs array
            if (maze[ycoord][xcoord].eastwall.isBroken()){ // Not "else if" because we want to look at all potential directions and weigh their cost
                tempX++;
                dist = (tempX - playerX) + (tempY - playerY);
                if (dist < 0){
                    dist = dist + (-dist * 2);
                }
                costEAST = dist; 
                //brokenWalls = FindBrokenWalls(tempY, tempX);
                //costEAST = costEAST + brokenWalls.length;
                if (mazeinfo.seen[ycoord][tempX]){
                    costEAST = costEAST + mazeinfo.costs[ycoord][tempX];
                }
                System.out.println("East cost is " + costEAST);
                tempX = xcoord;
            } else { 
                costEAST = 100;
            }
            costs[1] = costEAST;
            if (maze[ycoord][xcoord].southwall.isBroken()){
                tempY++;
                dist = (tempY - playerY) + (tempX - playerX);
                if (dist < 0){
                    dist = dist + (-dist * 2);
                }
                costSOUTH = dist; 
                //brokenWalls = FindBrokenWalls(tempY, tempX);
                //costSOUTH = costSOUTH + brokenWalls.length;
                if (mazeinfo.seen[tempY][xcoord]){
                    costSOUTH = costSOUTH + mazeinfo.costs[tempY][xcoord];
                }
                System.out.println("South cost is " + costSOUTH);
                tempY = ycoord;
            } else { 
                costSOUTH = 100;
            }
            costs[2] = costSOUTH;
            if (maze[ycoord][xcoord].westwall.isBroken()){
                tempX--;
                dist = (tempX - playerX) + (tempY - playerY);
                if (dist < 0){
                    dist = dist + (-dist * 2);
                }
                costWEST = dist; 
                //brokenWalls = FindBrokenWalls(tempY, tempX);
                //costWEST = costWEST + brokenWalls.length;
                if (mazeinfo.seen[ycoord][tempX]){
                    costWEST = costWEST + mazeinfo.costs[ycoord][tempX];
                }
                System.out.println("West cost is " + costWEST);
                tempX = xcoord;
            } else { 
                costWEST = 100;
            }
            costs[3] = costWEST;
            // Now find the least cost direction (if two or more have the same cost then just pick the first one)
            int leastCost = costs[0];
            for (int i=0; i<costs.length; i++){
                if (costs[i] < leastCost){
                    System.out.println("Got inside for-if");
                    leastCost = costs[i];
                    leastCostIndex = i;
                }
            }
            // And move in that direction
            if (leastCostIndex == 0){ // North has the least cost
                System.out.println("Trying to move North");
                --ycoord;
                move = 1;
                movelist.Push(ycoord, xcoord, move);
            } else if (leastCostIndex == 1){ // East has the least cost
                System.out.println("Trying to move East");
                ++xcoord;
                move = 2;
                movelist.Push(ycoord, xcoord, move);
            } else if (leastCostIndex == 2){ // South has the least cost
                System.out.println("Trying to move South");
                ++ycoord;
                move = 3;
                movelist.Push(ycoord, xcoord, move);
            } else if (leastCostIndex == 3){ // West has the least cost
                System.out.println("Trying to move West");
                --xcoord;
                move = 4;
                movelist.Push(ycoord, xcoord, move);
            } else {
                System.out.println("ERROR"); // Just in case...
            }
            mazeinfo.seen[ycoord][xcoord] = true;
        //}
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
                //System.out.println("LookAhead: NORTH inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].northwall.isBroken()){ // Once more...
                    --ycoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    //System.out.println("LookAhead: NORTH inside if2, pushed y "+ ycoord +" x "+xcoord);
                }
            }
        }
        if ((move == MoveInfo.SOUTH) && (maze[ycoord][xcoord].southwall.isBroken())) { // If there is a clear path to the SOUTH
            //System.out.println("Look down");
            ++ycoord;   // We can look up
            lineofsight.Push(ycoord, xcoord, move);
            System.out.println("LookAhead: SOUTH pushed y "+ ycoord +" x "+xcoord);
            if (maze[ycoord][xcoord].southwall.isBroken()){  
                ++ycoord;
                lineofsight.Push(ycoord, xcoord, move);
                //System.out.println("LookAhead: SOUTH inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].southwall.isBroken()){
                    ++ycoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    //System.out.println("LookAhead: SOUTH inside if2, pushed y "+ ycoord +" x "+xcoord);
                }
            }
        }
        if ((move == MoveInfo.EAST) && (maze[ycoord][xcoord].eastwall.isBroken())) { // If there is a clear path to the EAST
            //System.out.println("Look right");
            ++xcoord;   // We can look right
            lineofsight.Push(ycoord, xcoord, move);
            System.out.println("LookAhead: EAST pushed y "+ ycoord +" x "+xcoord);
            if (maze[ycoord][xcoord].eastwall.isBroken()){  
                ++xcoord;
                lineofsight.Push(ycoord, xcoord, move);
                //System.out.println("LookAhead: EAST inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].eastwall.isBroken()){
                    ++xcoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    //System.out.println("LookAhead: EAST inside if2, pushed y "+ ycoord +" x "+xcoord);
                }
            }
        }
        if ((move == MoveInfo.WEST) && (maze[ycoord][xcoord].westwall.isBroken())) { // If there is a clear path to the WEST
            //System.out.println("Look left");
            --xcoord;   // We can look left
            lineofsight.Push(ycoord, xcoord, move);
            System.out.println("LookAhead: WEST pushed y "+ ycoord +" x "+xcoord);
            if (maze[ycoord][xcoord].westwall.isBroken()){  
                --xcoord;
                lineofsight.Push(ycoord, xcoord, move);
                //System.out.println("LookAhead: WEST inside if1, pushed y "+ ycoord +" x "+xcoord);
                if (maze[ycoord][xcoord].westwall.isBroken()){
                    --xcoord;
                    lineofsight.Push(ycoord, xcoord, move);
                    //System.out.println("LookAhead: WEST inside if2, pushed y "+ ycoord +" x "+xcoord);
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
    
    public void SenseNearby(){
        // Add the surrounding cells to a list which we can "sense" - i.e. detect if player is nearby (but don't trigger an alert)
        MoveInfo currentCell = movelist.Peek(); // Only peek because we're not actually moving here, we just need to know where we are
        // Don't need to 'pop' here - done in GetSenseList()
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        if (maze[ycoord][xcoord].northwall.isBroken()) { // If there is a clear path to the NORTH
            --ycoord;
            senselist.Push(ycoord, xcoord, 0); // Push that cell into our sense list
        } // And so on...
        if (maze[ycoord][xcoord].southwall.isBroken()) {
            ++ycoord;
            senselist.Push(ycoord, xcoord, 0);
        }
        if (maze[ycoord][xcoord].eastwall.isBroken()) {
            ++xcoord;
            senselist.Push(ycoord, xcoord, 0);
        }
        if (maze[ycoord][xcoord].westwall.isBroken()) {
            --xcoord;
            senselist.Push(ycoord, xcoord, 0);
        }
    }
    
    // Turn and face the player's direction
    public void FacePlayerDir(MoveInfo player){
        MoveInfo currentCell = movelist.Peek();
        if (currentCell.y < player.y){
            System.out.println("Facing SOUTH");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.SOUTH);
        }
        if (currentCell.y > player.y){
            System.out.println("Facing NORTH");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.NORTH);
        }
        if (currentCell.x < player.x){
            System.out.println("Facing EAST");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.EAST);
        }
        if (currentCell.x > player.x){
            System.out.println("Facing WEST");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.WEST);
        }
    }
    
    // Turn and face a direction
    public void FaceDirection(int dir){
        MoveInfo currentCell = movelist.Peek();
        movelist.Push(currentCell.y, currentCell.x, dir);
    }
    
    // NOT IMPLEMENTED
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