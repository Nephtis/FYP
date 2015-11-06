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
        MessageTemplate mt; // The template to receive replies  
        
        protected void onTick(){
            // re-declaring here seems redundant but get null 
            // pointer exception when done above (outside setup())
            Object[] args = getArguments();  // args [0] = maze, [1] = mazeinfo, [2] = mazeview, [3] = player
            PrimMazeInfo mazeinfo = (PrimMazeInfo) args[1];
            MazeView view = (MazeView) args[2];
            PlayerMazeMove player = (PlayerMazeMove) args[3];
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
                        view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                        step = 1; // change to different 'action'
                        break;
                    }
                break;
                    
                case 1:
                    System.out.println("ALERT MODE");
                    break;    
                case 2:    
                    //step = 3;
                    doDelete();
                    break; 
                case 3:
                    //step = 4;
                    break;               
            }
        }
        
//        public boolean done() {
//            System.out.println("Done.");
//            return (step == 2 || step == 4);
//        }
    } // End of inner class Movement
    
}
