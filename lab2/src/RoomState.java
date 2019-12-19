import java.io.Serializable;

public enum RoomState implements Serializable{
    EMPTY, WUMPUS, GOLD, PIT, BREEZE, STENCH,
    UNKNOWN, VISITED
}