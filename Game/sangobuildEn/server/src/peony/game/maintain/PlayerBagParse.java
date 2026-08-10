package peony.game.maintain;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.XMLConfiguration;

import peony.game.DataService;
import peony.game.GameItem;
import peony.game.HorsePersistence;
import peony.game.IMoneyCardPersistence;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.Marshaller;
import peony.game.ObjectAccessor;
import peony.game.PersistenceException;
import peony.game.PersistenceManager;
import peony.game.itemenhance.ItemEnhancePersistence;
import peony.service.ServiceRegistry;

public class PlayerBagParse {
	
	protected static ServiceRegistry serviceRegistry;
	protected static XMLConfiguration config;
	protected static String file = "d:\\sango\\玩家數据\\9區\\20997\\bag";
	protected static int index = 0;
	
	public static void main(String[] args) throws Exception{
		init();
		parse(new FileInputStream(file));
	}
	
	protected static void parse(InputStream in) throws IOException{
		DataInputStream dis = new DataInputStream(in);
		int version = dis.readByte();
		index++;
		System.out.printf("version:%d\n", version);
		int size = dis.readInt();
		index += 4;
		System.out.printf("size:%d\n", size);
		int addedSize = 0;
		if(version>=3){
			addedSize = dis.readInt();
			index += 4;
			System.out.printf("addedsize:%d\n", addedSize);
		}
		int itemCount = 0;
		for (int i = 0; i < size + addedSize; i++) {
			int id = dis.readByte();
			index++;
			System.out.printf("id:%d\n", id);
			int count = (int)(dis.readByte() & 0xFF);
			index++;
			System.out.printf("count:%d\n", count);
			if (count > 0) {
				GameItem item;
				try {
					int itemId = dis.readInt();
					index += 4;
					System.out.printf("itemid:%d\n", itemId);
					ItemTemplate template = ObjectAccessor.getItemTemplate(itemId);
					if (template == null){
						System.out.printf("error:%d  itemcount:%d\n", index,itemCount);
						throw new IllegalArgumentException();
					}
					int leaveUseCount = dis.read();
					index++;
					System.out.printf("leaveusecount:%d\n", leaveUseCount);
					int validTime = dis.readInt();
					index += 4;
					System.out.printf("validtime:%d\n", validTime);
					int bindInstance = 0;
					if (version == 1) {
						boolean bind = dis.readBoolean();
						index += 1;
						System.out.printf("bind:%d\n", bind?1:0);
						bindInstance = bind ? 0 : -1;
					}
					else if(version>=2){
						bindInstance = dis.readInt();
						index += 4;
						System.out.printf("bindinstance:%d\n", bindInstance);
					}
					int duration = 0;
					if(template.isEquipment()){
						duration = dis.readShort();
						index += 2;
						System.out.printf("duration:%d\n", duration);
					}
					int instanceId = dis.readInt();
					index += 4;
					System.out.printf("instanceid:%d\n", instanceId);
					item = new GameItem(template,instanceId);
					item.instanceId = instanceId;
					item.bindInstance = bindInstance;
					item.leaveUseCount = leaveUseCount;
					item.duration = duration;
					item.validTime = validTime;
					if (version >= 2) {
						int marshallerId = dis.readInt();
						index += 4;
						System.out.printf("marshaller:%d\n", marshallerId);
						Marshaller m = PersistenceManager.marshaller(marshallerId);
						if (m != null) {
							item.object = m.marshaller(dis,item);
						}
					}
					itemCount ++;
					System.out.printf("index:%d",index);
					System.out.println("\n");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (PersistenceException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	protected static void init() throws Exception{
		config = new XMLConfiguration("peony.xml");
		serviceRegistry = new ServiceRegistry();
		HorsePersistence horsePersist = new HorsePersistence();
		PersistenceManager.registerMarshaller(horsePersist);
		PersistenceManager.registerSerializer(horsePersist);
		ItemEnhancePersistence itemEnhPersist = new ItemEnhancePersistence();
        PersistenceManager.registerMarshaller(itemEnhPersist);
        PersistenceManager.registerSerializer(itemEnhPersist);
        IMoneyCardPersistence imoneyCardPersist = new IMoneyCardPersistence();
        PersistenceManager.registerMarshaller(imoneyCardPersist);
        PersistenceManager.registerSerializer(imoneyCardPersist);
        
        DataService service = new DataService(new File(config.getString("datadir")),"PIP");
		serviceRegistry.addService(service);
		
		ItemUtil.load(service);
	}
	
	
}
