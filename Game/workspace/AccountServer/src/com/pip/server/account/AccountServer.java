package com.pip.server.account;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.pip.net.http.JettyServer;
import com.pip.server.account.dao.AccountCreditDAO;
import com.pip.server.account.dao.AccountDAO;
import com.pip.server.account.dao.FeeDAO;
import com.pip.server.account.dao.LoginInfoDAO;
import com.pip.server.account.dao.PurchasedDAO;
import com.pip.server.account.http.BalanceServlet;
import com.pip.server.account.http.BillingServlet;
import com.pip.server.account.http.Charge2Servlet;
import com.pip.server.account.http.ChargeServlet;
import com.pip.server.account.http.CreateAccountServlet;
import com.pip.server.account.http.CreatePhoneAccountServlet;
import com.pip.server.account.http.ModifyAccountServlet;
import com.pip.server.account.http.ModifyPasswordServlet;
import com.pip.server.account.http.VerifyAccountServlet;
import com.pip.server.account.stub.Sequence2Generator;
import com.pip.server.account.stub.UWAPSocketStub;

public class AccountServer {
	
	private Configuration configuration;
	private CacheManager cacheManager = CacheManager.create();
	private JettyServer jettyServer = null;
	private IStringValidator nameValidator;
	private IStringValidator passwordValidator;
	private IStringValidator phoneValidator;
	private PropertyService propertyService;
	
	private static final Logger log = Logger.getLogger(AccountServer.class);
	
	public AccountServer(Configuration configuration){
		this.configuration = configuration;
	}
	
	public void launch() throws Exception{
		Cache cache = new Cache("AccountEntityCache", 20000, false, true, 0, 0);
		Cache cache2 = new Cache("AccountCreditCache", 20000, false, true, 0, 0);
		cacheManager.addCache(cache);
		cacheManager.addCache(cache2);
		nameValidator = new StringValidator(1, 16, true, new File(System
				.getProperty("user.dir")
				+ "/invalidname.txt"), new File(System.getProperty("user.dir")
				+ "/keywords.xml"));
		passwordValidator = new StringValidator(1,16);
		// phoneValidator = new PatternStringValidator("^13[0-9]{1}[0-9]{8}|^15[0-9]{1}[0-9]{8}");
		// 修改为支持任意数字注册，以允许通过移动平台ID注册为手机号
		phoneValidator = new PatternStringValidator("^[0-9]{6}[0-9]*");
		propertyService = new PropertyService(new File(System
				.getProperty("user.dir")
				+ "/backdoor.txt"), true);
		SourceManager sourceManager = new SourceManager(new File(System
				.getProperty("user.dir")
				+ "/sources.txt"));
		AccountService accountService = new AccountService(new AccountDAO(),
				new LoginInfoDAO(), new PurchasedDAO(), cache, nameValidator,passwordValidator,phoneValidator, 10);
		AccountCreditService accountCreditService = new AccountCreditService(
		        new AccountCreditDAO(), cache2);
		FeeService feeService = new FeeService(new FeeDAO(),accountService);
		accountService.setFeeService(feeService);
		new Sequence2Generator();
		String[] s = configuration.getStringArray("billingtrusted");
		
		Set<String> billingtrusted = new HashSet<String>();
		for(int i=0;i<s.length;i++){
			billingtrusted.add(s[i]);
		}
		String[] s1 = configuration.getStringArray("chargetrusted");
		Set<String> chargetrusted = new HashSet<String>();
		for(int i=0;i<s1.length;i++){
			chargetrusted.add(s1[i]);
		}
		
		if (configuration.getString("guestprefix") != null) {
			UWAPSocketStub.QUICKREG_PREFIX = configuration.getString("guestprefix");
		}

		
		UWAPSocketStub stub = new UWAPSocketStub(sourceManager, accountService, accountCreditService,
				configuration,phoneValidator);
		stub.start();
		log.info("Server Started.");
		jettyServer = new JettyServer(configuration.getString("webip"),configuration.getInt("webport"),1,5);
		jettyServer.addServlet("/QueryFee", new BalanceServlet(accountService));
		jettyServer.addServlet("/billing_if", new BillingServlet(accountService,accountCreditService,feeService,billingtrusted));
		jettyServer.addServlet("/Charge", new ChargeServlet(accountService,feeService,chargetrusted));
		jettyServer.addServlet("/Charge2", new Charge2Servlet(accountService,feeService,chargetrusted));
		jettyServer.addServlet("/VerifyAccount",new VerifyAccountServlet(accountService));
		jettyServer.addServlet("/reg", new CreateAccountServlet(accountService,phoneValidator));
		jettyServer.addServlet("/phonereg", new CreatePhoneAccountServlet(accountService,phoneValidator));
		jettyServer.addServlet("/ModifyPassword", new ModifyPasswordServlet(accountService));
		jettyServer.addServlet("/backdoor", new ModifyAccountServlet(accountService,propertyService));
		jettyServer.start();
		log.info("HttpServer Started.");
	}
	
	public static void main(String[] args) throws Exception{
		Configuration configuration = new PropertiesConfiguration("config.properties");
		AccountServer server = new AccountServer(configuration);
		server.launch();
	}
}
