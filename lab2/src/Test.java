import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Test {

    public static void main(String args[]){
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl("localhost", 8888, "main");
        AgentContainer mainContainer = runtime.createMainContainer(profile);
        try{
            AgentController world = mainContainer.createNewAgent("world", "WorldAgent", new Object[0]);
            AgentController player = mainContainer.createNewAgent("player", "PlayerAgent", new Object[0]);
            AgentController navigator = mainContainer.createNewAgent("navigator", "NavigatorAgent", new Object[0]);
            world.start();
            player.start();
            navigator.start();
        }
        catch (StaleProxyException e){
            e.printStackTrace();
        }
    }
}