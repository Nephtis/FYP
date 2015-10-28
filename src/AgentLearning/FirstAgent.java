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
 * NOTE: THIS IS AN EXAMPLE CLASS FOR MY OWN REFERENCE AND WILL NOT BE PART OF THE FINAL PRODUCT
 */
public class FirstAgent extends Agent{
    private AID[] secondAgents; // list of other agents (of a different type)
    
    // Agent initialization  
    protected void setup() {     
    // Print a welcome message     
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        // Make this agent terminate
  	// Get the title of the book to buy as a start-up argument     
        Object[] args = getArguments();     
        if (args != null && args.length > 0) {       
            String argName = (String) args[0];
            System.out.println("arg name is "+argName);     
        } else {       
            // Make the agent terminate immediately       
            System.out.println("No args specified");       
            doDelete();     
        }
        // Add a TickerBehaviour that does things every 5 seconds
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() {
                System.out.println("5 second tick...");
                // Update the list of second agents
                // In order to publish a service an agent must create a proper description 
                // (as an instance of the DFAgentDescription class) 
                // and call the register() static method of the DFService class
                // BUT this is a template with which we will search for 
                // second agents, so we aren't actually register()ing ourselves
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                // Here, we want to find agents that are SELLING
                sd.setType("selling");
                template.addServices(sd);
                try{
                    // An agent wishing to search for services must provide the DF 
                    // with a template description. The result of the search is 
                    // the list of all the descriptions that match the provided template. 
                    // A description matches the template if all the fields specified in 
                    // the template are present in the description with the same values.
                    
                    // (Note the use of the myAgent protected variable: each behavior 
                    // has a pointer to the agent that is executing it)
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result != null){
                        System.out.println("Found the following second agents:");
                    } else {
                        System.out.println("No second agents found");
                    }
                    secondAgents = new AID[result.length];
                    for (int i=0; i<result.length; i++){
                        secondAgents[i] = result[i].getName();
                        System.out.println(secondAgents[i].getName());
                    }
                } catch (FIPAException fe){
                    fe.printStackTrace();
                }
                // Perform the request
                // (Note that the update of the list of known second agents is 
                // done before each attempt to buy the target item since second
                // agents may dynamically appear and disappear in our system)
                myAgent.addBehaviour(new RequestPerformer());
            }
        }); // end of addBehaviour
    }
    
     // Put agent clean-up operations here   
    protected void takeDown() {     
        // Print a dismissal message     
        System.out.println("Agent "+getAID().getName()+" terminating.");   
    }
    /**
	Inner class RequestPerformer.
	This is the behaviour used by first agents to request second 
	agents things.
    */
    private class RequestPerformer extends Behaviour{
        // Which step we're at will determine what action 
        // we perform here
        private int step = 0;
        private MessageTemplate mt; // The template to receive replies
        private AID bestSeller; // the agent who provides the best offer
        private int bestPrice; // the best offered price
        private int repliesCount = 0; // The counter of replies from second agents
        
        private String argName;
        Object[] args = getArguments();                         
        
        public void action(){
            if (args != null && args.length > 0) {       
                        argName = (String) args[0];
                    }
            switch(step){
                case 0:
                    // Send the CFP (call for proposal) to all second agents
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    // Make all second agents receivers of this message
                    for (int i=0; i<secondAgents.length; i++){
                        cfp.addReceiver(secondAgents[i]);
                    }
                    // The content i.e. the actual information included in the message 
                    // (i.e. the action to be performed in a REQUEST message, the fact that 
                    // the sender wants to disclose in an INFORM message …)
                    cfp.setContent(argName);
                    // Some fields used to control several concurrent conversations and 
                    // to specify timeouts for receiving a reply such as conversation-id, 
                    // reply-with, in-reply-to, reply-by
                    cfp.setConversationId("step-0");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis());
                    myAgent.send(cfp);
                    // Prepare the template to get responses
                    // so that we only receive messages in a certain format
                    // i.e. only the ones we're interested in here
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("step-0"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    // Whenever a conversation has to be carried out it is good 
                    // practice to specify the conversation control fields in the 
                    // messages exchanged within the conversation. 
                    // This allows you to easily and un-ambiguously create 
                    // templates matching the possible replies. 
                    step = 1;
                    break;
                
                case 1:
                    // Receive all proposals/refusals from second agents.
                    // JADE runtime automatically posts messages in the receiver’s 
                    // private message queue as soon as they arrive. 
                    // An agent can pick up messages from its message queue by 
                    // means of the receive() method
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null){
                        // Reply received, process it
                        if (reply.getPerformative() == ACLMessage.PROPOSE){
                            // This is an offer
                            int anInt = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || anInt < bestPrice){
                                // This is the best offer at the moment
                                bestPrice = anInt;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCount++;
                        if (repliesCount >= secondAgents.length){
                            // We have received all replies
                            step = 2;
                        }
                    }
                    else { // No reply received
                        // Mark the behaviour as “blocked” so that the agent does 
                        // not schedule it for execution anymore. When a new message 
                        // is inserted in the agent’s message queue all blocked 
                        // behaviours become available for execution again so 
                        // that they have a chance to process the received message.
                        // This makes behaviour execution a lot less CPU-intensive.
                        block();
                    }
                    break;
                    
                case 2:
                    // Send the purchase order to the second agent that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller); // Only send it to the one agent we want to contact
                    order.setContent(argName); 
                    order.setConversationId("item-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("item-trade"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                   
                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null){
                        // Purchase order reply received, process it
                        if (reply.getPerformative() == ACLMessage.INFORM){
                            // Purchase successful. We can terminate
                            System.out.println(argName + "successfully purchased from " + reply.getSender().getName());
                            System.out.println("Price = " + bestPrice);
                            myAgent.doDelete();
                        } else {
                            System.out.println("Attempt failed: requested item already sold.");
                        }
                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
                
            }
        }
        
        public boolean done() {
            if (step == 2 && bestSeller == null){
                System.out.println("Attempt failed: " + argName + " not available");
            }
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    } // End of inner class RequestPerformer
    
}
