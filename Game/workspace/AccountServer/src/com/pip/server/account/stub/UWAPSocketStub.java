package com.pip.server.account.stub;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.net.message.ServerMessageType;
import com.pip.net.message.gameaccount.GameAccountMessageType;
import com.pip.net.uwap2.mina.Packet;
import com.pip.net.uwap2.mina.UWAPData;
import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;
import com.pip.net.uwap2.mina.UWAPSegment;
import com.pip.server.account.AccountCreditService;
import com.pip.server.account.AccountEntity;
import com.pip.server.account.AccountService;
import com.pip.server.account.BalanceException;
import com.pip.server.account.BaseAccountException;
import com.pip.server.account.ChangeStatusException;
import com.pip.server.account.CreateAccountException;
import com.pip.server.account.Errors;
import com.pip.server.account.IAccountServiceListener;
import com.pip.server.account.ISource;
import com.pip.server.account.ISourceManager;
import com.pip.server.account.IStringValidator;
import com.pip.server.account.LoginException;
import com.pip.server.account.LoginResult;
import com.pip.server.account.LogoutException;
import com.pip.server.account.ModifyException;
import com.pip.server.account.NumberStringValidator;
import com.pip.server.account.ISource.Status;
import com.pip.server.account.bean.Balance;
import com.pip.server.account.bean.IMoneyCard;
import com.pip.server.account.bean.RecommendRequest;
import com.pip.server.account.bean.RecommendReward;
import com.pip.server.account.dao.IMoneyCardDAO;
import com.pip.server.account.dao.RecommendRequestDAO;

public class UWAPSocketStub implements IAccountServiceListener {
	
	private final ISourceManager manager;
	private IoAcceptor acceptor;
	private final AccountService service;
	private final AccountCreditService creditService;
	private final IStringValidator numberStringValidator = new NumberStringValidator();
	private final IStringValidator phoneValidator;
	
	private int receiveBufferSize = 32767;
	private int sendBufferSize = 32767;
	
	private final Configuration configuration;
	
	private final Logger log = Logger.getLogger(UWAPSocketStub.class);
	
	private final SessionFactory sf = HibernateUtil.getSessionFactory();
	
	private Vector<IoSession> sessions = new Vector<IoSession>();
	
	public static String QUICKREG_PREFIX = "游客";
	
	public UWAPSocketStub(ISourceManager manager,AccountService service, AccountCreditService cs,
	        Configuration configuration,IStringValidator phoneValidator){
		this.manager = manager;
		this.configuration = configuration;
		this.service = service;
		this.creditService = cs;
		this.phoneValidator = phoneValidator;
		this.service.setListener(this);
	}
	
	public void start() throws IOException{
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
		acceptor = new SocketAcceptor(1, Executors.newCachedThreadPool());
		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		cfg.setThreadModel(ThreadModel.MANUAL);
		cfg.setDisconnectOnUnbind(true);
		cfg.getSessionConfig().setTcpNoDelay(true);
		cfg.getSessionConfig().setReuseAddress(true);
		if (configuration.containsKey("receivebuffersize")) {
			receiveBufferSize = configuration.getInt("receivebuffersize");
		}
		if(configuration.containsKey("sendbuffersize")){
			sendBufferSize = configuration.getInt("sendbuffersize");
		}
		cfg.getSessionConfig().setReceiveBufferSize(receiveBufferSize);
		cfg.getSessionConfig().setSendBufferSize(sendBufferSize);
		cfg.getFilterChain().addLast("codec",new ProtocolCodecFilter(new UWAPEncoder(),new UWAPDecoder()));
		acceptor.bind(new InetSocketAddress(configuration.getString("serverip"),configuration.getInt("port")), new ClientSessionHandler(), cfg);
	}
	
	public void addSession(IoSession session) {
		for (int i = 0; i < sessions.size(); i++) {
			if (sessions.get(i) == session) {
				return;
			}
		}
		sessions.add(session);
	}
	
	public void removeSession(IoSession session) {
		for (int i = 0; i < sessions.size(); i++) {
			if (sessions.get(i) == session) {
				sessions.remove(i);
				i--;
			}
		}
	}
	
	public IoSession[] getSessions() {
		IoSession[] ret = new IoSession[sessions.size()];
		sessions.toArray(ret);
		return ret;
	}
	
	public void broadcast(UWAPSegment seg) {
		for (IoSession session : sessions) {
			session.write(seg);
		}
	}
	
	public void accountBalanceChanged(AccountEntity entity) {
		UWAPSegment seg = new UWAPSegment(GameAccountMessageType.SYNC_BALANCE);
		seg.writeInt(entity.getId());
		seg.writeInt((int)entity.getBalance().getValue());
		seg.writeBoolean(false);
		seg.writeBoolean(false);
		seg.writeLong(entity.getBalance().getValue());
		broadcast(seg);
	}
	
	class ClientSessionHandler extends IoHandlerAdapter{

		@Override
		public void exceptionCaught(IoSession session, Throwable cause)
				throws Exception {
			log.info(cause,cause);
		}

		@Override
		public void messageReceived(IoSession session, Object message)
				throws Exception {
			Packet packet = (Packet)message;
			UWAPData data = packet.datas[0];
			short type = data.getAppType();
			switch(type){
			case ServerMessageType.LOGIN:
				serverLogin(session,data);
				break;
			case GameAccountMessageType.LEGACY_LOGIN:
				login(session,data);
				break;
			case GameAccountMessageType.BUY:
				buy(session,data);
				break;
//			case UWAPType.IMONEY_NOSESSION:
//				imoneyNoSession(session,data);
//				break;
			case GameAccountMessageType.ACCOUNT_REG:
				reg(session,data);
				break;
			case GameAccountMessageType.LEGACY_QUICKREG:
				legacyQuickReg(session,data);
				break;
			case GameAccountMessageType._LOGOUT:
				logout(session,data);
				break;
			case GameAccountMessageType.DEC_BALANCE:
				decBalance(session,data);
				break;
			case GameAccountMessageType.MODIFY_PASSWORD:
				modifyPassword(session,data);
				break;
			case GameAccountMessageType.MODIFY_PHONE:
				modifyPhone(session,data);
				break;
			case GameAccountMessageType.ADD_RECOMMEND_BALANCE:
				addRecommendBalance(session,data);
				break;
			case GameAccountMessageType._CHANGE_STATUS:
				changeStatus(session,data);
				break;
			case GameAccountMessageType._ACCOUNT_INFO:
				accountInfo(session,data);
				break;
			case GameAccountMessageType.QUERY_BALANCE:
				queryBalance(session,data);
				break;
			case GameAccountMessageType.MODIFY_PASSWORD2:
				modifyPassword2(session,data);
				break;
			case GameAccountMessageType.MODIFY_PHONE2:
				modifyPhone2(session,data);
				break;
			case GameAccountMessageType.NUMERICAL_REG:
				numericalReg(session,data);
				break;
			case GameAccountMessageType.RESET_PASSWORD:
				resetPassword(session,data);
				break;
			case GameAccountMessageType.DEC_BALANCE2:
				decBalance2(session,data);
				break;
//			case GameAccountMessageType.PHONE_REG:
//				phoneReg(session,data);
//				break;
			case GameAccountMessageType.DEC_CBALANCE:
				decCbalance(session,data);
				break;
			case GameAccountMessageType.QUERY_ABC_BALANCE:
				queryABCbalance(session,data);
				break;
			case GameAccountMessageType.PHONEACCOUNT_LOGIN:
				phoneAccountLogin(session,data);
				break;
			case GameAccountMessageType.RENAME:
				rename(session,data);
				break;
			case GameAccountMessageType.ONLINE_TIME_NOTIFY:
			    onlineTimeNotify(session,data);
			    break;
			case GameAccountMessageType.RECOMMEND_REQUEST:
			    recommendRequest(session,data);
			    break;
			case GameAccountMessageType.LEVEL_UP_NOTIFY:
			    levelUpNotify(session, data);
			    break;
			case GameAccountMessageType.CREATE_IMONEY_CARD:
				createIMoneyCard(session, data);
				break;
			case GameAccountMessageType.USE_IMONEY_CARD:
				useIMoneyCard(session, data);
				break;
			case GameAccountMessageType.ADD_BALANCE:
				addBalance(session, data);
				break;
			case GameAccountMessageType.PHONE_NOTIFY:
				phoneNotify(session, data);
				break;
			}
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			ISource source = (ISource) session.getAttribute("ISource");;
			if(source!=null&&source.getStatus()==Status.valid){
				source.setStatus(Status.disconnected);
				session.removeAttribute("ISource");
			}
			log.info("Server["+session.getRemoteAddress()+"]Disconnected");
			removeSession(session);
		}

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			log.info("Server["+session.getRemoteAddress()+"]Connected");
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status)
				throws Exception {
		}

		private void phoneNotify(IoSession session, UWAPData data) throws Exception {
			ISource source = (ISource) session.getAttribute("ISource");
			if (source!=null) {
			    int accID = data.readInt();
			    String phone = data.readString();
			    log.info("ID[" + accID + "]PHONE[" + phone + "]");
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					service.updatePhone(accID, phone);
				} catch (BalanceException e) {
					tx.rollback();
				}
			}
		}
		
		private void addBalance(IoSession session, UWAPData data) throws Exception {
			ISource source = (ISource) session.getAttribute("ISource");
			if (source!=null) {
			    int accID = data.readInt();
			    int value = data.readInt();
			    String reason = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					long balance = service.forceAddBalance(accID, value, reason);
					tx.commit();
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.ADD_BALANCE_OK,data.getSerial());
					seg.writeInt(accID);
					seg.writeInt(value);
					session.write(seg);
					log.info("ID["+accID+"]Value["+value+"]Balance[" + balance + "]AddBalanceOK");
				}catch(BalanceException e){
					tx.rollback();
					log.info("ID["+accID+"]Value["+value+"]AddBalanceFail");
				}
			}
		}
				
		private void useIMoneyCard(IoSession session, UWAPData data) throws Exception {
			ISource source = checkSource(session);
            if (source != null) {
            	String gamecode = data.readString();
            	int accountID = data.readInt();
            	String sessionId = data.readString();
            	String cardno = data.readString();
            	String password = data.readString();
				log.info("[USE_IMONEY_CARD]gamecode[" + gamecode + "]AccountID[" + accountID + "]cardno["
						+ cardno + "]password[" + password + "]TRY");
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
	            	AccountEntity ae = service.getAccountEntity(accountID);
	            	if (ae == null) {
	            		throw new BalanceException(Errors.UNKNOW_ACCOUNT);
	            	}
	            	
	            	// 检查使用i币卡
	            	IMoneyCardDAO dao = new IMoneyCardDAO();
	            	IMoneyCard card = dao.useCard(gamecode, accountID, cardno, password);
	            	if (card == null) {
	            		throw new BalanceException(Errors.ILLEGAL_IMONEY_CARD);
	            	}
	            	
	            	// 卡使用成功，增加i币余额
	            	service.addBBalance(ae.getName(), card.getAmount());
					tx.commit();
					
					log.info("[USE_IMONEY_CARD]gamecode[" + gamecode + "]AccountID[" + accountID + "]cardno["
							+ cardno + "]password[" + password + "]Amount[" + card.getAmount() + "]Balance[" + ae.getBalance().getValue() + "]OK");
					
					// 发送返回包
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.USE_IMONEY_CARD_OK, data.getSerial(), data.getSessionId());
					seg.writeInt(accountID);
					seg.writeInt(card.getAmount());
					seg.writeInt((int)ae.getBalance().getValue());
					seg.writeLong(ae.getBalance().getValue());
					session.write(seg);
				} catch (BalanceException e) {
					log.info("[USE_IMONEY_CARD]gamecode[" + gamecode + "]AccountID[" + accountID + "]cardno["
							+ cardno + "]password[" + password + "]ERROR");
					tx.rollback();
					exceptionReply(session, e, data.getSerial());
				} 

            }
		}
		
		private void createIMoneyCard(IoSession session, UWAPData data) throws Exception {
			ISource source = checkSource(session);
            if (source != null) {
            	String gamecode = data.readString();
            	int accountID = data.readInt();
            	String sessionId = data.readString();
            	int amount = data.readInt();
				log.info("[CREATE_IMONEY_CARD]gamecode[" + gamecode + "]AccountID[" + accountID + "]Amount["
						+ amount + "]TRY");
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
	            	AccountEntity ae = service.getAccountEntity(accountID);
	            	if (ae == null) {
	            		throw new BalanceException(Errors.UNKNOW_ACCOUNT);
	            	}
	            	
	            	// 扣除余额并创建i币卡
					Balance[] bs = service.decBBalance(ae.getName(), sessionId, source.getId(), amount);
					IMoneyCardDAO dao = new IMoneyCardDAO();
					IMoneyCard card = dao.generateCard(gamecode, accountID, amount);
					tx.commit();
					
					log.info("[CREATE_IMONEY_CARD]gamecode[" + gamecode + "]AccountID[" + accountID + "]Amount["
							+ amount + "]cardno[" + card.getCardno() + "]Balance[" + ae.getBalance().getValue() + "]OK");
					
					// 发送返回包
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.CREATE_IMONEY_CARD_OK, data.getSerial(), data.getSessionId());
					seg.writeInt(accountID);
					seg.writeInt((int)(bs[0].getValue() - bs[1].getValue()));
					seg.writeInt((int)bs[1].getValue());
					seg.writeString(card.getCardno());
					seg.writeString(card.getPassword());
					seg.writeLong(bs[1].getValue());
					session.write(seg);
				} catch (BalanceException e) {
					log.info("[CREATE_IMONEY_CARD]gamecode[" + gamecode + "]AccountID[" + accountID + "]Amount["
							+ amount + "]ERROR");
					tx.rollback();
					exceptionReply(session, e, data.getSerial());
				} 

            }
		}
	
		private void levelUpNotify(IoSession session, UWAPData data) throws Exception {
		    ISource source = checkSource(session);
            if (source != null) {
                int accountID = data.readInt();
                int playerID = data.readInt();
                int level = data.readInt();
                String gamecode = data.readString();
                Transaction tx = sf.getCurrentSession().beginTransaction();
                try {
                    RecommendReward rr = service.checkRecommend(accountID, gamecode, playerID, level);
                    tx.commit();
                    if (rr != null) {
                        UWAPSegment seg = new UWAPSegment(GameAccountMessageType.RECOMMEND_REWARD_NOTIFY, data.getSerial());
                        seg.writeInt(accountID);
                        seg.writeInt(playerID);
                        seg.writeInt(level);
                        seg.writeString(gamecode);
                        seg.writeInt(rr.getGuestRewardValue());
                        seg.writeInt(rr.getOwnerID());
                        seg.writeInt(rr.getOwnerRewardValue());
                        session.write(seg);
                    }
                } catch (Exception e) {
                    log.error(e, e);
                    tx.rollback();
                }
            }
		}
		
		private void recommendRequest(IoSession session,UWAPData data) throws Exception {
		    ISource source = checkSource(session);
            if (source != null) {
                int accountID = data.readInt();
                String phone = data.readString();
                String gamecode = data.readString();
                Transaction tx = sf.getCurrentSession().beginTransaction();
                try {
                    RecommendRequest rr = new RecommendRequest();
                    rr.setAccount(accountID);
                    rr.setGameCode(gamecode);
                    rr.setRecTime(new Date());
                    rr.setTargetPhone(phone);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(rr.getRecTime());
                    cal.add(Calendar.DATE, 3);
                    rr.setValidTime(cal.getTime());
                    new RecommendRequestDAO().add(rr);
                    tx.commit();
                } catch (Exception e) {
                    log.error(e, e);
                    tx.rollback();
                }
            }
		}
		
        private void onlineTimeNotify(IoSession session,UWAPData data) throws Exception{
            ISource source = checkSource(session);
            if (source != null) {
                int accountID = data.readInt();
                int duration = data.readInt();
                Transaction tx = sf.getCurrentSession().beginTransaction();
                try {
                    creditService.addTimeCredit(accountID, duration);
                    tx.commit();
                } catch (Exception e) {
                    log.error(e, e);
                    tx.rollback();
                }
            }
        }

		private void rename(IoSession session,UWAPData data) throws Exception{
			ISource source = checkSource(session);
			if (source != null) {
				String oldName = data.readString().trim();
				String newName = data.readString().trim();
				log.info("Name["+oldName+"]NewName["+newName+"]TryRename");
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					if(newName.startsWith(QUICKREG_PREFIX)){
						throw new CreateAccountException(Errors.ILLEGAL_NAME_CHAR);
					}
					if (numberStringValidator.valid(newName) == IStringValidator.OK) { // 不允许注册纯数字帐号
						if (phoneValidator.valid(newName) != IStringValidator.OK) //允许用手机号注册
							throw new CreateAccountException(
									Errors.ILLEGAL_NAME_CHAR);
					}
					service.rename(oldName, newName);
					tx.commit();
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.RENAME_OK, data
									.getSerial());
					session.write(seg);
					log.info("Name["+oldName+"]NewName["+newName+"]RenameOk");
				}catch(CreateAccountException ex){
					log.error(ex, ex);
					tx.rollback();
					exceptionReply(session, ex, data.getSerial());
				}
				
			}
		}
		
		private void phoneAccountLogin(IoSession session, UWAPData data) throws Exception {
			ISource source = checkSource(session);
			if (source != null) {
				String name = data.readString().trim();
				String password = data.readString();
				log.info("Name["+name+"]Password["+password+"]TryPhoneLogin");
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					LoginResult r = service.login(name, password, name, source.getId());
					tx.commit();
					log.info("ID[" + r.getAccountEntity().getId() + "]Name["
							+ r.getAccountEntity().getName() + "]Key["
							+ r.getLoginInfo().getSessionId() + "]Service["
							+ r.getLoginInfo().getServiceId() + "]PhoneLogined");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.PHONEACCOUNT_LOGIN_OK, data
									.getSerial());
					seg.writeInt(r.getAccountEntity().getId());
					seg.writeString(r.getAccountEntity().getName());
					seg.writeString(r.getLoginInfo().getSessionId());
					seg.writeString(r.getAccountEntity().getPhone());
					seg.writeInt(0);
					seg.writeInt((int)r.getAccountEntity().getBalance().getValue());
					seg.writeInt(r.getAccountEntity().getAccount().getModifyPasswordTimes());
					seg.writeInts(r.getAccountEntity().getPurchasedCodes());
					seg.writeInt(r.getAccountEntity().getCbalance());
					seg.writeInt(r.getAccountEntity().getRegType());
					seg.writeLong(r.getAccountEntity().getBalance().getValue());
					session.write(seg);
				} catch (LoginException ex) {
					log.error(ex, ex);
					tx.rollback();
					exceptionReply(session, ex, data.getSerial());
				} 
			}
		}
		
		private void queryABCbalance(IoSession session, UWAPData data)
				throws Exception {
			ISource source = (ISource) session.getAttribute("ISource");
			if (source != null) {
				String name = data.readString().trim();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					AccountEntity ae = service.getAccountEntity(name);
					tx.commit();
					if (ae != null) {
						UWAPSegment seg = new UWAPSegment(
								GameAccountMessageType.QUERY_ABC_BALANCE_OK, data
										.getSerial());
						seg.writeInt((int)ae.getBalance().getValue());
						seg.writeInt(ae.getCbalance());
						seg.writeInts(ae.getPurchasedCodes());
						seg.writeLong(ae.getBalance().getValue());
						session.write(seg);
					}
				} catch (Exception e) {
					tx.rollback();
				}
			}
		}
		
		private void decCbalance(IoSession session,UWAPData data) throws Exception{
			ISource source = checkSource(session);
			if (source != null) {
				String name = data.readString().trim();
				int v = data.readInt();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					int balance = service.decCbalance(name, v);
					tx.commit();
					log.info("Name[" + name + "]Value[" + v + "]"+"CurrentCBalance[" + balance
							+ "]DecOk");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.DEC_CBALANCE_OK, data
									.getSerial(), data.getSessionId());
					seg.writeInt(balance);
					session.write(seg);
				} catch (BalanceException e) {
//					e.printStackTrace();
					tx.rollback();
					exceptionReply(session, e, data.getSerial());
				} 
			}			
		}
		
//		private void phoneReg(IoSession session,UWAPData data) throws Exception{
//			ISource source = (ISource) session.getAttribute("ISource");
//			if (source != null) {
//				String name = data.readString();
//				String password = data.readString();
//				int balance = data.readInt();
//				int cbalance = data.readInt();
//				int regType = data.readInt();
//				String model = data.readString();
//				String gamecode = data.readString();
//				String versionString = data.readString();
//				String[] versions = versionString.split("-");
//				String versionPatch = null;
//				if(versions.length==3){
//					versionString = versions[0]+"-"+versions[1];
//					versionPatch = versions[2];
//				}
//				Transaction tx = sf.getCurrentSession().beginTransaction();
//				try {
//
//					if (phoneValidator.valid(name) != IStringValidator.OK) // 允许用手机号注册
//						throw new CreateAccountException(
//								Errors.ILLEGAL_NAME_CHAR);
//
//					AccountEntity ae = service.registerWithPassword(name,
//							password, balance, cbalance, gamecode,
//							versionString, "", model, versionPatch, regType);
//					tx.commit();
//					log.info("ID[" + ae.getId() + "]Name[" + ae.getName()
//							+ "]GameCode["
//							+ ae.getAccount().getCreateGameCode() + "]cbalance["+cbalance+"]REGOK");
//					UWAPSegment seg = new UWAPSegment(
//							GameAccountMessageType.ACCOUNT_REG_OK, data
//									.getSerial());
//					seg.writeInt(ae.getId());
//					seg.writeString(ae.getName());
//					seg.writeString(ae.getPassWord());
//					session.write(seg);
//				} catch (CreateAccountException e) {
////					e.printStackTrace();
//					tx.rollback();
//					exceptionReply(session, e, data.getSerial());
//				} 
//			}			
//		}
		
		private void resetPassword(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if (source != null) {
				String name = data.readString().trim();
				String phone = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					AccountEntity ae = service.resetPassword(name, phone);
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.RESET_PASSWORD_OK, data
									.getSerial());
					seg.writeInt(ae.getId());
					seg.writeString(ae.getName());
					seg.writeString(ae.getPassWord());
					session.write(seg);
					tx.commit();
				}catch(ModifyException e){
				    log.error(e, e);
					tx.rollback();
					exceptionReply(session, e, data.getSerial());					
				}
			}
		}
		
		private void numericalReg(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if (source != null) {
//				String name = data.readString();
				String phone = data.readString();
				String recommend = data.readString();
				int recommendId = data.readInt();
				String model = data.readString();
				String gamecode = data.readString();
				String versionString = data.readString();
				String[] versions = versionString.split("-");
				String versionPatch = null;
				if(versions.length==3){
					versionString = versions[0]+"-"+versions[1];
					versionPatch = versions[2];
				}
				// 新版本协议可带上一个手机号
				String loginPhone = "";
				try {
				    loginPhone = data.readString();
				} catch (Exception e) {
				}
				
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
//					if(name.startsWith(QUICKREG_PREFIX)){
//						throw new CreateAccountException(Errors.ILLEGAL_NAME_CHAR);
//					}
//					if(numberStringValidator.valid(name)!=IStringValidator.OK){ //不允许注册纯数字帐号
//						throw new CreateAccountException(Errors.ILLEGAL_NAME_CHAR);
//					}
					int id = Sequence2Generator.getNextId(gamecode);
					AccountEntity ae = service.register(id+"", "", "", source.getBalance(),
							gamecode, phone, recommend, versionString, "",
							model,versionPatch, loginPhone);
					tx.commit();
					log.info("ID[" + ae.getId() + "]Name[" + ae.getName()
							+ "]GameCode["
							+ ae.getAccount().getCreateGameCode() + "]NUMERICALREGOK");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.NUMERICAL_REG_OK, data
									.getSerial());
					seg.writeInt(ae.getId());
					seg.writeString(ae.getName());
					seg.writeString(ae.getPassWord());
					session.write(seg);
				} catch (CreateAccountException e) {
				    log.error(e, e);
					tx.rollback();
					exceptionReply(session, e, data.getSerial());
				} 
			}			
		}

		private void queryBalance(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if(source!=null){
				String name = data.readString().trim();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					AccountEntity ae = service.getAccountEntity(name);
					tx.commit();
					if(ae!=null){
						UWAPSegment seg = new UWAPSegment(GameAccountMessageType.QUERY_BALANCE_OK,data.getSerial());
						seg.writeInt(ae.getId());
						seg.writeString(ae.getName());
						seg.writeInt((int)ae.getBalance().getValue());
						seg.writeInts(ae.getPurchasedCodes());
						seg.writeLong(ae.getBalance().getValue());
						session.write(seg);
					}
				}catch(Exception e){
					tx.rollback();
				}
			}			
		}
		
		private void accountInfo(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if(source!=null){
				String name = data.readString().trim();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					AccountEntity ae = service.getAccountEntity(name);
					tx.commit();
					if(ae!=null){
						UWAPSegment seg = new UWAPSegment(GameAccountMessageType.ACCOUNT_INFO_OK,data.getSerial());
						seg.writeInt(ae.getId());
						seg.writeString(ae.getName());
						seg.writeString(ae.getPassWord());
						seg.writeString(ae.getPhone());
						session.write(seg);
					}
				}catch(Exception e){
					tx.rollback();
				}
			}			
		}
		
		private void addRecommendBalance(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if(source!=null){
			    int accID = data.readInt();
			    int value = data.readInt();
			    int value2 = data.readInt();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					int[] info = service.addRecommendBalance(accID, value, value2);
					tx.commit();
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.ADD_RECOMMEND_BALANCE_OK,data.getSerial());
					if (info == null) {
					    seg.writeBoolean(false);
					    seg.writeInt(accID);
					    seg.writeInt(0);
					    seg.writeInt(-1);
					    seg.writeInt(0);
					} else {
					    seg.writeBoolean(true);
                        seg.writeInt(info[0]);
                        seg.writeInt(info[1]);
                        seg.writeInt(info[2]);
                        seg.writeInt(info[3]);
					}
					session.write(seg);
					log.info("ID["+accID+"]Value["+value+"," + value2 + "]RecommendBalanceAdded");
				}catch(BalanceException e){
					tx.rollback();
					log.info("ID["+accID+"]Value["+value+"," + value2 + "]RecommendBalanceAddFail");
				}
			}
		}

		private void modifyPhone2(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if(source!=null){
				String name = data.readString().trim();
				String oldPhone = data.readString();
				String phone = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					service.modifyPhone(name, oldPhone, phone);
					tx.commit();
					log.info("Name["+name+"]Phone["+phone+"]Modified");
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.MODIFY_PHONE2_OK,data.getSerial());
					session.write(seg);
				}catch(ModifyException e){
					tx.rollback();
					exceptionReply(session,e,data.getSerial());
				}
			}			
		}
		
		
		private void modifyPhone(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if(source!=null){
				String name = data.readString().trim();
				String key = data.readString();
				String phone = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					service.modifyPhone(name, key, phone, phone);
					tx.commit();
					log.info("Name["+name+"]Key["+key+"]Phone["+phone+"]Modified");
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.MODIFY_PHONE_OK,data.getSerial());
					session.write(seg);
				}catch(ModifyException e){
					tx.rollback();
					exceptionReply(session,e,data.getSerial());
				}
			}			
		}

		
		
		private void modifyPassword2(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if(source!=null){
				String name = data.readString().trim();
				String oldPassword = data.readString();
				String password = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					service.modifyPassword(name,oldPassword,password);
					tx.commit();
					log.info("Name["+name+"]OldPassword["+oldPassword+"]CurrentPassword["+password+"]Modified");
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.MODIFY_PASSWORD2_OK,data.getSerial());
					session.write(seg);					
				}catch(ModifyException e){
					log.info("Name["+name+"]OldPassword["+oldPassword+"]CurrentPassword["+password+"]ModifyFail");
					tx.rollback();
					exceptionReply(session,e,data.getSerial());					
				}
			}
		}
		
		private void modifyPassword(IoSession session,UWAPData data) throws Exception{
			ISource source = (ISource) session.getAttribute("ISource");
			if(source!=null){
				String name = data.readString().trim();
				String key = data.readString();
				String oldPassword = data.readString();
				String password = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					service.modifyPassword(name, key, oldPassword,password, source.getId());
					tx.commit();
					log.info("Name["+name+"]Key["+key+"]OldPassword["+oldPassword+"]CurrentPassword["+password+"]Modified");
					UWAPSegment seg = new UWAPSegment(GameAccountMessageType.MODIFY_PASSWORD_OK,data.getSerial());
					session.write(seg);
				}catch(ModifyException e){
					log.info("Name["+name+"]Key["+key+"]OldPassword["+oldPassword+"]CurrentPassword["+password+"]ModifyFail");
					tx.rollback();
					exceptionReply(session,e,data.getSerial());
				}
			}
		}
		
		private void serverLogin(IoSession session, UWAPData data)
				throws Exception {
			synchronized (session) {
				log.info("Server["+session.getRemoteAddress()+"]TryLogin");
				ISource source = (ISource) session.getAttribute("ISource");
				if (source != null) {
					UWAPSegment seg = new UWAPSegment(
							ServerMessageType.LOGINERROR, data.getSerial(), data
									.getSessionId());
					seg.writeString("已经存在Source");
					session.write(seg);
				} else {
					String serviceId = data.readString();
					String password = data.readString();
					log.info("serviceId=" + serviceId + ",password=" + password);
					source = manager.getSource(serviceId);
					if (source == null) {
						UWAPSegment seg = new UWAPSegment(
								ServerMessageType.LOGINERROR, data.getSerial(),
								data.getSessionId());
						seg.writeString("serviceId对应的Source不存在");
						session.write(seg);
						session.close();
					} else {
						if (source.getStatus() == Status.valid) {
							UWAPSegment seg = new UWAPSegment(
									ServerMessageType.LOGINERROR, data
											.getSerial(), data.getSessionId());
							seg.writeString("Source已经在可用状态");
							session.write(seg);
							session.close();
						} else {
							if (!source.getPassWord().equals(password)) {
								UWAPSegment seg = new UWAPSegment(
										ServerMessageType.LOGINERROR, data
												.getSerial(), data
												.getSessionId());
								seg.writeString("登陆密码错误");
								session.write(seg);
								session.close();
							}else{
								UWAPSegment seg = new UWAPSegment(ServerMessageType.LOGINOK,data.getSerial(),data.getSessionId());
								session.write(seg);
								source.setStatus(Status.valid);
								session.setAttribute("ISource",source);
								log.info("Server["+session.getRemoteAddress()+"]LoginOk");
								addSession(session);
							}

						}
					}
				}
			}
		}
		
		private void legacyQuickReg(IoSession session, UWAPData data)
				throws Exception {
			ISource source = (ISource) session.getAttribute("ISource");
			if (source != null) {
				String phone = data.readString();
				String version = data.readString();
				String model = data.readString();
				String gamecode = data.readString();
				String name = null;
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					name = QUICKREG_PREFIX + SequenceGenerator.getAccountName();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					tx.commit();
				}
				String[] versions = version.split("-");
				String versionPatch = null;
				if(versions.length==3){
					version = versions[0]+"-"+versions[1];
					versionPatch = versions[2];
				}
				
				// 新版本协议带上一个手机号
				String loginPhone = "";
				try {
				    loginPhone = data.readString();
				} catch (Exception e) {
				}
				
				if (name != null) {
					tx = sf.getCurrentSession().beginTransaction();
					try {
						AccountEntity ae = service.register(name, "", "", source.getBalance(),
								gamecode, phone, "", version, "", model, versionPatch, loginPhone);
						tx.commit();
						log.info("ID[" + ae.getId() + "]Name[" + ae.getName()
								+ "]GameCode["
								+ ae.getAccount().getCreateGameCode()
								+ "]Phone[" + loginPhone + "]QuickREGOK");
						UWAPSegment seg = new UWAPSegment(
								GameAccountMessageType._LEGACY_QUICKREG_RESULT,
								data.getSerial());
						seg.writeInt(ae.getId());
						seg.writeString(ae.getName());
						seg.writeString(ae.getPassWord());
						seg.write((byte) 0);// 新建了帐号
						session.write(seg);
					} catch (CreateAccountException e) {
//						e.printStackTrace();
						tx.rollback();
						exceptionReply(session, e, data.getSerial());
					} 
				}
			}
		}
		
		private void reg(IoSession session, UWAPData data) throws Exception {
			ISource source = (ISource) session.getAttribute("ISource");
			if (source != null) {
				String name = data.readString().trim();
				String phone = data.readString();
				String recommend = data.readString();
				int recommendId = data.readInt();
				if (recommendId == -1) {
				    recommend = "";
				} else {
				    recommend = String.valueOf(recommendId);
				}
				String model = data.readString();
				String gamecode = data.readString();
				String versionString = data.readString();
				String[] versions = versionString.split("-");
				String versionPatch = null;
				if(versions.length==3){
					versionString = versions[0]+"-"+versions[1];
					versionPatch = versions[2];
				}
				
				// 新协议会带一个手机号
				// 090831更新协议可在最后附加初始密码
				String loginPhone = "";
				String initPass = "";
				try {
				    loginPhone = data.readString();
				    initPass = data.readString();
				} catch (Exception e) {
				}
				
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					if(name.startsWith(QUICKREG_PREFIX)){
						throw new CreateAccountException(Errors.ILLEGAL_NAME_CHAR);
					}
					if (numberStringValidator.valid(name) == IStringValidator.OK) { // 不允许注册纯数字帐号
						if (phoneValidator.valid(name) != IStringValidator.OK) //允许用手机号注册
							throw new CreateAccountException(
									Errors.ILLEGAL_NAME_CHAR);
					}
					AccountEntity ae = service.register(name, initPass, "", source.getBalance(),
							gamecode, phone, recommend, versionString, "",
							model, versionPatch, loginPhone);
					tx.commit();
					log.info("ID[" + ae.getId() + "]Name[" + ae.getName()
							+ "]GameCode["
							+ ae.getAccount().getCreateGameCode() + "]Phone[" + loginPhone + "]REGOK");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.ACCOUNT_REG_OK, data
									.getSerial());
					seg.writeInt(ae.getId());
					seg.writeString(ae.getName());
					seg.writeString(ae.getPassWord());
					session.write(seg);
				} catch (CreateAccountException e) {
//					e.printStackTrace();
					tx.rollback();
					exceptionReply(session, e, data.getSerial());
				} 
			}
		}

		private void logout(IoSession session,UWAPData data) throws Exception{
			ISource source = checkSource(session);
			if(source!=null){
				String name = data.readString().trim();
				String key = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try{
					AccountEntity ae = service.logout(name, key,source.getId());
					log.info("ID["+ae.getId()+"]Name["+ae.getName()+"]Key["+key+"]Balance[" + ae.getBalance().getValue() + "]Logouted");
					tx.commit();
				}catch(LogoutException ex){
//					ex.printStackTrace();
					tx.rollback();
					exceptionReply(session, ex, data.getSerial());					
				}
			}
			
		}
		
		private void login(IoSession session, UWAPData data) throws Exception {
			ISource source = checkSource(session);
			if (source != null) {
				String name = data.readString().trim();
				String password = data.readString();
				String loginPhone = "";

				// 新版本协议会带一个手机号上来
				try {
				    loginPhone = data.readString();
				} catch (Exception e) {
				}
				log.info("Name["+name+"]Password["+password+"]Phone[" + loginPhone + "]TryLogin");
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					LoginResult r = service.login(name, password, loginPhone, source.getId());
					tx.commit();
					log.info("ID[" + r.getAccountEntity().getId() + "]Name["
							+ r.getAccountEntity().getName() + "]Key["
							+ r.getLoginInfo().getSessionId() + "]Service["
							+ r.getLoginInfo().getServiceId() + "]Phone[" + loginPhone + "]Balance["
							+ r.getAccountEntity().getBalance().getValue() + "]Logined");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType._LEGACY_LOGIN_OK, data
									.getSerial());
					seg.writeInt(r.getAccountEntity().getId());
					seg.writeString(r.getAccountEntity().getName());
					seg.writeString(r.getLoginInfo().getSessionId());
					seg.writeString(r.getAccountEntity().getPhone());
					seg.writeInt(r.getAccountEntity().getAccount().getModifyPasswordTimes());
					seg.writeInt((int)r.getAccountEntity().getBalance().getValue());
					seg.writeInt(0);
					seg.writeInts(r.getAccountEntity().getPurchasedCodes());
					seg.writeLong(r.getAccountEntity().getBalance().getValue());
					session.write(seg);
				} catch (LoginException ex) {
					log.error(ex, ex);
					tx.rollback();
					exceptionReply(session, ex, data.getSerial());
				} 
			}
		}
		
		private void buy(IoSession session, UWAPData data) throws Exception {
			ISource source = checkSource(session);
			if(source!=null){
				String name = data.readString().trim();
				String sessionId = data.readString();
				int v = data.readInt();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					Balance[] bs = service.decBBalance(name, sessionId, source.getId(), v);
					tx.commit();
					log.info("Name[" + name + "]Key[" + sessionId + "]Value["
							+ v + "]OldBalance[" + bs[0] + "]CurrentBalance["
							+ bs[1] + "]BuyOk");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.BUY_OK, data.getSerial(),
							data.getSessionId());
					seg.writeInt((int)(bs[0].getValue() - bs[1].getValue()));
					seg.writeInt((int)bs[1].getValue());
					seg.writeLong(bs[1].getValue());
					session.write(seg);
				} catch (BalanceException e) {
//					e.printStackTrace();
					tx.rollback();
					exceptionReply(session,e,data.getSerial());
				} 

			}
		}

		private void decBalance(IoSession session, UWAPData data)
				throws Exception {
			ISource source = checkSource(session);
			if (source != null) {
				String name = data.readString().trim();
				String sessionId = data.readString();
				int v = data.readInt();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					Balance[] balances = service.decABalanceToZero(name,
							sessionId, source.getId(), v);
					long m = balances[0].getValue() - balances[1].getValue();
					// int[] values = service.decConfirmedBalanceToZero(name,
					// sessionId, source.getId(), v);
					tx.commit();
					log.info("Name[" + name + "]Value[" + m + "]OldBalance["
							+ balances[0] + "]CurrentBalance[" + balances[1]
							+ "]DecOk");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.DEC_BALANCE_OK, data
									.getSerial(), data.getSessionId());
					seg.writeInt((int)m);
					seg.writeInt((int)balances[1].getValue());
					seg.writeLong(balances[1].getValue());
					session.write(seg);
				} catch (BalanceException e) {
//					e.printStackTrace();
					tx.rollback();
					exceptionReply(session, e, data.getSerial());
				} 
			}
		}	
		
		private void decBalance2(IoSession session,UWAPData data) throws Exception{
			ISource source = checkSource(session);
			if (source != null) {
				String name = data.readString().trim();
				int v = data.readInt();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					Balance[] balances = service.decABalance(name, v);
					long m = balances[0].getValue() - balances[1].getValue();
					tx.commit();
					log.info("Name[" + name + "]Value[" + m + "]OldBalance["
							+ balances[0] + "]CurrentBalance[" + balances[1]
							+ "]DecNoSessionOk");
					UWAPSegment seg = new UWAPSegment(
							GameAccountMessageType.DEC_BALANCE2_OK, data
									.getSerial(), data.getSessionId());
					seg.writeInt((int)m);
					seg.writeInt((int)balances[1].getValue());
					seg.writeLong(balances[1].getValue());
					session.write(seg);
				} catch (BalanceException e) {
//					e.printStackTrace();
					tx.rollback();
					exceptionReply(session, e, data.getSerial());
				} 
			}			
		}
		
//		private void imoneyNoSession(IoSession session,UWAPData data) throws Exception{
//			ISource source = checkSource(session);
//			if(source!=null){
//				int requestId = data.readInt();
//				String name = data.readString();
//				int v = data.readInt();
//				boolean allowNegative = data.readBoolean();
//				Transaction tx = sf.getCurrentSession().beginTransaction();
//				try {
//					int balance = service.modifyBalance(name, source.getId(), v, allowNegative);
//					tx.commit();
//					UWAPSegment seg = new UWAPSegment(UWAPType.IMONEY_NOSESSION_OK,data.getSerial(),data.getSessionId());
//					seg.writeInt(requestId);
//					seg.writeInt(balance);
//					session.write(seg);
//				} catch (BalanceException e) {
//					e.printStackTrace();
//					tx.rollback();
//					exceptionReply(session,e,requestId);
//				}
//			}
//		}
		
		private void changeStatus(IoSession session,UWAPData data) throws Exception{
			ISource source = checkSource(session);
			if(source!=null){
				String name = data.readString().trim();
				int status = data.readInt();
				String comment = data.readString();
				Transaction tx = sf.getCurrentSession().beginTransaction();
				try {
					AccountEntity ae = service.changeStatus(name, status, comment);
					tx.commit();
					log.info("ID["+ae.getId()+"]Name["+ae.getName()+"]status["+ae.getStatus()+"]Changed");
				} catch (ChangeStatusException e) {
					tx.rollback();
//					exceptionReply(session,e,data.getSerial());
				} 
			}
		}
		
		private void exceptionReply(IoSession session,BaseAccountException ex,int requestId){
			UWAPSegment seg = new UWAPSegment(ServerMessageType.ERROR,requestId);
			seg.writeInt(ex.getCode());
			session.write(seg);
		}
		
		private ISource checkSource(IoSession session){
			ISource source = (ISource)session.getAttribute("ISource");
			if(source==null||source.getStatus()!=Status.valid)
				return null;
			return source;
		}

	}
}
