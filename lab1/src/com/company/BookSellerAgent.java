package com.company;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class BookSellerAgent extends Agent {
    private Hashtable catalogue;

    private BookSellerGui myGui;

    @Override
    protected void setup() {
        catalogue = new Hashtable();

        myGui = new BookSellerGui(this);
        myGui.showGui();

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }

    @Override
    protected void takeDown() {
        myGui.dispose();
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    public void updateCatalogue(final String title, final int price) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                catalogue.put(title, new Integer(price));
            }
        });
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.get(title);
                if(price != null){
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                }
                else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogue.remove(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title+" sold to agent "+msg.getSender().getName());
                }
                else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }
}
