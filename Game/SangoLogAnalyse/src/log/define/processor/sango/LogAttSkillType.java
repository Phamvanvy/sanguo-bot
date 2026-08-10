package log.define.processor.sango;

import log.define.Definings;
import log.define.processor.LogProcessor;

public class LogAttSkillType extends LogProcessor {

	public LogAttSkillType(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String process(String data) {
		String result = null;
		if(data != null){
			if(data.contains(",")){
				StringBuilder sb = new StringBuilder();
				String[] tempS = data.split(",");
				if(data.contains("true") || data.contains("false")){
					for(int i=0;i<tempS.length;i++){
						if(tempS[i].equals("true")){
							sb.append("已激活");
							sb.append(",");
						}else if(tempS[i].equals("false")){
							sb.append("未激活");
							sb.append(",");
						}
					}
					result = sb.toString();
				} else{
					for (int i = 0; i < tempS.length; i++) {
						int ski = Integer.parseInt(tempS[i]);
						if(ski == 0){
							sb.append("未学习");
						} else {
						int skil = ski >> 16;
							String skillId = String.valueOf(skil);
							sb.append("技能Id：" + skillId + " ,技能名称："
									+ Definings.getSkillName(skillId) + " ");
						}
						if (i < tempS.length - 1) {
							sb.append(",");
						}
					}
					result = sb.toString();
				}
			}  else {
				result = data;
				int ski = Integer.parseInt(data);
				int skil = ski >> 16;
				String skillId = String.valueOf(skil);
				String s = "技能Id：" + skillId + " ,技能名称："
				+ Definings.getSkillName(skillId) + " ";
				result = s;
			}
		}
          return result;
    }
}
