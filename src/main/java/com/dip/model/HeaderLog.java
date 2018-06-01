package com.dip.model;

import java.io.Serializable;

public class HeaderLog implements Serializable {

    /** the variable serialVersionUID */
    private static final long serialVersionUID = -2444260275729780083L;

    /** the variable requester */
    private String requester;

    /** the variable sender */
    private String sender;

    /** the variable receiver */
    private String receiver;

    /** the variable myDipMessageUUID */
    private String myDipMessageUUID;

    /** the variable sentTimestamp */
    private String sentTimestamp;

    /** the variable myDipSenderServerIp */
    private String myDipSenderServerIp;

    /** the variable communicationTemplate */
    private String communicationTemplate;

    /** the variable myDipSenderComponent (DEM or CES) */
    private String myDipSenderComponent;

    /**
     * @param requester as a parameter
     * @param sender as a parameter
     * @param receiver as a parameter
     * @param myDipMessageUUID as a parameter
     * @param sentTimestamp as a parameter
     * @param myDipSenderServerIp as a parameter
     * @param communicationTemplate as a parameter
     * @param myDipSenderComponent as a parameter
     */
    public HeaderLog(String requester, String sender, String receiver, String myDipMessageUUID, String sentTimestamp, String myDipSenderServerIp, String communicationTemplate,
        String myDipSenderComponent) {
        super();
        this.requester = requester;
        this.sender = sender;
        this.receiver = receiver;
        this.myDipMessageUUID = myDipMessageUUID;
        this.sentTimestamp = sentTimestamp;
        this.myDipSenderServerIp = myDipSenderServerIp;
        this.communicationTemplate = communicationTemplate;
        this.myDipSenderComponent = myDipSenderComponent;
    }

    /**
     * @return the requester
     */
    public String getRequester() {
        return requester;
    }

    /**
     * @param requester the requester to set
     */
    public void setRequester(String requester) {
        this.requester = requester;
    }

    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * @return the receiver
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * @return the myDipMessageUUID
     */
    public String getMyDipMessageUUID() {
        return myDipMessageUUID;
    }

    /**
     * @param myDipMessageUUID the myDipMessageUUID to set
     */
    public void setMyDipMessageUUID(String myDipMessageUUID) {
        this.myDipMessageUUID = myDipMessageUUID;
    }

    /**
     * @return the sentTimestamp
     */
    public String getSentTimestamp() {
        return sentTimestamp;
    }

    /**
     * @param sentTimestamp the sentTimestamp to set
     */
    public void setSentTimestamp(String sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    /**
     * @return the myDipSenderServerIp
     */
    public String getMyDipSenderServerIp() {
        return myDipSenderServerIp;
    }

    /**
     * @param myDipSenderServerIp the myDipSenderServerIp to set
     */
    public void setMyDipSenderServerIp(String myDipSenderServerIp) {
        this.myDipSenderServerIp = myDipSenderServerIp;
    }

    /**
     * @return the communicationTemplate
     */
    public String getCommunicationTemplate() {
        return communicationTemplate;
    }

    /**
     * @param communicationTemplate the communicationTemplate to set
     */
    public void setCommunicationTemplate(String communicationTemplate) {
        this.communicationTemplate = communicationTemplate;
    }

    /**
     * @return the myDipSenderComponent
     */
    public String getMyDipSenderComponent() {
        return myDipSenderComponent;
    }

    /**
     * @param myDipSenderComponent the myDipSenderComponent to set
     */
    public void setMyDipSenderComponent(String myDipSenderComponent) {
        this.myDipSenderComponent = myDipSenderComponent;
    }

    @Override
    public String toString() {
        String header = "";
        if (requester != null && !requester.isEmpty()) {
            header = "requestor=" + requester + ", ";
        } else {
            header = "requestor=-,";
        }
        if (sender != null && !sender.isEmpty()) {
            header = header + "sender=" + sender.replaceAll(" ", "-") + ", ";
        }
        if (receiver != null && !receiver.isEmpty()) {
            header = header + "receiver=" + receiver + ", ";
        }
        if (myDipMessageUUID != null && !myDipMessageUUID.isEmpty()) {
            header = header + "myDipMessageUUID=" + myDipMessageUUID + ", ";
        }
        if (sentTimestamp != null && !sentTimestamp.isEmpty()) {
            header = header + "sentTimestamp=" + sentTimestamp + ", ";
        }
        if (myDipSenderServerIp != null && !myDipSenderServerIp.isEmpty()) {
            header = header + "myDipSenderServerIp=" + myDipSenderServerIp + ", ";
        }
        if (communicationTemplate != null && !communicationTemplate.isEmpty()) {
            header = header + "communicationTemplate=" + communicationTemplate + ", ";
        }
        if (myDipSenderComponent != null && !myDipSenderComponent.isEmpty()) {
            header = header + "myDipSenderComponent=" + myDipSenderComponent;
        }
        return header;
    }
}