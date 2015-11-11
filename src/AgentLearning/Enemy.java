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
    public MazeMove moves; // unique list of moves that this agent has done
    
    public Enemy(){
        
    }
    
    // Agent initialization  
    protected void setup() {
        System.out.println("Begin setup()");
        Object[] args = getArguments();  // [0] = maze, [1] = mazeinfo, [2] = maze view, [3] = player
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
        System.out.println("Agent "+getAID().getName()+" terminating.");  
        doDelete();
    }
    /**
	Inner class Movement.
    */
    // Maybe this would be better as a CyclicBehaviour which just waits for different times depending on what it's doing
    private class Movement extends TickerBehaviour{ // 'movement' is one thing, perhaps have multiple behaviours running concurrently?
        // e.g. 'movement' and 'vision'?
        public Movement(Agent a, long period) {
            super(a, period);
        }
        
        // Which step we're at will determine what action 
        // we perform on tick
        int step = 0;
        int i_alert = 0; // The 'counter' controlling duration (iterations) of alert mode 
        int i_search = 0;
        int i_caution = 0;
        MessageTemplate mt; // The template to receive replies  
        Object[] args = getArguments();  // args [0] = maze, [1] = mazeinfo, [2] = mazeview, [3] = player
        PrimMazeInfo mazeinfo = (PrimMazeInfo) args[1];
        MazeView view = (MazeView) args[2];
        PlayerMazeMove player = (PlayerMazeMove) args[3];
        
        MoveInfo[] lineofsight = new MoveInfo[3];
        
        protected void onTick(){            
            switch(step){
                case 0: // Patrol
                    // Right now, just move at random. Change and expand upon this later
                    view.paintEnemy(moves.GetLocation(), moves);
                    view.paintLineOfSight(moves);
                    //moves.LookAhead(); // also happens in PatrolArea...
                    moves.PatrolArea(0,0,0,0);
                    
                    // Check if player is in line of sight
                    // (Currently only works if player is in the final line of sight coord, also think about move direction being deceptive i.e. facing wrong way)
                    lineofsight = moves.getLineOfSight();
                    //if (lineofsight != null && lineofsight.length != 0){
                        for (int i=0; i<lineofsight.length; i++) {
                            if (lineofsight[i] != null) {
                                System.out.println("Enemy: lineofsight length is " + lineofsight.length);
                                System.out.println("Enemy: lineofsight["+ i +"] y " + lineofsight[i].y + ", x " + lineofsight[i].x);
                                System.out.println("Enemy: player y " + player.GetLocation().y + ", x " + player.GetLocation().x);
                                if ((lineofsight[i].y == player.GetLocation().y) && (lineofsight[i].x == player.GetLocation().x)) {
                                    // ALERT MODE
                                    // Send alert message to other agents
                                    System.out.println("Enemy: Player in my line of sight! Waiting 3 secs (so player can move away)");
                                    try {
                                        Thread.sleep(3000); // This has to be surrounded in a try/catch
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    System.out.println("Enemy: Done waiting, go to alert mode!");
                                    view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                                    step = 1; // change to different 'action' (relative to movement?)
                                    break;
                                }
                            }
                        //}
                    }
                    
                break;
                    
                case 1: 
                    view.paintEnemy(moves.GetLocation(), moves); // (not currently needed?) will need to get 'correct' location (from the correct TrackInfo)
                    // (not currently needed?) push enemy's current loc onto the "alert" TrackInfo stack (so it can "start" there)                   
                    //(time in ms is still used at the top level of the behaviour, this just says do it for ONLY 20 'iterations')
                    while (i_alert < 20){
                        // Wait time between each move
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        moves.PursuePlayer(player.GetLocation().y, player.GetLocation().x);
                        view.paintEnemy(moves.GetLocation(), moves);
                        i_alert++;
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // for now...
                            // 'captured' player (right now just 'if on top of player')
                            // Send capture message to other agents (including to HQ which will end the game)
                            view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                            System.out.println("Player caught.");
                            doDelete();
                            break;
                        }     
                    }
                    i_alert = 0; // reset the alert counter
                    // Send message to other agents (this way even if they're somehow out of sync they will all 'move on' together)
                    step = 2; // evasion/search
                    break;    
                    
                case 2:    
                    view.paintEnemy(moves.GetLocation(), moves);
                    System.out.println("Searching for player...");
                    while (i_search < 30){
                        // Wait time between each move
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //moves.SearchArea(y, x, m, n);
                        view.paintEnemy(moves.GetLocation(), moves);
                        i_search++;
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // for now...
                            System.out.println("Player spotted while searching, waiting 1 secs (so player can move away)"); // for now
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println("Done waiting, go to alert mode!");
                            view.paintEnemy(moves.GetLocation(), moves);
                            step = 1;
                            break;
                        }
                    }
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
