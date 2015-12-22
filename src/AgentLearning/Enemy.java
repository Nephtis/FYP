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
    private MazeMove moves; // unique list of moves that this agent has done
    // Which step we're at will determine what action 
    // we perform on tick
    int step = 0;
    private AID[] otherAgents; // list of other agents
    private int[][] pursuitAlgorithmSuccess[][]; // 0 = blind rush, 1 = A*, 2 = etc...
    /* 
    Each time the agents go to pursue the player, they pick the one that has the highest success rate.
    If two algorithms have the same success rate, pick the earlier one in the array.
    The array will look something like this:
    [0][1] // Blind rush has a score of 1
    [1][3] // A* has a score of 3
    [2][2] // Something else has a score of 2, etc.
    So the agent will pick A* for its pursuit algorithm.
    QUESTIONS: How do we pick a different one? e.g. if A* is more successful than blind rush, agents will always do that, even if there's a better solution in the next array row
    Maybe have rate of success? e.g. (successes of a particular algorithm / total number of games)?
    Have a "try" method, where agents will try each different algorithm at least once, and if it doesn't look promising, abandon it?
    
    */
    
    
    // Agent initialization  
    protected void setup() {
        System.out.println("Begin setup()");
        Object[] args = getArguments();  // [0] = maze, [1] = mazeinfo, [2] = maze view, [3] = player
        Cell[][] maze = (Cell[][]) args[0];
        PrimMazeInfo mazeinfo = new PrimMazeInfo(maze,0,0,0,0);//(PrimMazeInfo) args[1];
        MazeView view = (MazeView) args[2];
        // Decide agent spawn
        switch (view.enemyspawn){
            case (0):
                // Spawn this agent in the top left corner
                System.out.println("Spawning top left");
                mazeinfo.setStartM(1);
                mazeinfo.setStartN(1);
                view.enemyspawn++; // so the next enemy that spawns will spawn in another position
                break;
            case (1):
                System.out.println("Spawning top right");
                mazeinfo.setStartM(1);
                mazeinfo.setStartN(8);
                view.enemyspawn++;
                break;
            case (2):
                System.out.println("Spawning bottom left");
                mazeinfo.setStartM(8);
                mazeinfo.setStartN(1);
                view.enemyspawn++;
                break;
            case (3):
                System.out.println("Spawning bottom right");
                mazeinfo.setStartM(8);
                mazeinfo.setStartN(8);
                view.enemyspawn++;
                break;
            default:
                System.out.println("Spawning middle");
                mazeinfo.setStartM(4);
                mazeinfo.setStartN(4);
                break;
        }
        // Initialize move list
        moves = new MazeMove(maze, mazeinfo);

        // Register ourselves in the dfd so that other agents can find us
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("enemy");
        sd.setName("enemy-register");
        dfd.addServices(sd);     
        try{
            DFService.register(this, dfd);
        } catch (FIPAException fe){
            fe.printStackTrace();
        }
        
        // Add a TickerBehaviour that does things every (0.5) seconds
        addBehaviour(new Movement(this, 750));

        System.out.println("    "+getAID().getName()+": End of setup()");
    } // end of setup
    
     // Put agent clean-up operations here   
    protected void takeDown() {        
        System.out.println("    "+getAID().getName()+": Terminating.");  
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

        int i_alert = 0; // The 'counter' controlling duration (iterations) of alert mode 
        int i_search = 0;
        int i_caution = 0;
        MessageTemplate mt; // The template to receive replies  
        Object[] args = getArguments();  // args [0] = maze, [1] = mazeinfo, [2] = mazeview, [3] = player
        Cell[][] maze = (Cell[][]) args[0];
        PrimMazeInfo mazeinfo = new PrimMazeInfo(maze,0,0,0,0);//(PrimMazeInfo) args[1];
        MazeView view = (MazeView) args[2];
        PlayerMazeMove player = (PlayerMazeMove) args[3];
        
        MoveInfo[] lineofsight = new MoveInfo[3];
        MoveInfo[] sensearea = new MoveInfo[4];
        
        protected void onTick(){
            if (!(view.running)){
                doDelete();
            }
            // Find other agents
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            // Here, we want to find agents of type "enemy"
            sd.setType("enemy");
            template.addServices(sd);
            try{
                DFAgentDescription[] result = DFService.search(myAgent, template);
                /*if (result != null){
                    System.out.println("Found the following other agents:");
                } else {
                    System.out.println("No other agents found");
                }*/
                otherAgents = new AID[result.length];
                for (int i=0; i<result.length; i++){
                    otherAgents[i] = result[i].getName();
                    //System.out.println(otherAgents[i].getName());
                }
            } catch (FIPAException fe){
                fe.printStackTrace();
            }
                
            if (view.shouldreset){ // If agents need to reset
                System.out.println("    "+getAID().getName()+": shouldreset is true, resetting agent pos...");
                moves.ResetPosition(); // Reset pos back to start location
                view.paintEnemy(moves.GetLocation(), moves);
            }
            
            switch(step){
                case 0: // Patrol

                    // listen for alert messages from other agents
                    // I think this HAS to come first to keep agents in sync...
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null){
                        // Reply received, process it
                        if (reply.getPerformative() == ACLMessage.INFORM){
                            // We're going to alert mode (ignore other message types)
                            System.out.println("    "+getAID().getName()+": Other agent has spotted player, I'll go on alert too!");
                            step = 1;
                            break;
                        }
                    }
                    
                    if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // for now...
                        // 'captured' player (right now just 'if on top of player')
                        // Send capture message to other agents (including to HQ which will end the game)
                        view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                        System.out.println("    "+getAID().getName()+": Player caught at x " + player.GetLocation().x + " y " + player.GetLocation().y);
                        view.EndGame("lose");
                        doDelete();
                        break;
                    } 
                    
                    moves.SenseNearby();
                    sensearea = moves.getSenseList();
                    // Sense the player if they're nearby
                    for (int i=0; i<sensearea.length; i++) {
                        if (sensearea[i] != null) {
                            if ((sensearea[i].y == player.GetLocation().y) && (sensearea[i].x == player.GetLocation().x)) {
                                // SEARCH MODE - agent has sensed player is nearby but hasn't directly spotted them
                                // Currently not sending a msg to other agents, just making this agent go to search mode
                                System.out.println("    "+getAID().getName()+": I've sensed something nearby...");
                                moves.FacePlayerDir(player.GetLocation());
                                step = 2; // evasion/search
                                break;    
                            }
                        }
                    }
                    
                    moves.PatrolArea();// agent roams everywhere...
                    view.paintEnemy(moves.GetLocation(), moves);

                    // Check if player is in line of sight
                    // (Currently greater than 1 Cell away i.e. not next to enemy, also think about move direction being deceptive i.e. facing wrong way)
                    lineofsight = moves.getLineOfSight();
                    //if (lineofsight != null && lineofsight.length != 0){
                        for (int i=0; i<lineofsight.length; i++) {
                            if (lineofsight[i] != null) {
                                //System.out.println("Enemy: lineofsight length is " + lineofsight.length);
                                //System.out.println("Enemy: lineofsight["+ i +"] y " + lineofsight[i].y + ", x " + lineofsight[i].x);
                                //System.out.println("Enemy: player y " + player.GetLocation().y + ", x " + player.GetLocation().x);
                                if ((lineofsight[i].y == player.GetLocation().y) && (lineofsight[i].x == player.GetLocation().x)) {
                                    // ALERT MODE
                                    // Send alert message to other agents
                                    ACLMessage alertmsg = new ACLMessage(ACLMessage.INFORM);
                                    for (int j=0; j<otherAgents.length; j++){
                                        alertmsg.addReceiver(otherAgents[j]);
                                    }
                                    alertmsg.setConversationId("alert mode");
                                    alertmsg.setReplyWith("inform"+System.currentTimeMillis());
                                    myAgent.send(alertmsg);
                                    System.out.println("    "+getAID().getName()+": Player in my line of sight! Waiting 1 secs (so player can move away)");
                                    view.alertmode = true;
                                    view.PrintGUIMessage("alert"); // Display the alert message on the GUI
                                    // Then send a message to other agents so they don't also do it (i.e. it only gets painted once)
                                    try {
                                        Thread.sleep(500); // This has to be surrounded in a try/catch
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    System.out.println("    "+getAID().getName()+": Done waiting, go to alert mode!");
                                    step = 1; // change to different 'action'
                                    break;
                                }
                            }
                        //}
                    }
                    
                break;
                    
                case 1:                   
                    reply = myAgent.receive(mt);
                    if (reply != null){
                        // Reply received, process it
                        if (reply.getPerformative() == ACLMessage.PROPOSE){
                            // We're going to alert mode (ignore other message types)
                            System.out.println("    "+getAID().getName()+": Other agent has lost player, I'll go to search mode");
                            step = 2;
                            break;
                        }
                    }
                    
                    // (time in ms is still used at the top level of the behaviour, this just says do it for ONLY 20 'iterations')
                    while (i_alert < 20){
                        // Wait time between each move
                        if (!(view.running)){
                            doDelete();
                        }
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // for now...
                            // 'captured' player (right now just 'if on top of player')
                            // Send capture message to other agents
                            view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                            System.out.println("    "+getAID().getName()+": Player caught.");
                            view.EndGame("lose"); // End the game with a "player lose" condition
                            doDelete();
                            break;
                        }
                        try {
                            //moves.MoveToCoords(player.GetLocation().y, player.GetLocation().x);
                            //moves.PursuePlayer(player.GetLocation().y, player.GetLocation().x);
                            moves.AStarPursuePlayer(player.GetLocation().y, player.GetLocation().x);
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (moves.getYCoord() == -1){
                            System.out.println("    "+getAID().getName()+": my Y is -1!");
                        }
                        if (moves.getXCoord() == -1){
                            System.out.println("    "+getAID().getName()+": my X is -1!");
                        }
                        if (player.GetLocation() == null) {
                            System.out.println("PLAYER MOVE LIST IS EMPTY");
                        }
                        //System.out.println("Player is at Y: " + player.GetLocation().y + " X: " + player.GetLocation().x);
                        
                        view.paintEnemy(moves.GetLocation(), moves);
                        i_alert++;   
                    }
                    i_alert = 0; // reset the alert counter
                    
                    // Clear the "seen" array
                    mazeinfo.resetSeen();
                    mazeinfo.resetCosts();
                    
                    // Send message to other agents (this way even if they're somehow out of sync they will all 'move on' together)
                    ACLMessage searchmsg = new ACLMessage(ACLMessage.PROPOSE);
                    for (int j=0; j<otherAgents.length; j++){
                        searchmsg.addReceiver(otherAgents[j]);
                    }
                    searchmsg.setConversationId("search mode");
                    searchmsg.setReplyWith("propose"+System.currentTimeMillis());
                    myAgent.send(searchmsg);
                    step = 2; // evasion/search
                    break;    
                    
                case 2:    
                    view.paintEnemy(moves.GetLocation(), moves);
                    //System.out.println("Searching for player...");
                    view.alertmode = false;
                    view.searchmode = true;
                    view.PrintGUIMessage("search");
                    while (i_search < 5){
                        // Wait time between each move
                        try {
                            Thread.sleep(750);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //moves.SearchArea(y, x, m, n);
                        view.paintEnemy(moves.GetLocation(), moves);
                        i_search++;
                        
                        moves.SenseNearby();
                        sensearea = moves.getSenseList();
                        // Sense the player if they're nearby
                        for (int i=0; i<sensearea.length; i++) {
                            if (sensearea[i] != null) {
                                if ((sensearea[i].y == player.GetLocation().y) && (sensearea[i].x == player.GetLocation().x)) {
                                    // This time, if you get sensed, this will trigger an alert since guards are on edge
                                    ACLMessage alertmsg = new ACLMessage(ACLMessage.INFORM);
                                    for (int j=0; j<otherAgents.length; j++){
                                        alertmsg.addReceiver(otherAgents[j]);
                                    }
                                    alertmsg.setConversationId("alert mode");
                                    alertmsg.setReplyWith("inform"+System.currentTimeMillis());
                                    myAgent.send(alertmsg);
                                    System.out.println("    "+getAID().getName()+": Sensed player! Going to alert mode!");
                                    view.alertmode = true;
                                    view.PrintGUIMessage("alert"); // Display the alert message on the GUI
                                    step = 1;
                                    break;    
                                }
                            }
                        }
                        
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // for now...
                            //System.out.println("Player spotted while searching, waiting 1 secs (so player can move away)"); // for now
                            view.PrintGUIMessage("alert");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            //System.out.println("Done waiting, go to alert mode!");
                            view.paintEnemy(moves.GetLocation(), moves);
                            step = 1;
                            break;
                        }
                    }
                    step = 3;
                    break;
                    
                case 3:
                    view.searchmode = false;
                    System.out.println("    "+getAID().getName()+": Entering caution mode");
                    // Do caution stuff
                    //step = 0; // All clear, return to normal state (patrol)
                    step = 0;
                    break; 
            }
        }
    } // End of inner class Movement
    
}
