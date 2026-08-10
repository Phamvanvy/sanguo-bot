package peony.service.jetty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.thread.QueuedThreadPool;

import peony.service.Service;

public class JettyService implements Service{
	
	private static final Logger log = Logger.getLogger(JettyService.class);
	
	protected Configuration config;
	
	protected Server server;
	protected Context root;
	
	protected Map<String,String> urls = new HashMap<String,String>();
	
	public JettyService(Configuration config){
		this.config = config;
	}

	public void shutdown() {
		
	}

	@SuppressWarnings("unchecked")
	public void startup() throws Exception {
		if(config==null){
			log.info("Jetty not found.");
		}else{
			String ip = config.getString("ip");
			int port = config.getInt("port");
			initServer(ip,port);

			List<Configuration> l = ((SubnodeConfiguration)config).configurationsAt("servlet");
			for(Configuration c:l){
				String url = c.getString("url");
				String clazz = c.getString("class");
				root.addServlet(Class.forName(clazz), "/"+url);
				String s = "http://"+ip+":"+port+"/"+url;
				urls.put(url, s);
				log.info("Servlet["+clazz+"]["+s+"]");
			}
			server.start();
			log.info("Jetty Started");
		}
	}
	
	public String getUrl(String name){
		return urls.get(name);
	}
	
	protected void initServer(String ip,int port){
        server = new Server();
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(1);
        threadPool.setMaxThreads(16);
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort (port);
        connector.setHost (ip);
        root = new Context(server, "/", Context.SESSIONS);
        server.addConnector(connector);
	}
}
