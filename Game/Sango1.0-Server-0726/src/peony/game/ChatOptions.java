package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatOptions {
//	世界聊	默认代码1，可定义	默认不提示
//	国家聊	默认代码8，可定义	默认不提示
//	同乡聊	默认代码6，可定义	默认不提示
//	地区聊	默认代码7，可定义	默认不提示
//	军团聊	默认代码5，可定义	默认不提示
//	私人聊	默认代码9，可定义	默认提示
//	小队聊	默认代码15，可定义	默认提示
//	系统消息	默认代码4，不可定义	默认提示,不可定义
	
	public ChatOption[] options = new ChatOption[8];
	public String nativeName = "";

	public static ChatOptions newDefaultChatOptions(){
		ChatOptions ret = new ChatOptions();
		ret.options[ChatOption.WORLD] = new ChatOption(true,false,0);
		ret.options[ChatOption.FACTION] = new ChatOption(true,false,7);
		ret.options[ChatOption.NATIVE] = new ChatOption(true,false,5);
		ret.options[ChatOption.AREA] = new ChatOption(true,false,6);
		ret.options[ChatOption.GUILD] = new ChatOption(true,false,4);
		ret.options[ChatOption.PRIVATE] = new ChatOption(true,true,8);
		ret.options[ChatOption.PARTY] = new ChatOption(true,true,14);
		ret.options[ChatOption.SYSTEM] = new ChatOption(true,true,3);
		return ret;
	}

	@Override
	public ChatOptions clone(){
		ChatOptions ret = new ChatOptions();
		for(int i=0;i<options.length;i++){
			ret.options[i] = options[i].clone();
		}
		ret.nativeName = nativeName;
		return ret;
	}
	
	public byte[] toClientBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			for(int i=0;i<options.length;i++){
				dos.write(options[i].getClientByte());
			}
			dos.writeUTF(nativeName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(1);//version;
			for(int i=0;i<options.length;i++){
				ChatOption option = options[i];
				dos.writeBoolean(option.inChannel);
				dos.writeBoolean(option.notify);
				dos.write(option.color);
			}
			dos.writeUTF(nativeName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static ChatOptions getFromDb(byte[] bytes){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		ChatOptions ret = new ChatOptions();
		try {
			dis.read();//version;
			for(int i=0;i<8;i++){
				boolean inChannel = dis.readBoolean();
				boolean notify = dis.readBoolean();
				int color = dis.read();
				ret.options[i] = new ChatOption(inChannel,notify,color);
			}
			ret.nativeName = dis.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
