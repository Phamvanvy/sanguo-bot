package peony.util;

public class ProvinceUtil {

	protected static String[] provinces = { peony.Messages.STRING_00334, peony.Messages.STRING_00335, peony.Messages.STRING_00336, peony.Messages.STRING_00337, peony.Messages.STRING_00338 };

	protected static String[][] citys = {
			{ peony.Messages.STRING_00339, peony.Messages.STRING_00334, peony.Messages.STRING_00340, peony.Messages.STRING_00341, peony.Messages.STRING_00342, peony.Messages.STRING_00343, peony.Messages.STRING_00344, peony.Messages.STRING_00345, peony.Messages.STRING_00346, peony.Messages.STRING_00347, peony.Messages.STRING_00348 },
			{ peony.Messages.STRING_00349, peony.Messages.STRING_00350, peony.Messages.STRING_00351, peony.Messages.STRING_00352, peony.Messages.STRING_00353, peony.Messages.STRING_00354, peony.Messages.STRING_00355, peony.Messages.STRING_00356, peony.Messages.STRING_00357, peony.Messages.STRING_00358, peony.Messages.STRING_00359,
					peony.Messages.STRING_00360, peony.Messages.STRING_00361, peony.Messages.STRING_00362, peony.Messages.STRING_00363, peony.Messages.STRING_00364, peony.Messages.STRING_00365, peony.Messages.STRING_00366, peony.Messages.STRING_00367, peony.Messages.STRING_00368, peony.Messages.STRING_00369 },
			{ peony.Messages.STRING_00370, peony.Messages.STRING_00371, peony.Messages.STRING_00372, peony.Messages.STRING_00373, peony.Messages.STRING_00374, peony.Messages.STRING_00375, peony.Messages.STRING_00376, peony.Messages.STRING_00377, peony.Messages.STRING_00378, peony.Messages.STRING_00379, peony.Messages.STRING_00380,
					peony.Messages.STRING_00381, peony.Messages.STRING_00382 },
			{ peony.Messages.STRING_00383, peony.Messages.STRING_00384, peony.Messages.STRING_00385, peony.Messages.STRING_00386, peony.Messages.STRING_00387, peony.Messages.STRING_00388, peony.Messages.STRING_00389, peony.Messages.STRING_00390, peony.Messages.STRING_00391, peony.Messages.STRING_00392, peony.Messages.STRING_00393 },
			{ peony.Messages.STRING_00394, peony.Messages.STRING_00395, peony.Messages.STRING_00396, peony.Messages.STRING_00397, peony.Messages.STRING_00398, peony.Messages.STRING_00399, peony.Messages.STRING_00400, peony.Messages.STRING_00401, peony.Messages.STRING_00402 } };
	
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
