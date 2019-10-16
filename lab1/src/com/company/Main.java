package com.company;


import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "true");
        ContainerController cc = rt.createMainContainer(p);
        AgentController ac;
        try {
            ac=cc.createNewAgent("BookSellerAgent1", "com.company.BookSellerAgent", null);
            ac.start();
            ac=cc.createNewAgent("BookSellerAgent2", "com.company.BookSellerAgent", null);
            ac.start();
            ac=cc.createNewAgent("BookSellerAgent3", "com.company.BookSellerAgent", null);
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
            
        }

    }

}
