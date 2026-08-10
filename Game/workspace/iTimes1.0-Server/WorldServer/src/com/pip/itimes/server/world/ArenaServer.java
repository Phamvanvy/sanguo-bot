package com.pip.itimes.server.world;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;

import com.pip.itimes.net.Session;
import com.pip.itimes.net.SessionHandler;
import com.pip.itimes.net.SessionRegistry;
import com.pip.itimes.net.UWAPAcceptor;
import com.pip.itimes.server.dao.ArenaRecordDao;
import com.pip.itimes.server.dao.ArenaTeamTotalDao;
import com.pip.itimes.server.world.battle.arena.ArenaWorldListManager;
import com.pip.itimes.server.world.battle.arena.server.ArenaServerService;
import com.pip.itimes.server.world.battle.arena.server.ArenaServerSession;
import com.pip.itimes.server.world.battle.arena.server.ArenaService;

public class ArenaServer{
    private static final Logger log = Logger.getLogger(ArenaServer.class);
    public StageService stageService = null;
    public ArenaService arenaService = null;
    public ArenaWorldListManager arenaWorldListManager = null;

    public ArenaServerService arenaServerService;

    public Configuration configuration = null;
    public static ArenaServer instance = null;

    public ArenaServer(){
    }

    SessionRegistry registry = new SessionRegistry();

    public void launch() throws Exception{
        configuration = new PropertiesConfiguration("arena.properties");
        arenaWorldListManager = new ArenaWorldListManager(new File(System.getProperty("user.dir") + "/arenaworlds.txt"));
        stageService = new StageService(new File(configuration.getString("datadir")));
        arenaService = new ArenaService();
        arenaService.setArenaWorldListManager(arenaWorldListManager);
        arenaServerService = new ArenaServerService(new ArenaRecordDao(), new ArenaTeamTotalDao());
        arenaServerService.setArenaService(arenaService);

        if(!configuration.getBoolean("poolbuffer")){
            ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        }
        if(configuration.getBoolean("directbuffer")){
            ByteBuffer.setUseDirectBuffers(true);
        }else{
            ByteBuffer.setUseDirectBuffers(false);
        }

        SessionHandler arenaServerSessionHandler = new ArenaServerSessionHandler(registry);
        bind(registry, arenaServerSessionHandler);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        log.info("ArenaServer started");
    }

    private void bind(SessionRegistry registry, SessionHandler sessionHandler) throws Exception{
        IoAcceptor acceptor = new UWAPAcceptor(2, Executors.newCachedThreadPool());
        DefaultIoFilterChainBuilder filterChainBuilder = acceptor.getDefaultConfig().getFilterChain();
        filterChainBuilder.addLast("threadPool", new ExecutorFilter());
        SocketAcceptorConfig sconfig = (SocketAcceptorConfig) acceptor.getDefaultConfig();
        sconfig.setThreadModel(ThreadModel.MANUAL);
        SocketSessionConfig sc = (SocketSessionConfig) sconfig.getSessionConfig();
        sc.setReceiveBufferSize(configuration.getInt("receivebuffsize"));
        sc.setSendBufferSize(configuration.getInt("writebuffsize"));
        sc.setTcpNoDelay(configuration.getBoolean("tcpnodelay"));
        acceptor.bind(new InetSocketAddress(configuration.getString("localip"), configuration.getInt("port")), sessionHandler, sconfig);
        arenaService.setAcceptor(acceptor);
    }

    public static void main(String[] args){
        try{
            instance = new ArenaServer();
            instance.launch();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    class ArenaServerSessionHandler extends SessionHandler{
        public void exceptionCaught(IoSession session, Throwable ex) throws Exception{
            log.error(ex, ex);
        }

        public Session createSession(IoSession session){
            ArenaServerSession ret = new ArenaServerSession(session);
            ret.setServerService(arenaService);
            ret.setStageService(stageService);
            ret.setArenaServerService(arenaServerService);

            return ret;
        }

        public ArenaServerSessionHandler(SessionRegistry registry){
            super(registry);
        }
    }

    class ShutdownHook extends Thread{
        public void run(){
            arenaService.stop();
        }
    }
}
