package com.pip.itimes.server.world.question;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultListModel;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pip.itimes.server.world.Server;

public class QuestionLoader {

	public QuestionLoader(File pkgDir) throws Exception{
		SAXReader reader = new SAXReader();
		String dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),"Areas/questions.xml");
        Document doc = reader.read(new File(dirName));
        loadQuestions(doc);
        dirName = FilenameUtils.concat(pkgDir.getAbsolutePath(),"Areas/questiontime.xml");
        doc = reader.read(new File(dirName));
        loadQuestionTime(doc);
	}
	
	private void loadQuestionTime(Document doc) {
		Element root = doc.getRootElement();
		if(root == null)
			return;
		int id;
		long begin = 0;
		long end = 0;
		Iterator it = root.elementIterator("time");
		String timestr = "";
		while(it.hasNext()) {
			Element elem = (Element)it.next();
			id = Integer.parseInt(elem.attributeValue("id"));
			String b = elem.attributeValue("begin");
			String e = elem.attributeValue("end");
			timestr = timestr + b + "-" + e;
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			try {
				Date d = format.parse(b);
				begin = d.getTime();
				d = format.parse(e);
				end = d.getTime();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			Question.questionTimes.add(new Object[] {new Integer(id),begin,end});
			timestr = timestr + "、";
		}
		timestr = timestr.substring(0, timestr.length() - 1);
		Question.questionTimes_str = timestr;
	}
	
	private void loadQuestions(Document doc) {
		Element root = doc.getRootElement();
		if(root == null)
			return;
		int id;
		String question;
		int select;
		DefaultListModel answers = new DefaultListModel();
		Iterator versionIterator = root.elementIterator("version");
		int typeId = 0;
		Vector<Question> questionReference = null;
		ConcurrentHashMap<Integer, Question> questionMap = null;
		while(versionIterator.hasNext()) {
			Element elems = (Element)versionIterator.next();
			Iterator it = elems.elementIterator("question");
			typeId = Integer.parseInt(elems.attributeValue("id"));
			questionReference = new Vector<Question>(); //创建存储
			questionMap = new ConcurrentHashMap<Integer, Question>();			//ID信息的存储
			while(it.hasNext()) {
				Element elem = (Element)it.next();
				if(Integer.parseInt(elem.attributeValue("useable")) == 1)
					continue;
				id = Integer.parseInt(elem.attributeValue("id"));
				question = elem.attributeValue("question");
				select = Integer.parseInt(elem.attributeValue("select"));
				Iterator i = elem.elementIterator("answer");
				while(i.hasNext()) {
					Element el = (Element)i.next();
					answers.addElement(el.attributeValue("answer"));
				}
				String[] a = new String[answers.size()];
				for(int z = 0 ; z < answers.size() ; z ++) {
					a[z] = (String)answers.get(z);
				}
				Question q = new Question(id,question,select,a);
				questionReference.add(q);				//question存储
//				if(1 == typeId){
//					Question.addQuestion(q);
//				}else if(2 == typeId){
//					Question.addCmccQuestion(q);
//				}
				questionMap.put(q.getId(), q);
				answers.removeAllElements();	
			}
			if(questionReference != null){
				QuestionService.addQuestions(typeId, questionReference);				//题库保存
				QuestionService.addQuestions(typeId, questionMap);						//根据ID号，对题库进行存储
			}	
		}
		
	}
}
