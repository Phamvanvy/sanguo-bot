package com.pip.server.billing.card;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * 检查兑换卡。
 * 请求参数：
 *     cardno = 卡号
 *     gamecode = 游戏代码
 *     cardtype = 允许的兑换类型，-1表示不限制
 *     accountid = 帐号ID（可选）
 * 输出(UTF-8)：
 *     第一行：返回代码，0 - 成功，1 - 卡号不存在，2 - 游戏代码不匹配，3 - 兑换物品类型不匹配，4 - 已经兑换过了，5 - 卡已过期，6 - 暂停兑换，7 - 一个帐号只能兑换一次
 *     第二行：如果成功，返回这张卡的兑换物品类型；如果失败，返回错误信息。
 * @author lighthu
 */
public class CheckCardServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(CheckCardServlet.class);
    private CardDAO cardDAO;
    private Set<String> allows = null;
    private List<String> allowPrefix = null;
    
    public CheckCardServlet(String[] allows) {
        cardDAO = new CardDAO();
        if (allows != null) {
        	this.allows = new HashSet<String>();
        	this.allowPrefix = new ArrayList<String>();
	        for (String s : allows) {
	        	if (s.endsWith("*")) {
	        		this.allowPrefix.add(s.substring(0, s.length() - 1));
	        	} else {
	        		this.allows.add(s);
	        	}
	        }
        }
    }

    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {
    	// 验证请求IP
    	if (allows != null) {
    		String addr = request.getRemoteAddr();
    		boolean ok = false;
    		if (allows.contains(addr)) {
    			ok = true;
    		} else {
    			for (String prefix : allowPrefix) {
    				if (addr.startsWith(prefix)) {
    					ok = true;
    					break;
    				}
    			}
    		}
    		if (!ok) {
    			log.warn("Possible attack from [" + addr + "] is rejected.");
    			return;
    		}
        }
        
        String cardno = request.getParameter("cardno");
        int gamecode = Integer.parseInt(request.getParameter("gamecode"));
        int cardtype = Integer.parseInt(request.getParameter("cardtype"));
        int accountID = -1;
        try {
            accountID = Integer.parseInt(request.getParameter("accountid"));
        } catch (Exception e) {
        }

        log.info("[CHECK_CARD]cardno[" + cardno + "]gamecode[" + gamecode + "]cardtype[" + cardtype + "]accountid[" + accountID + "]");
        
        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        Card card = cardDAO.getByCardNo(cardno);
        int result = 0;
        if (card == null) {
            result = 1;
        } else if (card.getGameCode() != -1 && card.getGameCode() != gamecode) {
            result = 2;
        } else if (cardtype != -1 && cardtype != card.getCardType()) {
            result = 3;
        } else if (card.isUsed()) {
            result = 4;
        } else if (card.getValidTime().getTime() < System.currentTimeMillis()) {
            result = 5;
        }
        if (accountID != -1 && result == 0 && card.getCardType() != -1 && cardDAO.findCardByAccount(gamecode, card.getCardType(), accountID)) {
            // 一个帐号，一种激活码只能兑换一次
            result = 7;
        }
        if (result == 0) {
            log.info("[CHECK_CARD]cardno[" + card.getCardno() + "]gamecode[" + card.getGameCode() + "]cardtype[" + card.getCardType() + "]OK");
            card.setUsed(true);
            if (accountID != 0) {
                card.setAccountID(accountID);
            }
            if (card.getGameCode() == -1) {
            	card.setGameCode(gamecode);
            }
            cardDAO.update(card);
            out.println("0");
            out.println(card.getMapType());
        } else {
            out.println(result);
            switch (result) {
            case 1:
                out.println("卡号无效");
                break;
            case 2:
                out.println("卡号无效");
                break;
            case 3:
                out.println("卡类型不匹配");
                break;
            case 4:
                out.println("这张卡已经使用过了");
                break;
            case 5:
                out.println("卡已过期");
                break;
            case 6:
                out.println("暂停兑换");
                break;
            case 7:
                out.println("一个帐号只能兑换一次");
                break;
            }
        }
    }
}
