
package AgentLearning;

/**
 *
 * @author b2026323 - Dave Halperin
 */
// ENEMY maze move
public class MazeMove {

    private TrackInfo movelist;
    private TrackInfo onebehindmovelist;
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
    // This probably needs to be a behaviour inside enemy1...
    public void SearchForExit() {
        if (!done && !movelist.IsEmpty()) {
            MoveInfo cell = movelist.Pop();    // Where Enemy is currently (and what direction)
            System.out.println("Enemy: popped y " + cell.y + " x " + cell.x + " move " + cell.move + " stack length is " + movelist.length);
            if (cell.y == mazeinfo.getTargetM() && cell.x == mazeinfo.getTargetN()) // If at the exit
            {              
                movelist.Push(cell.y, cell.x, MoveInfo.NONE);
                System.out.println("Enemy: done, pushing y" + cell.y + " x " + cell.x + " move " + cell.move);
            } else {
                if ((movelist.length > 0)){ // only record previous moves if the Enemy has moved at least once (not counting the start 'move')
                    onebehindmovelist.Push(cell.y, cell.x, cell.move); // Push the unchanged values
                }
                    System.out.println("Enemy: pushing to 'onebehind' y" + cell.y + " x " + cell.x + " move " + cell.move);
                    for (int i = 1; i < 5; i++) {
                        int move = 0;
                        int xcoord = cell.x;
                        int ycoord = cell.y;
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
                            System.out.println("Enemy: valid loc and not seen, pushing y" + cell.y + " x " + cell.x + " move " + cell.move);
                        }
                    }
                }
            }
        
    }
}