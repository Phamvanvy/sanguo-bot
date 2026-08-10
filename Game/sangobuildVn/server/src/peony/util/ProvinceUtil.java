package peony.util;

public class ProvinceUtil {

	protected static String[] provinces = { "Cát Lâm", "Quảng Đông", "Giang Tô", "Chiết Giang", "Phúc Kiến" };

	protected static String[][] citys = {
			{ "长春", "Cát Lâm", "Diên cát", "Tứ bình", "Thông hóa", "Bạch thành", "Liêu nguyên", "Tùng nguyên", "Bạch sơn", "Cửa sông mai", "Hồn xuân" },
			{ "广州", "Thâm quyến", "Châu hải", "Sán đầu", "韶关", "佛山", "江门", "Trạm Giang", "Mậu danh", "Triệu Khánh ", "Huệ Châu",
					"Mai châu", "Sẫu Vỹ", "Hà Nguyên", "Dương giang", "Thanh viễn", "Đông Hoan", "中山", "Triều Châu", "揭阳", "云浮" },
			{ "Nam Kinh", "Tô Châu", "无锡", "Thường Châu", "镇江", "Nam thông", "泰州", "Dương châu", "Hoài an", "盐城", "Từ Châu",
					"Cảng Liên Vân", "Túc Thiên" },
			{ "Hàng Châu", "Đinh Ba", "Ôn châu", "Gia hưng", "Hồ châu ", "Thiệu hưng", "Kim hoa", "Cù Châu", "Đơn Sơn", "Đài châu", "Lệ Thủy" },
			{ "Phúc châu", "Hạ môn", "Tuyền châu", "Chương châu", "Tam Minh", "龙岩", "Nam Bình ", "莆田", "宁德" } };
	
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
