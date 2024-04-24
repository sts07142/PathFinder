package com.example.getapi;

import java.util.ArrayList;

public class NodeTable {
    String name;
    ArrayList<Node> nodes = new ArrayList<Node>();

    public NodeTable(String name){
        this.name = name;
    }
    public void addItem(Node node){
        nodes.add(node);
    }
    public String toString(){
        String output = "";
        for(int i = 0; i < nodes.size();i++ ){
            Node node = nodes.get(i);
            output += "Node name "+node.getRoadName() + "\n";
        }
        return  output;
    }
}
