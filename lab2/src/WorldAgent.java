import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.awt.Point;
import java.io.IOException;

public class WorldAgent extends Agent{

    private RoomState[][] map;
    private Point playerPosition;

    @Override
    protected void setup(){
        playerPosition = new Point(0, -1);
        generateMap();
        printMap();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("world");
        sd.setName("world");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new WorldServer());
    }

    private void generateMap(){
        map = new RoomState[4][4];
        map[0][0] = RoomState.EMPTY;
        map[0][1] = RoomState.BREEZE;
        map[0][2] = RoomState.PIT;
        map[0][3] = RoomState.BREEZE;
        map[1][0] = RoomState.STENCH;
        map[1][1] = RoomState.EMPTY;
        map[1][2] = RoomState.BREEZE;
        map[1][3] = RoomState.EMPTY;
        map[2][0] = RoomState.WUMPUS;
        map[2][1] = RoomState.GOLD;
        map[2][2] = RoomState.PIT;
        map[2][3] = RoomState.BREEZE;
        map[3][0] = RoomState.STENCH;
        map[3][1] = RoomState.EMPTY;
        map[3][2] = RoomState.BREEZE;
        map[3][3] = RoomState.PIT;
    }

    private void printMap(){
        for (int i = 3; i >= 0; i--){
            for (int j = 0; j < map[i].length; j++){
                if (i == playerPosition.x && j == playerPosition.y){
                    System.out.print("[o]");
                }
                else{
                    switch (map[i][j]) {
                        case GOLD:
                            System.out.print("[G]");
                            break;
                        case PIT:
                            System.out.print("[P]");
                            break;
                        case WUMPUS:
                            System.out.print("[W]");
                            break;
                        default:
                            System.out.print("[ ]");
                            break;
                    }
                }
                if (j == 3){
                    System.out.print("\n");
                }
            }
        }
    }

    private boolean movePlayer(Point position){
        if ((position.x - playerPosition.x >= -1 && position.x - playerPosition.x <= 1 && position.y == playerPosition.y)
                || (position.y - playerPosition.y >= -1 && position.y - playerPosition.y <= 1 && position.x == playerPosition.x)){
            playerPosition.x = position.x;
            playerPosition.y = position.y;
            System.out.println("Player has moved to " + playerPosition.x + " " + playerPosition.y);
            printMap();
            return true;
        }
        else{
            return false;
        }
    }

    private class WorldServer extends CyclicBehaviour{
        @Override
        public void action(){
            MessageTemplate messageTemplate = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("player-world"),
                    MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
            ACLMessage message = receive(messageTemplate);
            if (message != null){
                try{
                    Point destination = (Point) message.getContentObject();
                    ACLMessage reply = message.createReply();
                    if (movePlayer(destination)){
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        try{
                            reply.setContentObject(map[playerPosition.x][playerPosition.y]);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    else{
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                    send(reply);

                }
                catch(UnreadableException e){
                    e.printStackTrace();
                }
            }
        }
    }
}