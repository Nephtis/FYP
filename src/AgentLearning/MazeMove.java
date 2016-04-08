package AgentLearning;

import java.util.ArrayList;
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
    private TrackInfo senselist; // For "sensing" if player is nearby

    private ArrayList openCells; // For A*
    private ArrayList closedCells;

    private Cell[][] maze;
    private PrimMazeInfo mazeinfo;
    private boolean done;

    private int AStarStartY; // These will change depending on when we start doing A*
    private int AStarStartX;

    public MazeMove(Cell[][] maze, PrimMazeInfo mazeinfo) {
        System.out.println("mazemove, start m is " + mazeinfo.getStartM() + " start n is " + mazeinfo.getStartN());
        this.mazeinfo = mazeinfo;
        this.maze = maze;

        movelist = new TrackInfo();
        onebehindmovelist = new TrackInfo();
        senselist = new TrackInfo();
        openCells = new ArrayList<>();
        closedCells = new ArrayList<>();
        done = false;
        movelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);  // Push start loc onto stack
        onebehindmovelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);
        mazeinfo.seen[mazeinfo.getStartM()][mazeinfo.getStartN()] = true;    // Enemy has seen this cell

        AStarStartY = 0; // Dummy values for initialization
        AStarStartX = 0;
    }

    public void UpdateAStarStartCoords(int y, int x) {
        AStarStartY = y;
        AStarStartX = x;
    }

    public int GetAStarStartY() {
        return AStarStartY;
    }

    public int GetAStarStartX() {
        return AStarStartX;
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

    public MoveInfo GetPreviousLocation() {
        return onebehindmovelist.Peek();
    }

    // For agent...
    public int getXCoord() {
        if (movelist.Peek() != null) {
            return movelist.Peek().x;
        }
        return -1;
    }

    public int getYCoord() {
        if (movelist.Peek() != null) {
            return movelist.Peek().y;
        }
        return -1;
    }

    public void setDone() {
        done = true;
    }

    public MoveInfo[] getSenseList() {
        MoveInfo res[] = new MoveInfo[4];
        for (int i = 0; i <= senselist.length; i++) {
            res[i] = senselist.Pop();
        }
        return res;
    }

    // Move at random, but assign priority to exploring cells that haven't been visited as much as others (e.g. cover as large an area as possible)
    public void PatrolArea(PrimMazeInfo mastermazeinfo) {
        Random randomGenerator = new Random();
        MoveInfo currentCell = movelist.Pop();
        int[] brokenWalls; // Stores ints corresponding to only the possible Walls (broken) which we will select from (i.e. ignoring unbroken ones)

        if (movelist.length >= 0) { // only record previous moves if the Enemy has moved at least once (not counting the start 'move')
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move); // Push the unchanged values
        }

        brokenWalls = FindBrokenWalls(currentCell.y, currentCell.x); // Find what Walls are currently broken in our Cell
        int rand = 0;
        rand = randomGenerator.nextInt(brokenWalls.length); // Select one of the possile directions we can move - this will be the INDEX we select a dir from
        int direction = brokenWalls[rand]; // Select a VALID direction using the random index we generated

        int move = 0;
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        if (mastermazeinfo.seen[ycoord][xcoord]) {
            mastermazeinfo.patrolcosts[ycoord][xcoord]++;
        }

        if (rand < 1) { // Decides whether agent will move to a cell with less cost, or just randomly
            System.out.println("Moving via cell costs");
            int tempY = ycoord;
            int tempX = xcoord;
            int costNORTH = 0;
            int costEAST = 0;
            int costSOUTH = 0;
            int costWEST = 0;
            int costs[] = new int[4];
            if (maze[ycoord][xcoord].northwall.isBroken()) { // If we can move North, how much does North cost?
                tempY--;
                //brokenWalls = FindBrokenWalls(tempY, tempX);
                //costNORTH = costNORTH + brokenWalls.length; // More walls = worse?
                if (mazeinfo.seen[tempY][xcoord]) {
                    costNORTH = mazeinfo.costs[tempY][xcoord];
                }
                System.out.println("North cost is " + costNORTH);
                tempY = ycoord; // Reset tempY
            } else {
                costNORTH = 100;
            }
            costs[0] = costNORTH; // Add to costs array
            if (maze[ycoord][xcoord].eastwall.isBroken()) {
                tempX++;
                if (mazeinfo.seen[ycoord][tempX]) {
                    costEAST = mazeinfo.costs[ycoord][tempX];
                }
                System.out.println("East cost is " + costEAST);
                tempX = xcoord;
            } else {
                costEAST = 100;
            }
            costs[1] = costEAST;
            if (maze[ycoord][xcoord].southwall.isBroken()) {
                tempY++;
                if (mazeinfo.seen[tempY][xcoord]) {
                    costSOUTH = mazeinfo.costs[tempY][xcoord];
                }
                System.out.println("South cost is " + costSOUTH);
                tempY = ycoord;
            } else {
                costSOUTH = 100;
            }
            costs[2] = costSOUTH;
            if (maze[ycoord][xcoord].westwall.isBroken()) {
                tempX--;
                if (mazeinfo.seen[ycoord][tempX]) {
                    costWEST = mazeinfo.costs[ycoord][tempX];
                }
                System.out.println("West cost is " + costWEST);
                tempX = xcoord;
            } else {
                costWEST = 100;
            }
            costs[3] = costWEST;

            int leastCostIndex = 0;
            int leastCost = costs[0];
            for (int i = 0; i < costs.length; i++) {
                if (costs[i] < leastCost) {
                    System.out.println("Got inside for-if");
                    leastCost = costs[i];
                    leastCostIndex = i;
                }
            }
            // And move in that direction
            if (leastCostIndex == 0) { // North has the least cost
                --ycoord;
                move = 1;
                movelist.Push(ycoord, xcoord, move);
            } else if (leastCostIndex == 1) { // East has the least cost
                ++xcoord;
                move = 2;
                movelist.Push(ycoord, xcoord, move);
            } else if (leastCostIndex == 2) { // South has the least cost
                ++ycoord;
                move = 3;
                movelist.Push(ycoord, xcoord, move);
            } else if (leastCostIndex == 3) { // West has the least cost
                --xcoord;
                move = 4;
                movelist.Push(ycoord, xcoord, move);
            } else {
                System.out.println("ERROR - NO LEAST COST DETERMINED"); // Just in case...
            }
        } else {
            // Where can we move?
            System.out.println("Moving randomly");
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
            movelist.Push(ycoord, xcoord, move);
        }
        mastermazeinfo.seen[ycoord][xcoord] = true;
    }

    public void BlindlyPursuePlayer(int playerY, int playerX) {
        // Head directly for the player (works purely off of distance)
        MoveInfo currentCell = movelist.Pop();
        if (movelist.length >= 0) {
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int move = currentCell.move;
        if (currentCell.y < playerY && maze[ycoord][xcoord].southwall.isBroken()) { // We are above player and can move down (SOUTH)
            ++ycoord;
            move = 3;
        } else if (currentCell.y > playerY && maze[ycoord][xcoord].northwall.isBroken()) { // We are below player and can move up (NORTH)
            --ycoord;
            move = 1;
        } else if (currentCell.x < playerX && maze[ycoord][xcoord].eastwall.isBroken()) { // We are to the left of the left of the player and can move right (EAST)
            ++xcoord;
            move = 2;
        } else if (currentCell.x > playerX && maze[ycoord][xcoord].westwall.isBroken()) { // We are to the right of the player and can move left (WEST)
            --xcoord;
            move = 4;
        }
        movelist.Push(ycoord, xcoord, move);
    }

    // Implementation of the A* algorithm for pursuing the player. Uses heuristics to determine the best (least costly) cell to move to each time.
    public void AStarMoveToCoords(int targetY, int targetX, PrimMazeInfo mazeinfo) {
        // Work out the cost of moving to whatever cells we have available
        MoveInfo currentCell = movelist.Pop();

        if (movelist.length >= 0) {
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int move = currentCell.move;
        int costNORTH = 0; // Default costs start at 0 and get changed later
        int costEAST = 0;
        int costSOUTH = 0;
        int costWEST = 0;
        int tempX = xcoord;
        int tempY = ycoord;
        int[] brokenWalls = new int[3];
        int[] costs = new int[4];
        int leastCostIndex = 0;
        int dist = 0; // Distance between where agent is and where player is (not quite... purpose has changed a bit/is fluid)

        if (mazeinfo.seen[ycoord][xcoord]){ // Only increase the cost of a seen cell ONCE...
        // and to increase cost of where agent is NOW (to check next time) instead of increasing 
        // cost of potential locs and getting stuck because you'll trap yourself with higher cost potential locs
        mazeinfo.timesVisited[ycoord][xcoord]++;
        }
        // This needs to happen on each pursuit iteration (otherwise we'd just "teleport" to the player and catch them immediately)
        if (maze[ycoord][xcoord].northwall.isBroken()) { // If we can move North, how much does North cost?
            tempY = ycoord - 1; // Pretend we've moved there
            // Use Math.abs() because we just want the positive "distance" value, e.g. it might be -1 due to the coord but technically the distance is still 1
            dist = Math.abs(xcoord - targetX) + Math.abs(tempY - targetY); // Cost to reach this node (should always be 1 since all single cell traversals are 1 in this case...)
            int hx = Math.abs(xcoord - targetX);
            int hy = Math.abs(ycoord - targetY);
            int heuristic = hx + hy; // Use Manhattan distance as basic heuristic
            costNORTH = dist + heuristic;
            brokenWalls = FindBrokenWalls(tempY, xcoord); // How many walls does that cell have?
            if (brokenWalls.length == 1){ // If it's a dead-end (only 1 broken wall, the way out)
                System.out.println("NORTH is a dead-end...");
                costNORTH += 10; // Add a big cost, but not an infinite one because we maystill need to re-explore it
            }
            //costNORTH = costNORTH - brokenWalls.length; // More walls = worse? Uncomment this for just another heuristic, but it's not really necessary
            if (mazeinfo.seen[tempY][xcoord]) {
                    //costNORTH = costNORTH + mazeinfo.costs[tempY][xcoord];
                costNORTH += mazeinfo.timesVisited[tempY][xcoord];
            }
            System.out.println("NORTH total cost, dist: " + dist + " + heuristic: " + heuristic + " = " + costNORTH);
        } else {
            costNORTH = 100; // Assign an abnormally large cost to show we don't want to go through the wall - otherwise when we skip a wall because it's not broken, the "cost" will remain at 0 (which is "less" than the others and so "better")
        }
        costs[0] = costNORTH; // Add to costs array
        if (maze[ycoord][xcoord].eastwall.isBroken()) { // Not "else if" because we want to look at ALL potential directions and weigh their cost
            tempX = xcoord + 1;
            dist = Math.abs(tempX - targetX) + Math.abs(ycoord - targetY);
            int hx = Math.abs(xcoord - targetX);
            int hy = Math.abs(ycoord - targetY);
            int heuristic = hx + hy;
            costEAST = dist + heuristic;
            brokenWalls = FindBrokenWalls(ycoord, tempX);
            //costEAST = costEAST - brokenWalls.length;

            if (brokenWalls.length == 1){ // If it's a dead-end
                System.out.println("EAST is a dead-end...");
                costEAST += 10; // Add a big cost
            }
            if (mazeinfo.seen[ycoord][tempX]) {
                    //costEAST = costEAST + mazeinfo.costs[ycoord][tempX];
                costEAST += mazeinfo.timesVisited[ycoord][tempX];
            }
            System.out.println("EAST total cost, dist: " + dist + " + heuristic: " + heuristic + " = " + costEAST);
        } else {
            costEAST = 100;
        }
        costs[1] = costEAST;
        if (maze[ycoord][xcoord].southwall.isBroken()) {
            tempY = ycoord + 1;
            dist = Math.abs(xcoord - targetX) + Math.abs(tempY - targetY);
            int hx = Math.abs(xcoord - targetX);
            int hy = Math.abs(ycoord - targetY);
            int heuristic = hx + hy;
            costSOUTH = dist + heuristic;
            brokenWalls = FindBrokenWalls(tempY, xcoord);
            //costSOUTH = costSOUTH - brokenWalls.length;
            if (brokenWalls.length == 1){ // If it's a dead-end
                System.out.println("SOUTH is a dead-end...");
                costSOUTH += 10; // Add a big cost
            }
            if (mazeinfo.seen[tempY][xcoord]) {
                    //costSOUTH = costSOUTH + mazeinfo.costs[tempY][xcoord];

                costSOUTH += mazeinfo.timesVisited[tempY][xcoord];
            }
            System.out.println("SOUTH total cost, dist: " + dist + " + heuristic: " + heuristic + " = " + costSOUTH);
        } else {
            costSOUTH = 100;
        }
        costs[2] = costSOUTH;
        if (maze[ycoord][xcoord].westwall.isBroken()) {
            tempX = xcoord - 1;
            dist = Math.abs(tempX - targetX) + Math.abs(ycoord - targetY);
            int hx = Math.abs(xcoord - targetX);
            int hy = Math.abs(ycoord - targetY);
            int heuristic = hx + hy;
            costWEST = dist + heuristic;
            brokenWalls = FindBrokenWalls(ycoord, tempX);
            //costWEST = costWEST - brokenWalls.length;
            if (brokenWalls.length == 1){ // If it's a dead-end
                System.out.println("WEST is a dead-end...");
                costWEST += 10; // Add a big cost
            }
            if (mazeinfo.seen[ycoord][tempX]) {
                    //costWEST = costWEST + mazeinfo.costs[ycoord][tempX];
                costWEST += mazeinfo.timesVisited[ycoord][tempX];
            }
            System.out.println("WEST total cost, dist: " + dist + " + heuristic: " + heuristic + " = " + costWEST);
        } else {
            costWEST = 100;
        }
        costs[3] = costWEST;

        // Now find the least cost direction (if two or more have the same cost then just pick the first one)
        int leastCost = costs[0];
        for (int i = 0; i < costs.length; i++) { // But this will mean it ALWAYS takes the latest cost if there's a tie, which could make it get stuck
            if (costs[i] < leastCost) {
                leastCost = costs[i];
                leastCostIndex = i;
            }
        }

        // And move in that direction
        if (leastCostIndex == 0) { // North has the least cost
            --ycoord;
            move = 1;
            movelist.Push(ycoord, xcoord, move);
            mazeinfo.costs[ycoord][xcoord] = leastCost;
            mazeinfo.seen[ycoord][xcoord] = true;
        } else if (leastCostIndex == 1) { // East has the least cost
            ++xcoord;
            move = 2;
            movelist.Push(ycoord, xcoord, move);
            mazeinfo.costs[ycoord][xcoord] = leastCost;
            mazeinfo.seen[ycoord][xcoord] = true;
        } else if (leastCostIndex == 2) { // South has the least cost
            ++ycoord;
            move = 3;
            movelist.Push(ycoord, xcoord, move);
            mazeinfo.costs[ycoord][xcoord] = leastCost;
            mazeinfo.seen[ycoord][xcoord] = true;
        } else if (leastCostIndex == 3) { // West has the least cost
            --xcoord;
            move = 4;
            movelist.Push(ycoord, xcoord, move);
            mazeinfo.costs[ycoord][xcoord] = leastCost;
            mazeinfo.seen[ycoord][xcoord] = true;
        } else {
            System.out.println("ERROR - NO LEAST COST FOUND"); // Just in case...
        }
    }

    public int getTimesVisited() {
        MoveInfo currentCell = movelist.Peek();
        System.out.println("Times visited is: " + mazeinfo.timesVisited[currentCell.y][currentCell.x]);
        return mazeinfo.timesVisited[currentCell.y][currentCell.x];
    }

    // Do this if we're stuck in some part of the maze for whatever reason
    public void MoveRandomly() {
        Random randomGenerator = new Random();
        MoveInfo currentCell = movelist.Pop();
        int[] brokenWalls; // Stores ints corresponding to only the possible Walls (broken) which we will select from (i.e. ignoring unbroken ones)

        if (movelist.length >= 0) { // only record previous moves if the Enemy has moved at least once (not counting the start 'move')
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move); // Push the unchanged values
        }

        brokenWalls = FindBrokenWalls(currentCell.y, currentCell.x); // Find what Walls are currently broken in our Cell
        int rand = 0;
        rand = randomGenerator.nextInt(brokenWalls.length); // Select one of the possile directions we can move - this will be the INDEX we select a dir from
        int direction = brokenWalls[rand]; // Select a VALID direction using the random index we generated
        int xcoord = currentCell.x;
        int ycoord = currentCell.y;
        int move = 0;

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
        movelist.Push(ycoord, xcoord, move);
    }

    public void SenseNearby() {
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
    public void FacePlayerDir(MoveInfo player) {
        MoveInfo currentCell = movelist.Peek();
        if (currentCell.y < player.y) {
            System.out.println("Facing SOUTH");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.SOUTH);
        }
        if (currentCell.y > player.y) {
            System.out.println("Facing NORTH");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.NORTH);
        }
        if (currentCell.x < player.x) {
            System.out.println("Facing EAST");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.EAST);
        }
        if (currentCell.x > player.x) {
            System.out.println("Facing WEST");
            movelist.Push(currentCell.y, currentCell.x, MoveInfo.WEST);
        }
    }

    // Turn and face a direction
    public void FaceDirection(int dir) {
        MoveInfo currentCell = movelist.Peek();
        movelist.Push(currentCell.y, currentCell.x, dir);
    }

    public void SearchArea(int startX, int startY) {
        // y, x, m and n will be calculated outside depending on Enemy's current location, i.e. search within a 1-2 block radius of their current pos
        // covering EVERY cell,or just hang around there
        MoveInfo currentCell = movelist.Pop(); // Add old pos to onebehind movelist so it gets 'cleaned' on next paint
        if (movelist.length >= 0) {
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        // Add 2 to each dir of current pos, and search ALL CELLS within that area
        int ycoord = currentCell.y;
        int xcoord = currentCell.x;
        int move = currentCell.move;

        if (mazeinfo.seen[ycoord][xcoord]) {
            mazeinfo.costs[ycoord][xcoord]++;
        }

        int tempY = ycoord;
        int tempX = xcoord;
        int costNORTH = 0;
        int costEAST = 0;
        int costSOUTH = 0;
        int costWEST = 0;
        int costs[] = new int[4];
        tempY--;
        System.out.println("NORTH: tempY is " + tempY + ", startY-2 is " + (startY - 2));
        if (maze[ycoord][xcoord].northwall.isBroken() && tempY >= (startY - 2)) { // If we can move North, how much does North cost?
            if (mazeinfo.seen[tempY][xcoord]) {
                costNORTH = mazeinfo.costs[tempY][xcoord];
            }
            System.out.println("North cost is " + costNORTH);
        } else {
            costNORTH = 100;
        }
        tempY = ycoord; // Reset tempY
        costs[0] = costNORTH; // Add to costs array

        tempX++;
        System.out.println("EAST: tempX is " + tempX + ", startX+2 is " + (startX + 2));
        if (maze[ycoord][xcoord].eastwall.isBroken() && tempX <= (startX + 2)) {
            if (mazeinfo.seen[ycoord][tempX]) {
                costEAST = mazeinfo.costs[ycoord][tempX];
            }
            System.out.println("East cost is " + costEAST);
        } else {
            costEAST = 100;
        }
        costs[1] = costEAST;
        tempX = xcoord;

        tempY++;
        System.out.println("SOUTH: tempY is " + tempY + ", startY+2 is " + (startY + 2));
        if (maze[ycoord][xcoord].southwall.isBroken() && tempY <= (startY + 2)) {
            if (mazeinfo.seen[tempY][xcoord]) {
                costSOUTH = mazeinfo.costs[tempY][xcoord];
            }
            System.out.println("South cost is " + costSOUTH);
        } else {
            costSOUTH = 100;
        }
        costs[2] = costSOUTH;
        tempY = ycoord;

        tempX--;
        System.out.println("WEST: tempX is " + tempX + ", startX-2 is " + (startX - 2));
        if (maze[ycoord][xcoord].westwall.isBroken() && tempX >= (startX - 2)) {
            if (mazeinfo.seen[ycoord][tempX]) {
                costWEST = mazeinfo.costs[ycoord][tempX];
            }
            System.out.println("West cost is " + costWEST);
        } else {
            costWEST = 100;
        }
        costs[3] = costWEST;
        tempX = xcoord;

        int leastCostIndex = 0;
        int leastCost = costs[0];
        for (int i = 0; i < costs.length; i++) {
            if (costs[i] < leastCost) {
                System.out.println("Got inside for-if");
                leastCost = costs[i];
                leastCostIndex = i;
            }
        }
        // And move in that direction
        if (leastCostIndex == 0) { // North has the least cost
            System.out.println("Trying to move North");
            --ycoord;
            move = 1;
            movelist.Push(ycoord, xcoord, move);
        } else if (leastCostIndex == 1) { // East has the least cost
            System.out.println("Trying to move East");
            ++xcoord;
            move = 2;
            movelist.Push(ycoord, xcoord, move);
        } else if (leastCostIndex == 2) { // South has the least cost
            System.out.println("Trying to move South");
            ++ycoord;
            move = 3;
            movelist.Push(ycoord, xcoord, move);
        } else if (leastCostIndex == 3) { // West has the least cost
            System.out.println("Trying to move West");
            --xcoord;
            move = 4;
            movelist.Push(ycoord, xcoord, move);
        } else {
            System.out.println("ERROR"); // Just in case...
        }
        mazeinfo.seen[ycoord][xcoord] = true;
    }

    // Find what Walls are broken (i.e. traversable) in our current position
    // so as to avoid having to constantly generate random numbers and potentially sit there until the timer runs out
    // because we keep generating a number that corresponds to an unbroken Wall (especially prevalent in dead-ends where there's only one way out)
    public int[] FindBrokenWalls(int ycoord, int xcoord) {
        // Vector to store the broken Wall reference ints (variable size so 
        // easier to do this than an array whilst "growing")
        Vector<Integer> vct = new Vector<Integer>();
        // Array to be copied into and returned at the end so we can access it 
        // normally elsewhere
        int brokenWalls[];

        if (maze[ycoord][xcoord].northwall.isBroken()) {
            vct.add(1); // Add a reference to the Wall that's broken
        }
        if (maze[ycoord][xcoord].eastwall.isBroken()) {
            vct.add(2);
        }
        if (maze[ycoord][xcoord].southwall.isBroken()) {
            vct.add(3);
        }
        if (maze[ycoord][xcoord].westwall.isBroken()) {
            vct.add(4);
        }

        // Initialize the brokenWalls array to be the size of the vector 
        // (i.e. how many Walls are broken)
        brokenWalls = new int[vct.size()];
        for (int i = 0; i < brokenWalls.length; i++) {
            brokenWalls[i] = vct.get(i); // Copy the elements in
        }

        return brokenWalls;
    }

    // Is there a wall between me and the target? Target could be a player, or player's last known position.
    public boolean isTargetable(int targetY, int targetX) {
        MoveInfo currentCell = movelist.Peek();

        if (currentCell.x == targetX && currentCell.y < targetY && currentCell.move == MoveInfo.SOUTH) { // Target is directly to the South AND we're facing South
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].southwall.isBroken()) {
                ++y;
                if (y == targetY) {
                    System.out.println("Target visible to the South");
                    return true; // is targetable
                }
            }
            return false; // is not targetable          
        } else if (currentCell.x == targetX && currentCell.y > targetY && currentCell.move == MoveInfo.NORTH) { // Target is directly to the North AND we're facing North
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].northwall.isBroken()) {
                --y;
                if (y == targetY) {
                    System.out.println("Target visible to the North");
                    return true; // is targetable
                }
            }
            return false; // is not targetable
        } else if (currentCell.x > targetX && currentCell.y == targetY && currentCell.move == MoveInfo.WEST) { // Target is directly to the West AND we're facing West
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].westwall.isBroken()) {
                --x;
                if (x == targetX) {
                    System.out.println("Target visible to the West");
                    return true; // is targetable  
                }
            }
            return false; // is not targetable
        } else if (currentCell.x < targetX && currentCell.y == targetY && currentCell.move == MoveInfo.EAST) { // Target is directly to the East AND we're facing East
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].eastwall.isBroken()) {
                ++x;
                if (x == targetX) {
                    System.out.println("Target visible to the East");
                    return true; // is targetable
                }
            }
            return false; // is not targetable
        }
        return false; // is not targetable
    }
    
    public boolean isTargetableAnyDir(int targetY, int targetX) {
        MoveInfo currentCell = movelist.Peek();

        if (currentCell.x == targetX && currentCell.y < targetY) {
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].southwall.isBroken()) {
                ++y;
                if (y == targetY) {
                    System.out.println("Target visible to the South");
                    return true; // is targetable
                }
            }
            return false; // is not targetable          
        } else if (currentCell.x == targetX && currentCell.y > targetY) {
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].northwall.isBroken()) {
                --y;
                if (y == targetY) {
                    System.out.println("Target visible to the North");
                    return true; // is targetable
                }
            }
            return false; // is not targetable
        } else if (currentCell.x > targetX && currentCell.y == targetY) {
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].westwall.isBroken()) {
                --x;
                if (x == targetX) {
                    System.out.println("Target visible to the West");
                    return true; // is targetable  
                }
            }
            return false; // is not targetable
        } else if (currentCell.x < targetX && currentCell.y == targetY) {
            int y = currentCell.y;
            int x = currentCell.x;
            while (maze[y][x].eastwall.isBroken()) {
                ++x;
                if (x == targetX) {
                    System.out.println("Target visible to the East");
                    return true; // is targetable
                }
            }
            return false; // is not targetable
        }
        return false; // is not targetable
    }

    // Force enemy to be at a certain position (used in reset)
    public void ResetPosition() {
        MoveInfo currentCell = movelist.Pop(); // Add old pos to onebehind movelist so it gets 'cleaned' on next paint
        if (movelist.length >= 0) {
            onebehindmovelist.Push(currentCell.y, currentCell.x, currentCell.move);
        }
        System.out.println("Resetting pos to x " + mazeinfo.getStartM() + " y " + mazeinfo.getStartN());
        movelist.Push(mazeinfo.getStartM(), mazeinfo.getStartN(), MoveInfo.NONE);  // re-push start loc onto stack so it will be the next thing popped off
    }
}
