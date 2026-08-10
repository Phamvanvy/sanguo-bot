package com.pip.itimes.server.world.question;


import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.world.BathHouse;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.WorldPlayer;


public class QuestionControl{
    private WorldPlayer player;

    private int answered;
    private int total;
    private int lastQuestion;
    private int questionState;
    
    
//    private static 

    public static final int Question_Begin = 0;
    public static final int Question_Goon = 1;
    public static final int Question_Succeed = 2;
    public static final int Question_Error = 3;
    public static final int Question_Wait = 4;

    protected static int[] MonsterExp = {
                    8, 8, 9, 10, 12, 14, 16, 18, 21, 24, 28, 32, 36, 40, 45, 50, 56, 62, 68, 74, 81, 88, 96, 104, 112, 120, 129, 138, 148, 158, 168, 178, 189, 200, 212, 224, 236, 248, 261, 274, 278,
                    291, 304, 317, 331, 345, 359, 374, 389, 394, 410, 426, 442, 458, 475, 492, 509, 527, 545, 563, 582, 601, 620, 640, 660, 680, 700, 721, 742, 763, 785, 807, 829, 852, 875, 898, 922,
                    946, 970, 994, 1019, 1044, 1069, 1095, 1121, 1147, 1174, 1201, 1228, 1256, 1284, 1312, 1340, 1369, 1398, 1427, 1457, 1487, 1517
    };
    private static ConcurrentHashMap<Integer, Integer> player2question = new ConcurrentHashMap<Integer, Integer>();

	public void setTypeID(int typeID) {
		int playerID = player.getId();		//玩家ID
		if(player2question.containsKey(playerID))
		{
			int temp = player2question.get(playerID);		//玩家接触的NPC的类型
			if(temp != typeID){
				player2question.put(playerID, typeID);		//保存玩家所接触的NPC的类型
			}
		}else{
			player2question.put(playerID, typeID);			//保存玩家所接触的NPC的类型
		}
	}

	public int getTypeID(int playerID){
		if(player2question.containsKey(playerID)){
			return player2question.get(playerID);
		}
		return -1;					//题库NPC的题号不能为-1
	}
	public QuestionControl(WorldPlayer player){
        this.player = player;
        if(!inTime()){
            questionState = Question_Wait;
        }else{
            Date t = player.getQuestionTime();
    
            if(t != null && isNewTime(t.getTime())){
                t = null;
                player.setQuestionTime(null);
            }
            
            if(t == null){
                if(!inTime()){
                    questionState = Question_Wait;
                }else{
                    questionState = Question_Begin;
                }
            }else{
                int state = player.getQuestionState();
    
                if((state & 0x80000000) != 0){
                    questionState = Question_Succeed; //第32位表示是否成功
                }else if((state & 0x40000000) != 0){ //第31位表示是否失败
                    questionState = Question_Error;
                }else{
                    answered = ((state >>> 24) & 0x3F); //第25-30位存储已答过的题，无符号，可以处理最多64道题
                    total = ((state >>> 16) & 0x3F); //第17-22位存储总题数，无符号，可以处理最多64道题  （23-24位预留，为扩充题目需要）
                    lastQuestion = state & 0xFFFF; //第1-16位存储最后答题的id，无符号，可以处理ID的最大值是65535的总题数
    
                    if(answered == 0 && total == 0){
                        questionState = Question_Begin;
                    }else{
                        questionState = Question_Goon;
                    }
                }
            }
        }
    }

    public void initQuestionState(){
        answered = 0;
//        total = (player.getLevel() * 2) / 5 + 10;
        total = 20;//20110328modify
        lastQuestion = 0;
        questionState = Question_Goon;

        player.setQuestionTime(new Date(System.currentTimeMillis()));
        makeQuestionState();
    }

    public void clearQuestionState(){
        answered = 0;
        total = 0;
        lastQuestion = 0;
        questionState = Question_Begin;

        player.setQuestionTime(null);
        makeQuestionState();
    }

    public void passQuestion(){
        int questionId = player.getQuestionId();
        int typeID = getTypeID(player.getId());
    	if(typeID == -1){
    		questionState = Question_Error;
    	}else{
    		Question qs = QuestionService.changeQuestions(typeID);
    		Question q = null;
    		if(qs != null){
    			q = qs.getQuestionById(questionId);
    		}

            if(q != null){
                answered++;

                if(answered >= total){
                    questionState = Question_Succeed;
                }else{
                    questionState = Question_Goon;
                }
            }else{
                questionState = Question_Error;
            }
    	}
        lastQuestion = 0;
        makeQuestionState();
    }

    public void answerQuestion(int answer){
        int questionId = player.getQuestionId();

        int typeID = getTypeID(player.getId());
    	if(typeID == -1){
    		questionState = Question_Error;
    	}else{
    		Question qs = QuestionService.changeQuestions(typeID);
    		Question q = null;
    		if(qs != null){
    			q = qs.getQuestionById(questionId);		//获得题目信息
    		} 

            if(q != null){
                if(q.getSelect() == answer){
                    answered++;

                    if(answered >= total){
                        questionState = Question_Succeed;
                    }else{
                        questionState = Question_Goon;
                    }
                }else{
                    questionState = Question_Error;
                }
            }else{
                questionState = Question_Error;
            }
    	}
    	

        lastQuestion = 0;
        makeQuestionState();
    }

    public int getQuestionState(){
        return questionState;
    }

    private void makeQuestionState(){
        int state = ((answered & 0x3F) << 24) | ((total & 0x3F) << 16) | (lastQuestion & 0xFFFF);

        if(questionState == Question_Error){
            state |= 0x40000000;
        }else if(questionState == Question_Succeed){
            state |= 0x80000000;
        }

        player.setQuestionState(state);
    }

    private boolean isNewTime(long oldTime){
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if(oldTime < cal.getTime().getTime()){
            return true;
        }else{
            return false;
        }
    }

    private boolean inTime(){
        long nowTime = System.currentTimeMillis() % 86400000;

        for(int i = 0; i < Question.questionTimes.size(); i++){
            Object[] time = (Object[])Question.questionTimes.get(i);
            if(nowTime >= Long.parseLong(String.valueOf(time[1])) && nowTime <= Long.parseLong(String.valueOf(time[2]))){
                break;
            }
            if(i == Question.questionTimes.size() - 1)
                return false;
        }
        return true;
    }

    public Question getQuestion(){
    	int typeID = getTypeID(player.getId());
    	if(typeID == -1){
    		return null;
    	}
    	Question qs = QuestionService.changeQuestions(typeID);
    	if(qs == null)
    	{
    		return null;			//返回null
    	}
        Question q = null;

        if(lastQuestion != 0){
            q = qs.getQuestionById(lastQuestion);

            if(q == null){
                q = qs.getNextQuestion();
            }
        }else{
            q = qs.getNextQuestion();
        }

        if(q != null){
            player.setQuestionId(q.getId());
            lastQuestion = q.getId() & 0xFFFF;

            makeQuestionState();
        }

        return q;
    }

    public int getPriceMoney(){
        if(player.getLevel() >= player.getMaxLevel()){
            return 0;
        }

//        return player.getLevel() * answered / 2 + 500;
        return player.getLevel() * answered / 2;
    }

    public int getPriceExp(){
        if(player.getLevel() >= player.getMaxLevel()){
            return 0;
        }
//        int oldexp = MonsterExp[player.getLevel() - 1] * answered / 2 + 800 + 5000 / (player.getLevel() + 1);
//        int oldexp = MonsterExp[player.getLevel() - 1] * answered / 2 ;
        //return oldexp * 2;//mengjie modify
//        int oldexp = MonsterExp[player.getLevel() - 1] * answered;
        int r = 27;//1505浴场倍数
        int oldexp = BathHouse.VIP_EXP[player.getLevel()]  * r * answered /100;//20110328modify
        return oldexp;	//世界杯答题修改
    }

    public static UWAPSegment getQuestionWaitSegment(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        byte[] questionWaitBytes = stageService.getTaskBytes((short)31019, new String[]{
            "未到答题时间!现在到瓦伊特杂货店商人处购买随身答题果可以在答题时间内随处答题啦.答题时间：" + Question.questionTimes_str
        });

        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        seg.writeShort((short)31019);
        seg.writeShort((short)2);
        seg.write(questionWaitBytes);

        return seg;
    }

    public static UWAPSegment getQuestionBeginSegment(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        String s = "你";

        if(player.getLevel() >= player.getMaxLevel()){
            s = "你已经满级,答题将得不到任何奖励!";
        }

        byte[] bytes = stageService.getTaskBytes((short)31010, new String[]{
                        "2", "1", s + "需要帮助吗?\n1.开始答题 \n2.不需要", "question_begin", "ok"
        });

        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        seg.writeShort((short)31010);
        seg.writeShort((short)2);
        seg.write(bytes);

        return seg;
    }

    public static UWAPSegment getQuestionGoonSegment(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        Question question = control.getQuestion();

        String str = "(" + (control.answered + 1) + "/" + control.total + ")" + question.getQuestion() + "?\n";
        String s[] = question.getAnswers();

        for(int i = 0; i <= s.length; i++){
            if(i == s.length){
                str += (i + 1) + "." + "稍后再答\n";
                str += (i + 2) + "." + "智多星答题";
            }else{
                str += (i + 1) + "." + s[i] + "\n";
            }
        }

        String[] p = new String[5 + s.length];

        p[0] = String.valueOf(s.length + 2);
        p[1] = "1";
        p[2] = str;

        for(int i = 0; i < s.length; i++){
            p[i + 3] = "question_answer " + i;
        }

        p[p.length - 2] = "question_pause";
        p[p.length - 1] = "question_next";

        byte[] questionBytes = stageService.getTaskBytes((short)31010, p);

        UWAPSegment questionSeg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        questionSeg.writeShort((short)31010);
        questionSeg.writeShort((short)2);
        questionSeg.write(questionBytes);

        return questionSeg;
    }

    public static UWAPSegment getQuestionPauseSegment(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        byte[] questionBytes = stageService.getTaskBytes((short)31019, new String[]{
            "本次共有" + control.total + "道题,答对了" + control.answered + "道!"
        });

        UWAPSegment questionSeg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        questionSeg.writeShort((short)31019);
        questionSeg.writeShort((short)2);
        questionSeg.write(questionBytes);

        return questionSeg;
    }

    public static UWAPSegment getQuestionSucceedSegment(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        byte[] questionBytes = stageService.getTaskBytes((short)31019, new String[]{
//            "恭喜你,全答对了!本次答题已经结束。可以到" + Server.iMoneyStoreString + "购买答题机会果获得额外的答题机会."
            "恭喜你,全答对了!本次答题已经结束。可以到瓦伊特的杂货店商人处购买答题机会果获得额外的答题机会."
        });

        UWAPSegment questionSeg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        questionSeg.writeShort((short)31019);
        questionSeg.writeShort((short)2);
        questionSeg.write(questionBytes);

        return questionSeg;
    }

    public static UWAPSegment getQuestionErrorSegment(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        byte[] questionBytes = stageService.getTaskBytes((short)31019, new String[]{
//            "很遗憾.你答错了.每场答题只能答一次!可以到" + Server.iMoneyStoreString + "购买答题机会果获得额外的答题机会."
            "很遗憾.你答错了.每场答题只能答一次!可以到瓦伊特的杂货店商人处购买答题机会果获得额外的答题机会."
        });

        UWAPSegment questionSeg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        questionSeg.writeShort((short)31019);
        questionSeg.writeShort((short)2);
        questionSeg.write(questionBytes);

        return questionSeg;
    }

    public static UWAPSegment getQuestionItemSegment(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        byte[] bytes = stageService.getTaskBytes((short)31010, new String[]{
                        "2", "1", "必须拥有智多星才可使用此功能!\n1.继续答题\n2.稍后再答", "question_again", "question_pause"
        });

        UWAPSegment questionSeg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        questionSeg.writeShort((short)31019);
        questionSeg.writeShort((short)2);
        questionSeg.write(bytes);

        return questionSeg;
    }
    public static UWAPSegment getQuestionError(QuestionControl control, WorldPlayer player, StageService stageService, int serial, int sessionId){
        byte[] questionWaitBytes = stageService.getTaskBytes((short)31019, new String[]{
            "未到答题时间!使用答题果不能答题！"});

        UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK, serial, sessionId);
        seg.writeShort((short)31019);
        seg.writeShort((short)2);
        seg.write(questionWaitBytes);

        return seg;
    }
}
