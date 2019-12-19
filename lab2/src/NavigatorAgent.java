import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

public class NavigatorAgent extends Agent {

    private RoomState[][] map;
    private Point playerPosition;
    private Point nextPosition;

    @Override
    protected void setup() {
        playerPosition = new Point();
        nextPosition = new Point(0, 0);
        map = new RoomState[4][4];

        for (int i = 0; i < map.length; i++){
            for (int j = 0; j < map[i].length; j++){
                map[i][j] = RoomState.UNKNOWN;
            }
        }

        addBehaviour(new NavigatorServer());
    }

    private Point getNextPosition(RoomState newState){
        assignNewStates(newState);
        return getBestNeighbour(playerPosition);
    }

    private void assignNewStates(RoomState newState){
        map[playerPosition.x][playerPosition.y] = RoomState.VISITED;

        ArrayList<Point> neighbours = new ArrayList<>();
        if (playerPosition.x != 0) neighbours.add(new Point(playerPosition.x - 1, playerPosition.y));
        if (playerPosition.x < 3) neighbours.add(new Point(playerPosition.x + 1, playerPosition.y));
        if (playerPosition.y != 0) neighbours.add(new Point(playerPosition.x, playerPosition.y - 1));
        if (playerPosition.y < 3) neighbours.add(new Point(playerPosition.x, playerPosition.y + 1));
        ListIterator<Point> iterator = neighbours.listIterator();

        switch (newState) {
            case EMPTY:
                while (iterator.hasNext()){
                    Point point = iterator.next();
                    if (map[point.x][point.y] != RoomState.VISITED) map[point.x][point.y] = RoomState.EMPTY;
                }
                break;
            case BREEZE:
                while (iterator.hasNext()){
                    Point point = iterator.next();
                    if (map[point.x][point.y] != RoomState.VISITED) {
                        if (map[point.x][point.y] == RoomState.UNKNOWN
                                || map[point.x][point.y] == RoomState.PIT){
                            map[point.x][point.y] = RoomState.PIT;
                        }
                        else map[point.x][point.y] = RoomState.EMPTY;
                    }
                }
                break;
            case STENCH:
                while (iterator.hasNext()){
                    Point point = iterator.next();
                    if (map[point.x][point.y] != RoomState.VISITED) {
                        if (map[point.x][point.y] == RoomState.UNKNOWN
                                || map[point.x][point.y] == RoomState.WUMPUS){
                            map[point.x][point.y] = RoomState.WUMPUS;
                        }
                        else map[point.x][point.y] = RoomState.EMPTY;
                    }
                }
                break;
        }
    }

    private Point getBestNeighbour(Point room){
        Point result = new Point();
        ArrayList<Point> neighbours = new ArrayList<>();
        ArrayList<Point> candidates = new ArrayList<>();
        if (room.x != 0) neighbours.add(new Point(room.x - 1, room.y));
        if (room.x < 3) neighbours.add(new Point(room.x + 1, room.y));
        if (room.y != 0) neighbours.add(new Point(room.x, room.y - 1));
        if (room.y < 3) neighbours.add(new Point(room.x, room.y + 1));
        ListIterator<Point> iterator = neighbours.listIterator();

        while (iterator.hasNext()){
            Point point = iterator.next();
            if (map[point.x][point.y] == RoomState.EMPTY) {
                candidates.add(point);
            }
        }
        if (candidates.size() == 1){
            result = candidates.get(0);
        }
        else if (candidates.size() > 1){
            Random random = new Random();
            result = candidates.get(random.nextInt(candidates.size()));
        }
        else {
            iterator = neighbours.listIterator();
            while (iterator.hasNext()){
                Point point = iterator.next();
                if (map[point.x][point.y] == RoomState.VISITED) {
                    if (getBestNeighbour(point) != null)
                        candidates.add(point);
                }
            }
            if (candidates.size() == 1){
                result = candidates.get(0);
            }
            else if (candidates.size() > 1){
                Random random = new Random();
                result = candidates.get(random.nextInt(candidates.size()));
            }
            else return null;
        }
        return result;
    }

    private class NavigatorServer extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("player-navigator"),
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
            ACLMessage message = receive(messageTemplate);
            if (message != null){
                playerPosition = nextPosition;
                try{
                    RoomState newState = (RoomState) message.getContentObject();
                    nextPosition = getNextPosition(newState);
                    ACLMessage responseToPlayer = message.createReply();
                    try{
                        responseToPlayer.setContentObject(nextPosition);
                        send(responseToPlayer);
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