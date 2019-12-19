import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.awt.*;
import java.io.IOException;

public class PlayerAgent extends Agent{

    private AID worldAgent, navigatorAgent;

    @Override
    protected void setup() {
        worldAgent = new AID("world", AID.ISLOCALNAME);
        navigatorAgent = new AID("navigator", AID.ISLOCALNAME);

        ACLMessage startMessage = new ACLMessage(ACLMessage.PROPOSE);
        startMessage.setConversationId("player-world");
        try{
            startMessage.setContentObject(new Point(0, 0));
        }
        catch (IOException e){
            e.printStackTrace();
        }
        startMessage.addReceiver(worldAgent);
        send(startMessage);

        addBehaviour(new WorldMessageHandler());
    }

    private class WorldMessageHandler extends CyclicBehaviour{
        @Override
        public void action(){
            ACLMessage message = receive(MessageTemplate.MatchConversationId("player-world"));
            if (message != null){
                ACLMessage requestToNavigator;
                switch (message.getPerformative()){
                    case ACLMessage.ACCEPT_PROPOSAL:
                        try{
                            RoomState newState = (RoomState) message.getContentObject();
                            switch (newState){
                                case PIT:
                                case WUMPUS:
                                    System.out.println("Player died. Game over");
                                    doDelete();
                                    break;
                                case GOLD:
                                    System.out.println("Player acquired gold!");
                                    doDelete();
                                    break;
                                default:
                                    requestToNavigator = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                                    requestToNavigator.addReceiver(navigatorAgent);
                                    requestToNavigator.setConversationId("player-navigator");
                                    try{
                                        requestToNavigator.setContentObject(newState);
                                        send(requestToNavigator);
                                        removeBehaviour(this);
                                        addBehaviour(new NavigatorMessageHandler());
                                    }
                                    catch (IOException e){
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                        }
                        catch (UnreadableException e){
                            e.printStackTrace();
                        }
                        break;
                    case ACLMessage.REJECT_PROPOSAL:
                        requestToNavigator = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        requestToNavigator.addReceiver(navigatorAgent);
                        requestToNavigator.setConversationId("player-navigator");
                        send(requestToNavigator);
                        removeBehaviour(this);
                        addBehaviour(new NavigatorMessageHandler());
                        break;
                }
            }
        }
    }

    private class NavigatorMessageHandler extends CyclicBehaviour{
        @Override
        public void action(){
            ACLMessage message = receive(MessageTemplate.MatchConversationId("player-navigator"));
            if (message != null){
                try{
                    Point newDestination = (Point) message.getContentObject();
                    ACLMessage requestToWorld = new ACLMessage(ACLMessage.PROPOSE);
                    requestToWorld.addReceiver(worldAgent);
                    requestToWorld.setConversationId("player-world");
                    try{
                        requestToWorld.setContentObject(newDestination);
                        send(requestToWorld);
                        removeBehaviour(this);
                        addBehaviour(new WorldMessageHandler());
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
                catch (UnreadableException e){
                    e.printStackTrace();
                }
            }
        }
    }
}