package com.pip.server.account.stub;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Transaction;

import com.pip.db.hibernateDAO.HibernateUtil;
import com.pip.server.account.bean.Sequence2;
import com.pip.server.account.dao.Sequence2DAO;

public class Sequence2Generator {
	
	private static final Map<String,SequenceWrapper> name2sequence = new HashMap<String,SequenceWrapper>();
	private static final Sequence2DAO dao = new Sequence2DAO();
	
	static{
		Transaction tx = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
		Sequence2[] s = dao.getSequences();
		for(int i=0;i<s.length;i++){
			name2sequence.put(s[i].getName(), new SequenceWrapper(s[i]));
			s[i].setUsedId(s[i].getUsedId()+s[i].getStep());
			dao.save(s[i]);
		}
		tx.commit();
	}
	
	public static int getNextId(String name) throws SequenceException{
		SequenceWrapper s = name2sequence.get(name);
		if(s==null)
			throw new SequenceException("sequence "+name+" not found.");
		synchronized(s){
			int ret = s.currentId++;
			ensureSequence(s);
			return ret;
		}
	}
	
	public static void ensureSequence(SequenceWrapper sequence) throws SequenceException{
		if(sequence.currentId>sequence.sequence.getUsedId()){
			if(sequence.currentId>=sequence.sequence.getMaxId())
				throw new SequenceException("Sequence "+sequence.sequence.getName()+" MaxId.");
			int usedId = Math.min(sequence.currentId+sequence.sequence.getStep()-1, sequence.sequence.getMaxId());
			sequence.sequence.setUsedId(usedId);
			dao.save(sequence.sequence);
		}
	}

}

class SequenceWrapper{
	public Sequence2 sequence;
	public int currentId;
	
	public SequenceWrapper(Sequence2 sequence){
		this.sequence = sequence;
		this.currentId = sequence.getUsedId();
	}
}
