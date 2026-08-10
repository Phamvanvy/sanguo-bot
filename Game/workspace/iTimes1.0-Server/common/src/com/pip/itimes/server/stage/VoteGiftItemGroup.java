package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

public class VoteGiftItemGroup {
	private String name;
	private VoteGiftItem[] items;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public VoteGiftItemGroup(String name, VoteGiftItem[] items) {
    	this.name = name;
        this.items = items;
    }

    public VoteGiftItem[] getItems() {
        return items;
    }
}
