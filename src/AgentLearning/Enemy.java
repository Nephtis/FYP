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
 * @author Dave Halperin
 */
public class Enemy extends Agent{
    private MazeMove moves; // Contains unique list of moves that this agent has done as well as enabling agent movement
    int step = 0; // Which step we're at will determine what action we perform on tick
    private AID[] otherAgents; // list of other agents
    
    // Agent initialization  
    protected void setup() {
        System.out.println("Begin setup()");
        Object[] args = getArguments();  // [0] = maze, [1] = mazeinfo, [2] = maze view, [3] = player
        Cell[][] maze = (Cell[][]) args[0];
        PrimMazeInfo mazeinfo = new PrimMazeInfo(maze,0,0,0,0);// Unique to this agent
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
                System.out.println("Spawning bottom left (ish)");
                mazeinfo.setStartM(8);
                mazeinfo.setStartN(4);
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
        
        // Add a TickerBehaviour that does things every (0.75) seconds
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
    private class Movement extends TickerBehaviour{
        // Initialize
        public Movement(Agent a, long period) {
            super(a, period);
        }

        int i_alert = 0; // The 'counter' controlling duration (iterations) of alert mode 
        int i_search = 0; // And search, etc.
        int i_caution = 0;
        MessageTemplate mt; // The template to receive replies  
        Object[] args = getArguments();  // args [0] = maze, [1] = mazeinfo, [2] = mazeview, [3] = player
        Cell[][] maze = (Cell[][]) args[0];
        PrimMazeInfo mazeinfo = new PrimMazeInfo(maze,0,0,0,0); // Unique to this agent (params are all 0 because agent doesn't need to know player's "target" coords)
        PrimMazeInfo mastermazeinfo = (PrimMazeInfo) args[1]; // Shared by all agents
        MazeView view = (MazeView) args[2];
        PlayerMazeMove player = (PlayerMazeMove) args[3]; // Enables info about player
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
                otherAgents = new AID[result.length];
                for (int i=0; i<result.length; i++){
                    otherAgents[i] = result[i].getName();
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
                    
                    if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // 'captured' player (if in the same cell)
                        // Send capture message to other agents and end the game
                        view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                        System.out.println("    "+getAID().getName()+": Player caught at x " + player.GetLocation().x + " y " + player.GetLocation().y);
                        view.EndGame("lose");
                        doDelete();
                        break;
                    } 
                    
                    // Sense the player if they're nearby
                    moves.SenseNearby();
                    sensearea = moves.getSenseList();
                    for (int i=0; i<sensearea.length; i++) {
                        if (sensearea[i] != null) {
                            if ((sensearea[i].y == player.GetLocation().y) && (sensearea[i].x == player.GetLocation().x)) {
                                // SEARCH MODE - agent has sensed player is nearby but hasn't directly spotted them
                                // Don't send a msg to other agents, just make this agent go to search mode
                                System.out.println("    "+getAID().getName()+": I've sensed something nearby...");
                                moves.FacePlayerDir(player.GetLocation());
                                step = 2; // evasion/search
                                break;    
                            }
                        }
                    }
                    
                    moves.PatrolArea(mastermazeinfo);// agent roams everywhere...
                    view.paintEnemy(moves.GetLocation(), moves);
                    //view.PrintGUIMessage("normal");

                    // Check if player is in line of sight
                    if (moves.isTargetable(player.GetLocation().y, player.GetLocation().x)) { // SPOTTED PLAYER, ALERT MODE
                        // Create and send alert message to other agents
                        ACLMessage alertmsg = new ACLMessage(ACLMessage.INFORM);
                        for (int j=0; j<otherAgents.length; j++){
                            alertmsg.addReceiver(otherAgents[j]);
                        }
                        alertmsg.setConversationId("alert mode");
                        alertmsg.setReplyWith("inform"+System.currentTimeMillis());
                        myAgent.send(alertmsg);
                            
                        // Update last known loc of player
                        mastermazeinfo.setPlayerLastKnownY(player.GetLocation().y);
                        mastermazeinfo.setPlayerLastKnownX(player.GetLocation().x);
                            
                        // Update GUI info
                        System.out.println("    "+getAID().getName()+": Player in my line of sight!");
                        view.alertmode = true;
                        view.PrintGUIMessage("alert"); // Display the alert message on the GUI
                        // Alert message ensures other agents don't also update it (i.e. it only gets painted once)
                        /*try {
                            Thread.sleep(500); // This has to be surrounded in a try/catch
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                        }*/
                        System.out.println("    "+getAID().getName()+": Going to alert mode!");
                        step = 1; // change to different 'action'
                        break;
                    }
                    
                break;
                    // ALERT CASE ----------------------------------------------------------------------------------------------------------------------------------------
                case 1:     
                    // Receive search mode replies
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
                    
                    moves.UpdateAStarStartCoords(moves.getYCoord(), moves.getXCoord()); // Set the initial coords for this A* chase (can get changed later if we see player again)
                    
                    while (i_alert < 20){ // (time in ms is still used at the top level of the behaviour, this just says do it for ONLY 20 'iterations')
                        // Wait time between each move
                        if (!(view.running)){
                            doDelete();
                        }
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){ // Captured player
                            // Send capture message to other agents
                            view.paintEnemy(moves.GetLocation(), moves); // re-paint so we're not left with an afterimage
                            System.out.println("    "+getAID().getName()+": Player caught.");
                            view.EndGame("lose"); // End the game with a "player lose" condition
                            doDelete();
                            break;
                        }
                        try {
                            if (!moves.isTargetableAnyDir(player.GetLocation().y, player.GetLocation().x)){ // If player is not visible to the agent                           
                                moves.AStarMoveToCoords(mastermazeinfo.getPlayerLastKnownY(), mastermazeinfo.getPlayerLastKnownX(), mastermazeinfo);
                                // Head for last known loc...                               
                            }
                            else { // If the player is visible, just rush towards them and update their last known coords
                                mazeinfo.resetSeen(); // Our old "seen" cells will now be irrelevant, so reset them
                                mastermazeinfo.setPlayerLastKnownX(player.GetLocation().x); // Shared by all agents - let everyone know the new coords
                                mastermazeinfo.setPlayerLastKnownY(player.GetLocation().y);
                                moves.BlindlyPursuePlayer(player.GetLocation().y, player.GetLocation().x); // Move towards the player
                            }
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
                    
                    // SEARCH CASE ----------------------------------------------------------------------------------------------------------------------------------------
                case 2:    
                    view.paintEnemy(moves.GetLocation(), moves);
                    System.out.println("    "+getAID().getName()+"Searching for player...");
                    
                    view.alertmode = false;
                    view.searchmode = true;
                    
                    view.PrintGUIMessage("search");
                    while (i_search < 20){
                        // Wait time between each move
                        //try {
                        //    Thread.sleep(600);
                            moves.SearchArea(moves.getXCoord(), moves.getYCoord());
                        //} catch (InterruptedException ex) {
                        //    Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                        //}
                        
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
                        
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){
                            view.paintEnemy(moves.GetLocation(), moves);
                            System.out.println("    "+getAID().getName()+": Player caught at x " + player.GetLocation().x + " y " + player.GetLocation().y);
                            view.EndGame("lose");
                            doDelete();
                            break;
                        }
                        
                        if (moves.isTargetable(player.GetLocation().y, player.GetLocation().x)) { // SPOTTED PLAYER, GO TO ALERT MODE
                            // Update last known loc of player
                            mastermazeinfo.setPlayerLastKnownY(player.GetLocation().y);
                            mastermazeinfo.setPlayerLastKnownX(player.GetLocation().x);
                            
                            // Send alert message to other agents
                            ACLMessage alertmsg = new ACLMessage(ACLMessage.INFORM);
                            for (int j=0; j<otherAgents.length; j++){
                                alertmsg.addReceiver(otherAgents[j]);
                            }
                            alertmsg.setConversationId("alert mode");
                            alertmsg.setReplyWith("inform"+System.currentTimeMillis());
                            myAgent.send(alertmsg);
                            System.out.println("    "+getAID().getName()+": Player in my line of sight!");
                            view.alertmode = true;
                            view.PrintGUIMessage("alert"); // Display the alert message on the GUI
                            /*try {
                                Thread.sleep(500); // This has to be surrounded in a try/catch
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                            }*/
                            System.out.println("    "+getAID().getName()+": Going to alert mode!");
                            step = 1; // change to different 'action'
                            break;
                        }
                        
                        i_search = 0;
                        mazeinfo.resetSeen();
                        step = 3;
                        break;
                    }
                    
                case 3:
                    view.searchmode = false;
                    //view.cautionmode = true;
                    //view.PrintGUIMessage("caution");
                    System.out.println("    "+getAID().getName()+": Entering caution mode");
                    // Do caution stuff
                    while (i_caution < 30){
                        //try {
                         //   Thread.sleep(700);
                            moves.SearchArea(moves.getXCoord(), moves.getYCoord());
                        //} catch (InterruptedException ex) {
                        //    Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                        //}
                        
                        view.paintEnemy(moves.GetLocation(), moves);
                        i_caution++;
                        
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
                        
                        if ((moves.getYCoord() == player.GetLocation().y) && (moves.getXCoord() == player.GetLocation().x)){
                            view.paintEnemy(moves.GetLocation(), moves);
                            System.out.println("    "+getAID().getName()+": Player caught at x " + player.GetLocation().x + " y " + player.GetLocation().y);
                            view.EndGame("lose");
                            doDelete();
                            break;
                        }
                        
                        if (moves.isTargetable(player.GetLocation().y, player.GetLocation().x)) {
                            // ALERT MODE
                            // Send alert message to other agents
                            ACLMessage alertmsg = new ACLMessage(ACLMessage.INFORM);
                            for (int j=0; j<otherAgents.length; j++){
                                alertmsg.addReceiver(otherAgents[j]);
                            }
                            alertmsg.setConversationId("alert mode");
                            alertmsg.setReplyWith("inform"+System.currentTimeMillis());
                            myAgent.send(alertmsg);
                            System.out.println("    "+getAID().getName()+": Player in my line of sight!");
                            view.alertmode = true;
                            view.PrintGUIMessage("alert"); // Display the alert message on the GUI
                            /*try {
                                Thread.sleep(500); // This has to be surrounded in a try/catch
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Enemy.class.getName()).log(Level.SEVERE, null, ex);
                            }*/
                            System.out.println("    "+getAID().getName()+": Going to alert mode!");
                            step = 1; // change to different 'action'
                            break;
                        }
                    step = 0; // All clear, return to normal state (patrol)
                    break; 
                }
            }
        } // End of inner class Movement
    } // End of onTick()
} // End of Enemy class