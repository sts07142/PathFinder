package com.example.getapi;

public class Node {
    String roadName;
    String linkId;
    String startNodeId;
    String endNodeId;
    String speed;
    String travelTime;

    public Node(String roadName,String linkId,String startNodeId, String endNodeId,String speed,String travelTime){
        this.roadName = roadName;
        this.linkId = linkId;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.speed = speed;
        this.travelTime = travelTime;
    }

    public String getRoadName() {
        return roadName;
    }

    public String getEndNodeId() {
        return endNodeId;
    }

    public String getLinkId() {
        return linkId;
    }

    public String getSpeed() {
        return speed;
    }

    public String getStartNodeId() {
        return startNodeId;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public void setEndNodeId(String endNodeId) {
        this.endNodeId = endNodeId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public void setStartNodeId(String startNodeId) {
        this.startNodeId = startNodeId;
    }

    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }
}
