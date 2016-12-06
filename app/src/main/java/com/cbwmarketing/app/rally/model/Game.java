package com.cbwmarketing.app.rally.model;

import java.util.List;

/**
 * Created by jcrawford on 7/11/2016.
 */

public class Game {
    private String alias;
    private int challengesnumber;
    private String company;
    private long createddate;
    private long finishdate;
    private long gameid;
    private String name;
    private List<Prize> prizes;
    private List<Resource> resources;
    private long startdate;
    private int status;
    private int timeinminutes;
    private String title;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getChallengesnumber() {
        return challengesnumber;
    }

    public void setChallengesnumber(int challengesnumber) {
        this.challengesnumber = challengesnumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public long getCreateddate() {
        return createddate;
    }

    public void setCreateddate(long createddate) {
        this.createddate = createddate;
    }

    public long getFinishdate() {
        return finishdate;
    }

    public void setFinishdate(long finishdate) {
        this.finishdate = finishdate;
    }

    public long getGameid() {
        return gameid;
    }

    public void setGameid(long gameid) {
        this.gameid = gameid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Prize> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<Prize> prizes) {
        this.prizes = prizes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public long getStartdate() {
        return startdate;
    }

    public void setStartdate(long startdate) {
        this.startdate = startdate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTimeinminutes() {
        return timeinminutes;
    }

    public void setTimeinminutes(int timeinminutes) {
        this.timeinminutes = timeinminutes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
