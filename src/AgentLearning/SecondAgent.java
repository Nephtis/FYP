package AgentLearning;

import jade.core.Agent; 
import jade.core.AID;
import jade.core.behaviours.*;
// publish and search for services through method calls
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

/**
 *
 * @author Dave
 * NOTE: THIS IS AN EXAMPLE CLASS FOR MY OWN REFERENCE AND WILL NOT BE PART OF THE FINAL PRODUCT
 */
public class SecondAgent extends Agent{
    // The catalogue of items for sale (maps the title to a price)
    private Hashtable catalogue;
    // The GUI by means of which the user can add items to the catalogue
    private SellerGui myGui;
    
    // Agent initialization  
    protected void setup() {         
        System.out.println("Hello! Agent "+getAID().getName()+" is ready.");
        // Create the catalogue
        catalogue = new Hashtable();
        // Create and show the GUI
        myGui = new SellerGui(this);
        myGui.showGui();
        
        // Register the selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        // Remember how in FirstAgent we specified a template
        // with type "selling" to search for?
        // Here we are setting the type "selling" as what this agent
        // actually does.
        sd.setType("selling");
        sd.setName("item-trading");
        dfd.addServices(sd);
        
        try{
            DFService.register(this, dfd);
        } catch (FIPAException fe){
            fe.printStackTrace();
        }
        
        // Add the behaviour serving queries from FirstAgents
        addBehaviour(new OfferRequestsServer());
        
        // Add the behaviour serving purchase orders from FirstAgents
        addBehaviour(new PurchaseOrdersServer());
    }
    
    // Put agent clean-up operations here
    protected void takeDown(){
        // De-register from the yellow pages
        try{
            DFService.deregister(this);
        } catch (FIPAException fe){
            fe.printStackTrace();
        }
        // CLose the GUI
        myGui.dispose();
        // Print a dismissal message
        System.out.println("Second agent " + getAID().getName() + " terminating.");
    }
    
    /**
     This is invoked by the GUI when the user adds a new item for sale
     */
    public void updateCatalogue(final String title, final int price){
        addBehaviour(new OneShotBehaviour(){
        // One-shot behaviours only execute once
            public void action(){
                catalogue.put(title, new Integer(price));
                System.out.println(title + " inserted into catalogue. Price = " + price);
            }
        });
    }
    
    /**
	   Inner class OfferRequestsServer.
	   This is the behaviour used by Second agents to serve incoming requests 
	   for offer from First agents.
	   If the requested item is in the local catalogue the Second agent replies 
	   with a PROPOSE message specifying the price. Otherwise a REFUSE message is
	   sent back.
	 */
    
    private class OfferRequestsServer extends CyclicBehaviour{
        // Cyclic behaviours execute continuously until terminated
        public void action(){
            // Only receive CFP (call for proposal) messages
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                // CFP message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                
                Integer price = (Integer) catalogue.get(title);
                if (price != null){
                    // The requested item is available for sale
                    // Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else {
                    // The requested item is NOT available for sale
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                // If there is no reply, no point in continuing to process
                // So wait until there is one
                block();
            }
        }
    } // End of inner class OfferRequestsServer
    
    /**
	   Inner class PurchaseOrdersServer.
	   This is the behaviour used by Second agents to serve incoming 
	   offer acceptances (i.e. purchase orders) from First agents.
	   The Second agent removes the purchased item from its catalogue 
	   and replies with an INFORM message to notify the First agent that the
	   purchase has been sucesfully completed.
	 */
    private class PurchaseOrdersServer extends CyclicBehaviour{
        public void action(){
            // Only respond to first agents who accepted our proposal
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null){
                // Accept proposal message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();
                
                Integer price = (Integer) catalogue.remove(title);
                if (price != null){
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title + " sold to agent " + msg.getSender().getName());
                } else {
                    // The requested item has been sold to another agent in the meanwhile
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    } // End of inner class OfferRequestsServer
}