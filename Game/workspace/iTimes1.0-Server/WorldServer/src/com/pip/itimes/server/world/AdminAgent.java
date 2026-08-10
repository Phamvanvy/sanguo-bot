package com.pip.itimes.server.world;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.pip.itimes.net.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.mina.common.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class AdminAgent {

    private Configuration config;
    private Scheduler scheduler = null;
    private ClientSession cSession = null;


    public AdminAgent(Configuration config) {
        this.config = config;
    }

    public void initScheduler(Reader reader) throws Exception{
        scheduler = new StdSchedulerFactory().getScheduler();
        BufferedReader br = new BufferedReader(reader);
        String s = null;
        int i = 1;
        while((s=br.readLine())!=null){
            if(!addJob(s,scheduler,"admin"+i)){
                System.out.println("scheduler["+s+"]error");
            }
            i++;
        }
    }

    public void initConnect() {
        SessionRegistry registry = new SessionRegistry();
        IoConnector connector = new UWAPConnector(1, Executors.newCachedThreadPool());
        ConnectFuture future = connector.connect(new InetSocketAddress(config.getString("localip"),
                config.getInt("adminport")), new ClientHandler(registry));
        future.join();
    }

    public void startScheduler() throws Exception{
        scheduler.start();
    }

    protected boolean addJob(String s,Scheduler scheduler,String name){
        String[] ss = s.split("\\|");
        if(ss.length==2){
            try {
                JobDetail job = new JobDetail("job", name, AdminJob.class);
                CronTrigger trigger = new CronTrigger("job", name, ss[0]);
                job.getJobDataMap().put("command",ss[1]);
                job.getJobDataMap().put("agent",this);
                scheduler.scheduleJob(job,trigger);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    void sendCommand(String command){
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_COMMAND);
        seg.writeString(command);
        cSession.write(seg);
    }

    public static void main(String[] args) throws Exception{
        Configuration cfg =  new PropertiesConfiguration("config.properties");
        AdminAgent agent = new AdminAgent(cfg);
        agent.initScheduler(new FileReader(System.getProperty("user.dir")+"/adminscheduler"));
        agent.initConnect();
        agent.startScheduler();
    }

    public static class AdminJob implements Job {
        public void execute(JobExecutionContext context) throws
                JobExecutionException {
            String command = (String) context.getJobDetail().
                             getJobDataMap().get("command");
            AdminAgent agent = (AdminAgent) context.getJobDetail().
                               getJobDataMap().get("agent");
            agent.sendCommand(command);
            System.out.println(command);
        }
    }

    class ClientHandler extends SessionHandler{

        public ClientHandler(SessionRegistry registry){
            super(registry);
        }

        public Session createSession(IoSession session){
            cSession = new ClientSession(session);
            return cSession;
        }

    }

    class ClientSession extends Session{

        public ClientSession(IoSession session){
            super(session);
        }

        public void created(){
            sendCommand("login agent agent345");
        }

        public void idle(IdleStatus status){

        }

        public void closed(){
            System.out.println("closed");
        }

        public void handle(Packet packet){
            UWAPData data = packet.datas[0];
            if(data.getAppType() == ServerConstants.ADMIN_COMMAND){
                try{
                    String s = data.readString();
                    System.out.println(s);
                }catch(IllegalAccessException ex){
                }
            }
        }

        public void opened(){
            System.out.println("opened");
        }

    }
}
