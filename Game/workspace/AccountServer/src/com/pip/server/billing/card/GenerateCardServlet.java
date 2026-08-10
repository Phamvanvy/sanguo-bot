package com.pip.server.billing.card;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * 批量生成兑换卡。
 * 请求参数：
 *     password = 验证密码
 *     gamecode = 游戏代码，1 - 幻想，2 - 武林，3 - 乐园，4 - 三国
 *     cardtype = 兑换类型，由各游戏自己编码
 *     maptype = 实际兑换礼包代码，由各游戏自己编码
 *     count = 生成数量
 *     valid = 有效期（天）
 *     digits = 位数
 *     cardnofmt = 卡号的编码
 * 输出(UTF-8)：
 *     卡号列表，每行一个
 * @author lighthu
 */
public class GenerateCardServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(GenerateCardServlet.class);
    private CardDAO cardDAO;
    private Set<String> allows = null;
    private List<String> allowPrefix = null;
    private int digits;   // 卡号位数，10或者12
    
    public GenerateCardServlet(String[] allows, int digits) {
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
        this.digits = digits;
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
        
        String pass = request.getParameter("password");
        int gamecode = Integer.parseInt(request.getParameter("gamecode"));
        int cardtype = Integer.parseInt(request.getParameter("cardtype"));
        int maptype = Integer.parseInt(request.getParameter("maptype"));
        int count = Integer.parseInt(request.getParameter("count"));
        int valid = Integer.parseInt(request.getParameter("valid"));
        int dds = -1;
        try {
        	dds = Integer.parseInt(request.getParameter("digits"));
        } catch (Exception e) {
        }
        DecimalFormat cardNoFormat = null;
        try {
        	cardNoFormat = new DecimalFormat(request.getParameter("cardnofmt"));
        } catch (Exception e) {
        }
        
        if (!"pip@game2008".equals(pass)) {
            return;
        }
        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Date now = new Date(System.currentTimeMillis());
        Date endTime = new Date(System.currentTimeMillis() + valid * 86400000L);
        Random rand = new Random(now.getTime());
        for (int i = 0; i < count; i++) {
            while (true) {
                String no = generateCardNo(rand, now, dds);
                if (cardDAO.getByCardNo(no) == null) {
                    Card card = new Card();
                    card.setCardno(no);
                    card.setCardType(cardtype);
                    card.setMapType(maptype);
                    card.setGameCode(gamecode);
                    card.setValidTime(endTime);
                    card.setUsed(false);
                    cardDAO.create(card);
                    if (cardNoFormat != null) {
                    	out.print(cardNoFormat.format(card.getId()));
                    	out.print(",");
                    }
                	out.println(no);
                    out.flush();
                    break;
                }
            }
        }
    }
    
    private String generateCardNo(Random rand, Date date, int dds) {
    	if (dds == -1) {
    		dds = digits;
    	}
        int d = (int)((date.getTime() / 86400000L) % 1000);
        String ds = String.valueOf(d);
        while (ds.length() < 3) {
            ds = "0" + ds;
        }
        StringBuilder sb = new StringBuilder();
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append(ds.charAt(0));
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append(ds.charAt(1));
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append((char)('0' + rand.nextInt(10)));
        sb.append(ds.charAt(2));
        for (int i = 0; i < dds - 10; i++) {
        	sb.append((char)('0' + rand.nextInt(10)));
        }
        sb.append(Math.abs(sb.toString().hashCode()) % 10);
        return sb.toString();
    }
}
