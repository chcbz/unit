package net.chcbz.util.mail;

import java.util.Timer;
import java.util.TimerTask;

import net.chcbz.util.date.DateHelper;
import net.chcbz.util.web.WebHelper;

public class MailListen{
	static String lastReadTime = DateHelper.getStringDate();
	
	static class MailTask extends TimerTask{
		public void run() {
			try {
//				System.out.println("start.........");
				ReceiveMail rm = new ReceiveMail();
				HandleMessage[] mails = rm.doReceiveByIMap("imap.163.com", "dongan_server@163.com", "99=a+b-c", null);
				for(int i=0;i<mails.length;i++){
					if(DateHelper.isDateBefore(lastReadTime, mails[i].getSentDate())){
						lastReadTime = mails[i].getSentDate();
						String from = mails[i].getFrom();
						if(mails[i].getSubject().toLowerCase().equals("getserverip")){
							SendMail.doSend("系统回复:服务器IP地址", "服务器 IP地址为:"+WebHelper.getRouteIp(null,"admin","22346699"), "dongan_server@163.com", from.substring(from.indexOf("<")+1,from.length()-1), "dongan_server", "99=a+b-c", "smtp.163.com");
							mails[i].doDel();
						}
					}
				}
				rm.close();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args){
		Timer timer = new Timer();
		timer.schedule(new MailTask(), 1000, 60000);
	}
}
