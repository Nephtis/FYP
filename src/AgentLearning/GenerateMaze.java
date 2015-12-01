package AgentLearning;

import java.util.Random;

/**
 *
 * @author b2026323 - Dave Halperin
 */
public class GenerateMaze {
    int randomM;    // Random start coords
    int randomN;
    int startM;
    int startN;
    int currentM;
    int currentN;
    char currentdirection;
    Cell[][] maze;
    Cell currentCell, nextCell;
    Random randomGenerator = new Random();
    int randomwallno = 0;
    WallList list = new WallList(null, null);
    Wall peekedwall;

    // Constructor
    public GenerateMaze(int m, int n) {
        // Create maze
        maze = new Cell[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                maze[i][j] = new Cell(i, j);
            }
        }
        
        // Set outer walls of maze to be edges
        for (int i = 0; i < n; i++) {  // Top row
            maze[0][i].northwall.setEdge();
        }
        for (int i = 0; i < n; i++) {  // Bottom row
            maze[m - 1][i].southwall.setEdge();
        }
        for (int i = 0; i < m; i++) {  // Leftmost column
            maze[i][0].westwall.setEdge();
        }
        for (int i = 0; i < m; i++) {  // Rightmost column
            maze[i][n - 1].eastwall.setEdge();
        }
        
        // Pick a room at random and mark it as part of the maze
        randomM = randomGenerator.nextInt(m);
        randomN = randomGenerator.nextInt(n);
        currentM = randomM;
        currentN = randomN;
        currentCell = maze[currentM][currentN];
        currentCell.setCellExplored(true);
        startM = 5;
        startN = 5;
        
        // Add the walls of the room to the wall list
        list.AddWallToList(currentCell.northwall);
        list.AddWallToList(currentCell.eastwall);
        list.AddWallToList(currentCell.southwall);
        list.AddWallToList(currentCell.westwall);
        
        // While there are walls in the list
        while (list.GetWallListCount() > 1) // 1 is 'list'
        {
            // Pick a random wall from the list and remove it from the wall list
            randomwallno = randomGenerator.nextInt(list.GetWallListCount() - 1) + 1;
            peekedwall = list.PeekWallFromList(randomwallno).GetDataItem(); // change so peekedwall is Wall? (GetDataItem directly)
            currentdirection = list.DeleteWallFromList(randomwallno);   // Returns direction (as char) of the removed wall (from list)
            Cell peekedcell = maze[currentM][currentN];
            
            // Keep picking random walls until you get one that isn't an edge and the cell hasn't been explored
            if (!(peekedwall.isEdge() && peekedcell.isExplored())) {
                // Update the current cell
                currentM = peekedwall.getRow();
                currentN = peekedwall.getCol();
                currentCell = maze[currentM][currentN];
                
                // If the neighbouring room on the opposite side of the wall isn’t in the maze yet
                switch (currentdirection) {
                    case 'N':
                        // Is the 'next cell' out of bounds?
                        if (currentM - 1 < 0) {
                            break;
                        }
                        nextCell = maze[currentM - 1][currentN];    // 1 up
                        if (nextCell.isExplored() == false) {
                            // Remove the wall from the two rooms, thus creating an open passage
                            currentCell.breakCellWall('N');
                            // Mark the neighbouring room as being part of the maze
                            currentM--;
                            currentCell = nextCell;
                            currentCell.setCellExplored(true);
                            // Add the remaining neighbouring walls of the room to the wall list
                            // Break the 'next' cell's opposite wall to the previous one broken
                            currentCell.breakCellWall('S');
                            list.AddWallToList(currentCell.northwall);
                            list.AddWallToList(currentCell.eastwall);
                            // southwall not added to list
                            list.AddWallToList(currentCell.westwall);
                        }
                        break;
                    case 'E':
                        if (currentN + 1 >= n) {
                            break;
                        }
                        nextCell = maze[currentM][currentN + 1];  // 1 right
                        if (nextCell.isExplored() == false) {
                            currentCell.breakCellWall('E');
                            currentN++;
                            currentCell = nextCell;
                            currentCell.setCellExplored(true);
                            currentCell.breakCellWall('W');
                            list.AddWallToList(currentCell.northwall);
                            list.AddWallToList(currentCell.eastwall);
                            list.AddWallToList(currentCell.southwall);
                        }
                        break;
                    case 'S':
                        if (currentM + 1 >= m) {
                            break;
                        }
                        nextCell = maze[currentM + 1][currentN];  // 1 down
                        if (nextCell.isExplored() == false) {
                            currentCell.breakCellWall('S');
                            currentM++;
                            currentCell = nextCell;
                            currentCell.setCellExplored(true);
                            currentCell.breakCellWall('N');
                            list.AddWallToList(currentCell.eastwall);
                            list.AddWallToList(currentCell.southwall);
                            list.AddWallToList(currentCell.westwall);
                        }
                        break;
                    case 'W':
                        if (currentN - 1 < 0) {
                            break;
                        }
                        nextCell = maze[currentM][currentN - 1];  // 1 left
                        if (nextCell.isExplored() == false) {
                            currentCell.breakCellWall('W');
                            currentN--;
                            currentCell = nextCell;
                            currentCell.setCellExplored(true);
                            currentCell.breakCellWall('E');
                            list.AddWallToList(currentCell.northwall);
                            list.AddWallToList(currentCell.southwall);
                            list.AddWallToList(currentCell.westwall);
                        }
                        break;
                }
            }
        }
        
        // Pick one room along the edge of the maze and mark it as the exit
        int random = randomGenerator.nextInt(4);   // 0 = top row, 1 = bottom row, 2 = leftmost col, 3 = rightmost col
        int random2;
        int targetM = 0, targetN = 0;
        do {
            if (random == 0) {
                random2 = randomGenerator.nextInt(n);    // random col on top row
                targetM = 0;
                targetN = random2;
            } else if (random == 1) {
                random2 = randomGenerator.nextInt(n);    // random col on bottom row
                targetM = m-1;
                targetN = random2;
            } else if (random == 2) {
                random2 = randomGenerator.nextInt(m);    // random row on left col
                targetM = random2;
                targetN = 0;
            } else if (random == 3) {
                random2 = randomGenerator.nextInt(m);    // random row on right col
                targetM = random2;
                targetN = n-1;
            }
        } while (targetM == startM && targetN == startN);   // Don't let the target be the start
        PrimMazeInfo mazeinfo = new PrimMazeInfo(this.maze, startM, startN, targetM, targetN);
        MazeView view = new MazeView(mazeinfo);
    }
}