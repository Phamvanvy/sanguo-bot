package com.pip.itimes.server.world;

import com.pip.itimes.server.stage.Command;

public interface CommandProcessor {
    public void process(WorldPlayer player,Command command) throws Exception;
}
