package AgentLearning;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 *
 * @author Dave
 */
public class Boss extends Agent {

    private MazeMove moves; // unique list of moves that this agent has done

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

            moves.AStarPursuePlayer(player.GetLocation().y, player.GetLocation().x, mazeinfo);
            
            //view.paintCosts(moves.GetLocation(), moves, mazeinfo);
            view.paintBoss(moves.GetLocation(), moves, mazeinfo);

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