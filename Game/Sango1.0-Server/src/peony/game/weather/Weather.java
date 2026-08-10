package peony.game.weather;

public class Weather {
	
	public int type;
	public int size; //画线的长度
	public int count; //密集度
	public int speed; //速度
	public int wind; //风力
	public int color; //颜色

	public static final int TYPE_RAIN = 0;
	public static final int TYPE_SNOW = 1;
	
	public Weather(int type,int size,int count,int speed,int wind,int color){
		this.type = type;
		this.size = size;
		this.count = count;
		this.speed = speed;
		this.wind = wind;
		this.color = color;
	}
}
