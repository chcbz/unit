package net.chcbz.util.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.QDecoderStream;

/**
 * 有一封邮件就需要建立一个ReciveMail对象
 */
public class ReceiveMail {
	private Store store;
	private Folder folder;
	
	public void close(){
		try {
			folder.close(true);
			store.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public HandleMessage[] doReceive(String host, String username, String password, String attachPath) {
		Properties props = System.getProperties();
//		props.put("mail.smtp.host", "smtp.163.com");
//		props.put("mail.smtp.auth", "true");
		props.setProperty("mail.pop3.disabletop", "true");
		Session session = Session.getDefaultInstance(props, null);
		URLName urln = new URLName("pop3", host, 110, null,
				username, password);
		try{
			store = session.getStore(urln);
			store.connect();
			folder = store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			Message message[] = folder.getMessages();
			HandleMessage[] pmm = new HandleMessage[message.length];
			for (int i = 0; i < message.length; i++) {
				pmm[i] = new HandleMessage((MimeMessage) message[i]);
				if(attachPath!=null && new File(attachPath).exists()){
					pmm[i].setAttachPath(attachPath);
					pmm[i].saveAttachMent((Part) message[i]);
				}
			}
			return pmm;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public HandleMessage[] doReceiveByIMap(String host, String username, String password, String attachPath) {
		Properties prop = System.getProperties();
		prop.put("mail.imap.host", host);
		prop.put("mail.imap.auth.plain.disable", "true");
		Session mailsession = Session.getInstance(prop, null);
		mailsession.setDebug(false);
		int total = 0;
		URLName urln = new URLName("imap", host, 143, null,
				username, password);
		try {
			store = (IMAPStore) mailsession.getStore(urln);
			store.connect(host, username, password);
			folder = (IMAPFolder) store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			total = folder.getMessageCount();
		} catch (MessagingException ex) {
			System.err.println("不能以读写方式打开邮箱!");
			ex.printStackTrace();
		}
		try {
			HandleMessage[] pmm = new HandleMessage[total];
			for (int i = 0; i < total; i++) {
				IMAPMessage message = (IMAPMessage) folder.getMessage(i+1);
				pmm[i] = new HandleMessage(message);
				if(attachPath!=null && new File(attachPath).exists()){
					pmm[i].setAttachPath(attachPath);
					pmm[i].saveAttachMent((Part) message);
				}
			}
			return pmm;
		} catch (Exception bs) {
			bs.printStackTrace();
			return null;
		}

	}

	/**
	 * PraseMimeMessage类测试
	 */
	public static void main(String args[]) throws Exception {
		Properties props = System.getProperties();
//		props.put("mail.smtp.host", "smtp.163.com");
//		props.put("mail.smtp.auth", "true");
		props.setProperty("mail.pop3.disabletop", "true");
		Session session = Session.getDefaultInstance(props, null);
		URLName urln = new URLName("pop3", "pop.163.com", 110, null,
				"dongan_server@163.com", "99=a+b-c");
		Store store = session.getStore(urln);
		store.connect();
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);
		Message message[] = folder.getMessages();
		System.out.println("Messages's length: " + message.length);
		HandleMessage pmm = null;
		for (int i = 0; i < message.length; i++) {
			System.out.println("======================");
			pmm = new HandleMessage((MimeMessage) message[i]);
			System.out
					.println("Message " + i + " subject: " + pmm.getSubject());
			System.out.println("Message " + i + " sentdate: "
					+ pmm.getSentDate());
			System.out.println("Message " + i + " replysign: "
					+ pmm.getReplySign());
			System.out.println("Message " + i + " hasRead: " + pmm.isNew());
			System.out.println("Message " + i + "  containAttachment: "
					+ pmm.isContainAttach((Part) message[i]));
			System.out.println("Message " + i + " form: " + pmm.getFrom());
			System.out.println("Message " + i + " to: "
					+ pmm.getMailAddress("to"));
			System.out.println("Message " + i + " cc: "
					+ pmm.getMailAddress("cc"));
			System.out.println("Message " + i + " bcc: "
					+ pmm.getMailAddress("bcc"));
			pmm.setDateFormat("yy年MM月dd日 HH:mm");
			System.out.println("Message " + i + " sentdate: "
					+ pmm.getSentDate());
			System.out.println("Message " + i + " Message-ID: "
					+ pmm.getMessageId());
			// 获得邮件内容===============
			pmm.getMailContent((Part) message[i]);
			System.out.println("Message " + i + " bodycontent: \r\n"
					+ pmm.getBodyText());
			pmm.setAttachPath("e:\\");
			pmm.saveAttachMent((Part) message[i]);
		}
	}

	public static String decodeWord(String s) {
		if (!s.startsWith("=?"))
			return s;
		int i = 2;
		int j;
		if ((j = s.indexOf(63, i)) == -1)
			return s;
		// String s1 = (s.substring(i, j));
		i = j + 1;
		if ((j = s.indexOf(63, i)) == -1)
			return s;
		String s2 = s.substring(i, j);
		i = j + 1;
		if ((j = s.indexOf("?=", i)) == -1)
			return s;
		String s3 = s.substring(i, j);
		try {
			ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(
					s3.getBytes());
			Object obj;
			if (s2.equalsIgnoreCase("B"))
				obj = new BASE64DecoderStream(bytearrayinputstream);
			else if (s2.equalsIgnoreCase("Q"))
				obj = new QDecoderStream(bytearrayinputstream);
			else
				return s;
			int k = bytearrayinputstream.available();
			byte abyte0[] = new byte[k];
			k = ((InputStream) (obj)).read(abyte0, 0, k);
			return new String(abyte0, 0, k);
		} catch (Exception ex) {
			return s;
		}
	}
}