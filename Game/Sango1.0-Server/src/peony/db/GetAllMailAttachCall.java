package peony.db;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import com.pip.sanguo.data.item.Item;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.Mail;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionException;
import peony.game.itemeffect.KingItemEffect;
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
	protected boolean getAttachSuccess;
	
	public GetAllMailAttachCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.MAIL_GETALLATTACH_SERVER);
			pt.putInt(serial);
			session.send(pt);
			if(getAttachSuccess)
				p.message(-1, "提取附件成功", -1, -1);
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
									error(null, peony.Messages.STRING_00020);
									break;
								} else {
									Gain gain = new Gain(p);
									if((att.getGameItem().template.id==CandidateService.KING_WEAL_BAG || att.getGameItem().template.id==CandidateService.KING_TOKEN
											|| att.getGameItem().template.id == KingItemEffect.KINGITEM_HORSE) 
											&& p.isKing()!=1){
										error(null, peony.Messages.STRING_01358);
//										p.message(-1, peony.Messages.STRING_01358, -1, -1);
										break;
									}
									gain.addGainItem(att.getGameItem(), att.getCount());
									PlayerTransaction tx = p.newTransaction("ATT");
									try {
										p.bag.addGainComplete(gain, tx, true);
										p.decMoney(m.getPrice(), tx, true);
										if (m.getSourceId()==-1 && (m.getContent().length() == 0 || m.getSourceName().equals(peony.Messages.STRING_01359))) {
											mailDAO.makeTransient(m);
										} else {
											m.setAttachment(null);
											m.setStatus(Mail.READED);
											mailDAO.updateEntity(m);
										}
										tx.commit();
										getAttachSuccess = true;
										for(GainItem gi : gain.getGainItems()){
											if(gi!=null){
												GameItem item = gi.getItem();
												if(item!=null && item.template.itemType==Item.TYPE_CARD){
													int qulity = item.template.quality;
													PlayerTransaction tx1 = p.newTransaction("CARDCREDIT");
													try {
														if(qulity==0)
															p.addCredit(1*gi.getCount(), tx1, true);
														else if(qulity==1)
															p.addCredit(5*gi.getCount(), tx1, true);
														else if(qulity==2)
															p.addCredit(20*gi.getCount(), tx1, true);
														else if(qulity==3)
															p.addCredit(50*gi.getCount(), tx1, true);
														tx1.commit();
													} catch (TransactionException e) {
														tx1.rollback();
													}
												}
											}
										}
										if (m.getPrice() > 0) {
											Mail returnMail = new Mail();
											returnMail.setDestId(m.getSourceId());
											returnMail.setPrice(0);
											returnMail.setSourceId(-1);
											returnMail.setSourceName(peony.Messages.STRING_00004);
											returnMail.setStatus(Mail.UNREADED);
											returnMail.setTitle(MessageFormat.format(peony.Messages.STRING_01360, m.getTitle()));
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
										error(null, peony.Messages.STRING_00238);
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
									if (m.getSourceId()==-1 && (m.getContent().length() == 0 || m.getSourceName().equals(peony.Messages.STRING_01359))) {
										mailDAO.makeTransient(m);
									} else {
										m.setAttachment(null);
										m.setStatus(Mail.READED);
										mailDAO.updateEntity(m);
									}
									tx.commit();
									getAttachSuccess = true;
									LogUtil.logGetAttachOK(p, m.getId(), m
											.getSourceId(), att.getCount(), null,
											0, 0, -1);
								} catch (Exception e) {
									error(null, peony.Messages.STRING_01361);
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
