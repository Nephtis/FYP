package AgentLearning;

import jade.core.Agent; 
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
// publish and search for services through method calls
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dave
 */
public class Enemy extends Agent{
    //private AID[] secondAgents; // list of other agents (of a different type)
    public MazeMove moves; // unique list of moves that this agent has done
    
    //MoveInfo current = new MoveInfo(0,0,0);
    
    public Enemy(){
        
    }
    
    // Agent initialization  
    protected void setup() {
        System.out.println("Begin setup()");
        Object[] args = getArguments();  // [0] = maze, [1] = mazeinfo, [2] = maze view
        Cell[][] maze = (Cell[][]) args[0];
        PrimMazeInfo mazeinfo = (PrimMazeInfo) args[1];
        // Initialize move list
        moves = new MazeMove(maze, mazeinfo);
        
        // Print a welcome message     
        System.out.println("    Enemy "+getAID().getName()+" is ready.");
        
        // Add a TickerBehaviour that does things every (0.5) seconds
        addBehaviour(new Movement(this, 500));
        System.out.println("End of setup()");
    } // end of setup
    
     // Put agent clean-up operations here   
    protected void takeDown() {     
        // Print a dismissal message     
        System.out.println("Agent "+getAID().getName()+" terminating.");  
        doDelete();
    }
    /**
	Inner class DoThings.
    */
    private class Movement extends TickerBehaviour{ // 'movement' is one thing, perhaps have multiple behaviours running concurrently?
        // e.g. 'movement' and 'vision'?
        public Movement(Agent a, long period) {
            super(a, period);
        }
        
        // Which step we're at will determine what action 
        // we perform on tick
        int step = 0;
        int i = 5; // The 'counter' controlling duration of alert mode 
        MessageTemplate mt; // The template to receive replies  
        Object[] args = getArguments();  // args [0] = maze, [1] = mazeinfo, [2] = mazeview, [3] = player
        PrimMazeInfo mazeinfo = (PrimMazeInfo) args[1];
        MazeView view = (MazeView) args[2];
        PlayerMazeMove player = (PlayerMazeMove) args[3];
        
        protected void onTick(){
            
            switch(step){
                case 0: // Patrol
                    // Right now, just move at random. Change and expand upon this later
                    System.out.println("Patrolling...");
                    view.paintEnemy(moves.GetLocation(), moves);
                    moves.PatrolArea(0,0,0,0);
                    
                    // sometimes null exception in the if?
//                    if (moves.getYCoord() == mazeinfo.getTargetM() && moves.getXCoord() == mazeinfo.getTargetN()) { // coords are the exit coords
//                        moves.setDone();
//                        view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
//                        System.out.println("Exit found.");
//                        doDelete();
//                        break;
//                    }
                    
                    if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // for now...
                        //ALERT MODE
                        // Send alert message to other agents
                        System.out.println("Player spotted, switching to alert mode!");
                        view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                        step = 1; // change to different 'action' (relative to movement?)
                        break;
                    }
                break;
                    
                case 1: 
                    view.paintEnemy(moves.GetLocation(), moves); // will need to get 'correct' location (from the correct TrackInfo)
                    // push enemy's current loc onto the "alert" TrackInfo stack (so it can "start" there)
                    
                    //(ms is still used at the top level of the behaviour, this just says do it for ONLY 20 iterations)
                    while (i > 0){
                        System.out.println("Inside while loop... Pursuing player!");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                }
                        moves.PursuePlayer(0,0);
                        i--;
                        System.out.println("i is " + i);
                        // REMEMBER: this will need to use the correct TrackInfo from MazeMove (i.e. get the correct y and x coords) 
                        // Or will it? Try using 'master' movelist...
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // for now...
                            // 'captured' player (right now just 'if on top of player')
                            // Send capture message to other agents
                            view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                            System.out.println("Player caught.");
                            doDelete();
                            break;
                        }     
                    }
                    i = 20; // reset the alert counter
                    // Send message to other agents (this way even if they're somehow out of sync they will all 'move on' together)
                    step = 2; // evasion/search
                    break;    
                    
                case 2:    
                    System.out.println("Searching for player...");
                    step = 3;
                    break;
                    
                case 3:
                    System.out.println("Entering caution mode");
                    step = 0; // All clear, return to normal state (patrol)
                    break; 
                    
                case 4: // necessary? or just do it inside if? need to see how this will work
                    System.out.println("Player caught.");
                    doDelete();
                    break;
            }
        }
        
//        public boolean done() {
//            System.out.println("Done.");
//            return (step == 2 || step == 4);
//        }
    } // End of inner class Movement
    
}
