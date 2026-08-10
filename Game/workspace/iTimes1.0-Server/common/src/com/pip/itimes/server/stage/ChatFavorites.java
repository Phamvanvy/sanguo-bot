package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ChatFavorites {
    private final static Map favorites = new TreeMap();

    public static void addFavorite(ChatFavorite favorite){
        favorites.put(new Integer(favorite.id),favorite);
    }

    public static ChatFavorite getFavorite(int id){
        return (ChatFavorite)favorites.get(new Integer(id));
    }

    public static ChatFavorite[] getChatFavorites(){
        ChatFavorite[] ret = new ChatFavorite[favorites.size()];
        favorites.values().toArray(ret);
        return ret;
    }
}
