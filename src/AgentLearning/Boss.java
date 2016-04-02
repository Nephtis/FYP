package AgentLearning;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dave
 */
public class Boss extends Agent {

    private MazeMove moves; // unique list of moves that this agent has done
    private int counter = 0;
    boolean setup = true;

    // Agent initialization  
    protected void setup() {
        System.out.println("Begin boss setup()");
        Object[] args = getArguments();  // [0] = maze, [1] = mazeinfo, [2] = maze view, [3] = player
        Cell[][] maze = (Cell[][]) args[0];
        PrimMazeInfo mazeinfo = new PrimMazeInfo(maze, 0, 0, 0, 0);//(PrimMazeInfo) args[1];
        MazeView view = (MazeView) args[2];
        // Initialize move list
        moves = new MazeMove(maze, mazeinfo);

        // Add a TickerBehaviour that does things every (0.3) seconds
        addBehaviour(new Movement(this, 300));

        System.out.println("    " + getAID().getName() + ": End of setup()");
    } // end of setup

    // Put agent clean-up operations here   
    protected void takeDown() {
        System.out.println("    " + getAID().getName() + ": Terminating.");
        doDelete();
    }

    /**
     * Inner class Movement.
     */
    private class Movement extends TickerBehaviour {

        public Movement(Agent a, long period) {
            super(a, period);
        }

        Object[] args = getArguments();  // args [0] = maze, [1] = mazeinfo, [2] = mazeview, [3] = player
        Cell[][] maze = (Cell[][]) args[0];
        PrimMazeInfo mazeinfo = new PrimMazeInfo(maze, 0, 0, 0, 0); // Unique to this agent (params are all 0 because agent doesn't need to know player's "target" coords)
        PrimMazeInfo mastermazeinfo = (PrimMazeInfo) args[1]; // Shared by all agents
        MazeView view = (MazeView) args[2];
        PlayerMazeMove player = (PlayerMazeMove) args[3];

        protected void onTick() {
            if (!(view.running)) {
                doDelete();
            }
            
            if (setup){
            //System.out.println("Times visited: " + mazeinfo.timesVisited[moves.getYCoord()][moves.getXCoord()]);
                moves.UpdateAStarStartCoords(moves.getYCoord(), moves.getXCoord());
                mastermazeinfo.setPlayerLastKnownX(player.GetLocation().x);
                mastermazeinfo.setPlayerLastKnownY(player.GetLocation().y);
                setup = false;
            }

            try {
                if (!moves.isTargetable(player.GetLocation().y, player.GetLocation().x)) { // If player is not visible to the agent                           
                    moves.AStarMoveToCoords(moves.GetAStarStartY(), moves.GetAStarStartX(), mastermazeinfo.getPlayerLastKnownY(), mastermazeinfo.getPlayerLastKnownX(), mastermazeinfo);
                    // Head for last known loc...                               
                } else { // If the player is visible, just rush towards them and update their last known coords
                    mastermazeinfo.setPlayerLastKnownX(player.GetLocation().x); // Shared by all agents - let everyone know the new coords
                    mastermazeinfo.setPlayerLastKnownY(player.GetLocation().y);
                    moves.ResetClosedCells(); // Our old calculations will now be irrelevant, so reset them
                    moves.ResetOpenCells();
                    moves.UpdateAStarStartCoords(moves.getYCoord(), moves.getXCoord()); // Update the coords for this A* chase in case we lose the player
                    moves.BlindlyPursuePlayer(player.GetLocation().y, player.GetLocation().x); // Move towards the player
                }
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
            }

            /*if (mazeinfo.timesVisited[moves.getYCoord()][moves.getXCoord()] > 3) { // Am I dithering around in one area?
                System.out.println("blindly");
                counter++;
            }*/

            //view.paintCosts(moves.GetLocation(), moves, mastermazeinfo);
            view.paintBoss(moves.GetLocation(), moves, mastermazeinfo);

            if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)) {
                view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                System.out.println("    " + getAID().getName() + ": Player caught.");
                view.EndGame("lose");
                doDelete();
            }
            if (player.GetLocation() == null) {
                System.out.println("PLAYER MOVE LIST IS EMPTY");
            }
        }
    } // End of inner class Movement
}
