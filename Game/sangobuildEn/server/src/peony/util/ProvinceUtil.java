package peony.util;

public class ProvinceUtil {

	protected static String[] provinces = { "吉林", "廣東", "江蘇", "浙江", "福建" };

	protected static String[][] citys = {
			{ "長春", "吉林", "延吉", "四平", "通化", "白城", "遼源", "松原", "白山", "梅河口", "琿春" },
			{ "廣州", "深圳", "珠海", "汕頭", "韶關", "佛山", "江門", "湛江", "茂名", "肇慶", "惠州",
					"梅州", "汕尾", "河源", "陽江", "清遠", "東莞", "中山", "潮州", "揭陽", "云浮" },
			{ "南京", "蘇州", "無錫", "常州", "鎮江", "南通", "泰州", "揚州", "淮安", "鹽城", "徐州",
					"連云港", "宿遷" },
			{ "杭州", "宁波", "溫州", "嘉興", "湖州", "紹興", "金華", "衢州", "舟山", "台州", "麗水" },
			{ "福州", "廈門", "泉州", "漳州", "三明", "龍岩", "南平", "莆田", "宁德" } };
	
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
