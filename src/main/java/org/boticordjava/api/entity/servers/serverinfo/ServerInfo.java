package org.boticordjava.api.entity.servers.serverinfo;

import org.boticordjava.api.entity.servers.Bot;

import java.util.List;

public class ServerInfo {

    private String id;
    private String shortCode;
    private String status;
    private List<String> links;
    private Bot bot;
    private ServerInformation information;

    public String getId() {
        return id;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getLinks() {
        return links;
    }

    public Bot getBot() {
        return bot;
    }

    public ServerInformation getInformation() {
        return information;
    }

    @Override
    public String toString() {
        return "id: " + id +
                "\nshortCode: " + shortCode +
                "\nstatus: " + status +
                "\nlinks: " + links +
                "\nbot: " + bot.toString() +
                "\ninformation: " + information.toString() +
                "\n";
    }
}
