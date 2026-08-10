package peony.db;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.LogUtil;
import peony.game.Mail;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.mail.ItemMailAttachment;
import peony.game.mail.MailAttachment;
import peony.game.mail.MailService;
import peony.game.mail.MoneyMailAttachment;
import peony.game.nation.CandidateService;
import peony.net.ClientSession;
import peony.net.Packet;

public class GetAllMailAttachCall extends ClientSessionAsyncCall {

	protected int serial;
	protected Player p;
	
	public GetAllMailAttachCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.MAIL_GETALLATTACH_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MAIL_GETALLATTACH_CLIENT, errorMessage);
		}
	}

	public void run() {
		p = (Player)session.getClient();
		if(p!=null){
			MailDAO mailDAO = Server.server.getServiceRegistry().getDbService().mailDAO;
			List<Mail> mails = mailDAO.getAllSystemMailList(p.id, new Date());
			synchronized (Server.server.getServiceRegistry().getMailService()) {
				for(Mail m : mails){
					if (m != null && m.getDestId() == p.id) {
						MailAttachment attachment = m.getAttachment();
						if (attachment != null) {
							if (attachment instanceof ItemMailAttachment) {
								ItemMailAttachment att = (ItemMailAttachment) attachment;
								if (m.getPrice() > 0 && p.getMoney() < m.getPrice()) {
									error(null, "没有足够的金钱");
									break;
								} else {
									Gain gain = new Gain(p);
									if((att.getGameItem().template.id==CandidateService.KING_WEAL_BAG || att.getGameItem().template.id==CandidateService.KING_TOKEN) 
											&& p.isKing()!=1){
										error(null, "此邮件已过期不能提取");
										break;
									}
									gain.addGainItem(att.getGameItem(), att.getCount());
									PlayerTransaction tx = p.newTransaction("ATT");
									try {
										p.bag.addGainComplete(gain, tx, true);
										p.decMoney(m.getPrice(), tx, true);
										if (m.getSourceId()==-1 && (m.getContent().length() == 0 || m.getSourceName().equals("拍卖行"))) {
											mailDAO.makeTransient(m);
										} else {
											m.setAttachment(null);
											m.setStatus(Mail.READED);
											mailDAO.updateEntity(m);
										}
										tx.commit();
										if (m.getPrice() > 0) {
											Mail returnMail = new Mail();
											returnMail.setDestId(m.getSourceId());
											returnMail.setPrice(0);
											returnMail.setSourceId(-1);
											returnMail.setSourceName("系统");
											returnMail.setStatus(Mail.UNREADED);
											returnMail.setTitle(MessageFormat.format("回复:{0}", m.getTitle()));
											returnMail.setPostTime(new Date());
											returnMail.setValidTime(new Date());
											returnMail.setExpirationTime(new Date(System.currentTimeMillis()
								+ MailService.EXPIRATION_TIME));
											returnMail.setContent("");
											returnMail
													.setAttachment(new MoneyMailAttachment(
															m.getPrice()));
											mailDAO.newEntity(returnMail);

											LogUtil.logGetAttachOK(p, m.getId(), m
													.getSourceId(), 0, att
													.getGameItem(), att.getCount(),
													m.getPrice(), returnMail
															.getId());

											Player dest = ObjectAccessor
													.getPlayer(m.getSourceId());
											if (dest != null
													&& dest.systemState == Player.SYSTEMSTATE_READY) {
												Packet pt = new Packet(
														OpCode.MAIL_NEW_SERVER);
												dest.send(pt);
											}
										} else {
											LogUtil.logGetAttachOK(p, m.getId(), m
													.getSourceId(), 0, att
													.getGameItem(), att.getCount(),
													0, -1);
										}
									} catch (Exception e1) {
										tx.rollback();
										error(null, "没有足够的包格");
										m.setStatus(Mail.UNREADED);
										mailDAO.updateEntity(m);
										break;
									}
								}
							} else if (attachment instanceof MoneyMailAttachment) {
								MoneyMailAttachment att = (MoneyMailAttachment) attachment;
								PlayerTransaction tx = p.newTransaction("ATT");
								try {
									p.addMoney(att.getCount(), tx, true);
									if (m.getSourceId()==-1 && (m.getContent().length() == 0 || m.getSourceName().equals("拍卖行"))) {
										mailDAO.makeTransient(m);
									} else {
										m.setAttachment(null);
										m.setStatus(Mail.READED);
										mailDAO.updateEntity(m);
									}
									tx.commit();
									LogUtil.logGetAttachOK(p, m.getId(), m
											.getSourceId(), att.getCount(), null,
											0, 0, -1);
								} catch (Exception e) {
									error(null, "取金钱错误");
									tx.rollback();
									m.setStatus(Mail.UNREADED);
									mailDAO.updateEntity(m);
									break;
								}
							}
						} else {
							continue;
						}
					} else {
						continue;
					}
				}
			}
		}
		addToClientSession();
	}

}
