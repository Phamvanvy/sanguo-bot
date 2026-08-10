package peony.util;

public class ProvinceUtil {

	protected static String[] provinces = { "吉林", "广东", "江苏", "浙江", "福建" };

	protected static String[][] citys = {
			{ "长春", "吉林", "延吉", "四平", "通化", "白城", "辽源", "松原", "白山", "梅河口", "珲春" },
			{ "广州", "深圳", "珠海", "汕头", "韶关", "佛山", "江门", "湛江", "茂名", "肇庆", "惠州",
					"梅州", "汕尾", "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮" },
			{ "南京", "苏州", "无锡", "常州", "镇江", "南通", "泰州", "扬州", "淮安", "盐城", "徐州",
					"连云港", "宿迁" },
			{ "杭州", "宁波", "温州", "嘉兴", "湖州", "绍兴", "金华", "衢州", "舟山", "台州", "丽水" },
			{ "福州", "厦门", "泉州", "漳州", "三明", "龙岩", "南平", "莆田", "宁德" } };
	
	public static String getProvinceByCity(String city){
		int index = -1;
		for(int i=0;i<citys.length;i++){
			String[] cs = citys[i];
			for(int j=0;j<cs.length;j++){
				if(cs[j].equals(city))
					index = i;
			}
		}
		if(index!=-1)
			return provinces[index];
		return "";
	}

}
