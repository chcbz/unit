package net.chcbz.util.mail;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMail {
	private static final Logger logger = LoggerFactory.getLogger(SendMail.class);

	private MimeMessage mimeMsg; // MIME邮件对象
	private Session session; // 邮件会话对象
	private Properties props; // 系统属性
//	private boolean needAuth = false; // smtp是否需要认证
	private String username = ""; // smtp认证用户名和密码
	private String password = "";
	private Multipart mp; // Multipart对象,邮件内容,标题,附件等内容均添加到其中后再生成MimeMessage对象

	public SendMail() {
		// setSmtpHost(getConfig.mailHost);// 如果没有指定邮件服务器,就从getConfig类中获取
		setSmtpHost("smtp.126.com");// 如果没有指定邮件服务器,就从getConfig类中获取
		createMimeMessage();
	}

	public SendMail(String smtp) {
		setSmtpHost(smtp);
		createMimeMessage();
	}

	/**
	 * 
	 * @param hostName
	 *            String
	 * 
	 */
	public void setSmtpHost(String hostName) {
		logger.info("设置系统属性：mail.smtp.host = " + hostName);
		if (props == null)
			props = System.getProperties(); // 获得系统属性对象
		props.put("mail.smtp.host", hostName); // 设置SMTP主机
	}

	/**
	 * 
	 * @return boolean
	 * 
	 */
	public boolean createMimeMessage(){
		try {
			logger.info("准备获取邮件会话对象！");
			session = Session.getDefaultInstance(props, null); // 获得邮件会话对象
		}catch (Exception e) {
			logger.error("获取邮件会话对象时发生错误！" + e);
			return false;
		}
		logger.info("准备创建MIME邮件对象！");

		try {
			mimeMsg = new MimeMessage(session); // 创建MIME邮件对象
			mp = new MimeMultipart();
			return true;
		}catch (Exception e) {
			logger.error("创建MIME邮件对象失败！",e);
			return false;
		}
	}

	/**
	 * 
	 * @param need
	 *            boolean
	 * 
	 */
	public void setNeedAuth(boolean need) {
		logger.info("设置smtp身份认证：mail.smtp.auth = " + need);
		if (props == null)
			props = System.getProperties();
		if (need) {
			props.put("mail.smtp.auth", "true");
		} else {
			props.put("mail.smtp.auth", "false");
		}
	}

	/**
	 * 
	 * @param name
	 *            String
	 * 
	 * @param pass
	 *            String
	 * 
	 */
	public void setNamePass(String name, String pass) {
		username = name;
		password = pass;
	}

	/**
	 * 
	 * @param mailSubject
	 *            String
	 * 
	 * @return boolean
	 * 
	 */
	public boolean setSubject(String mailSubject) {
		logger.info("设置邮件主题！");
		try {
			mimeMsg.setSubject(mailSubject);
			return true;
		}catch (Exception e) {
			logger.error("设置邮件主题发生错误！",e);
			return false;
		}
	}

	/**
	 * 
	 * @param mailBody
	 *            String
	 * 
	 */
	public boolean setBody(String mailBody) {
		try {
			BodyPart bp = new MimeBodyPart();
			bp.setContent(
					"<meta http-equiv=Content-Type content=text/html; charset=gb2312>"
							+ mailBody, "text/html;charset=GB2312");
			mp.addBodyPart(bp);
			return true;
		}catch (Exception e) {
			logger.error("设置邮件正文时发生错误！", e);
			return false;
		}
	}

	/**
	 * 
	 * @param name
	 *            String
	 * 
	 * @param pass
	 *            String
	 * 
	 */
	public boolean addFileAffix(String filename) {
		logger.info("增加邮件附件：" + filename);
		try {
			BodyPart bp = new MimeBodyPart();
			FileDataSource fileds = new FileDataSource(filename);
			bp.setDataHandler(new DataHandler(fileds));
			bp.setFileName(fileds.getName());
			mp.addBodyPart(bp);
			return true;
		}catch (Exception e) {
			logger.error("增加邮件附件：" + filename + "发生错误！", e);
			return false;
		}
	}

	/**
	 * 
	 * @param name
	 *            String
	 * 
	 * @param pass
	 *            String
	 * 
	 */
	public boolean setFrom(String from) {
		logger.info("设置发信人！");
		try {
			mimeMsg.setFrom(new InternetAddress(from)); // 设置发信人
			return true;
		}catch (Exception e){
			return false;
		}
	}

	/**
	 * 
	 * @param name
	 *            String
	 * 
	 * @param pass
	 *            String
	 * 
	 */
	public boolean setTo(String to) {
		if (to == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress
					.parse(to));
			return true;
		}catch (Exception e){
			return false;
		}
	}

	/**
	 * 
	 * @param name
	 *            String
	 * 
	 * @param pass
	 *            String
	 * 
	 */
	public boolean setCopyTo(String copyto){
		if (copyto == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.CC,
					(Address[]) InternetAddress.parse(copyto));
			return true;
		}catch (Exception e){
			return false;
		}
	}

	/**
	 * 
	 * @param name
	 *            String
	 * 
	 * @param pass
	 *            String
	 * 
	 */
	public boolean sendout(){
		try {
			mimeMsg.setContent(mp);
			mimeMsg.saveChanges();
			logger.info("正在发送邮件....");
			Session mailSession = Session.getInstance(props, null);
			Transport transport = mailSession.getTransport("smtp");
			transport.connect((String) props.get("mail.smtp.host"), username,
					password);
			transport.sendMessage(mimeMsg, mimeMsg
					.getRecipients(Message.RecipientType.TO));
			// transport.send(mimeMsg);
			logger.info("发送邮件成功！");
			transport.close();
			return true;
		}catch (Exception e){
			logger.error("邮件发送失败！", e);
			return false;
		}
	}
	
	/**
	 * 发送邮件的静态方法
	 * @param title 邮件标题
	 * @param content 邮件主体内容
	 * @param from 邮件的发送方
	 * @param to 邮件的接收方
	 * @param name 发送方邮箱的用户名
	 * @param password 发送方邮箱的密码
	 * @param smtp 发送方邮箱的SMTP服务器地址,如smtp.qq.com
	 * @return
	 */
	public static boolean doSend(String title, String content, String from, String to, String name, String password, String smtp){
		String mailbody = "<meta http-equiv=Content-Type content=text/html; charset=gb2312>"
			+content;
		SendMail themail = new SendMail(smtp);
		themail.setNeedAuth(true);
		if (themail.setSubject(title) == false)
			return false;
		if (themail.setBody(mailbody) == false)
			return false;
		if (themail.setTo(to) == false)
			return false;
		if (themail.setFrom(from) == false)
			return false;
		themail.setNamePass(name, password);
		if (themail.sendout() == false)
			return false;
		return true;
	}
	
	/**
	 * 发送邮件的静态方法,可以发送附件
	 * @param title 邮件标题
	 * @param content 邮件主体内容
	 * @param from 邮件的发送方
	 * @param to 邮件的接收方
	 * @param affixPath 附件的绝对地址
	 * @param name 发送方邮箱的用户名
	 * @param password 发送方邮箱的密码
	 * @param smtp 发送方邮箱的SMTP服务器地址,如smtp.qq.com
	 * @return
	 */
	public static boolean doSend(String title, String content, String from, String to, String affixPath, String name, String password, String smtp){
		String mailbody = "<meta http-equiv=Content-Type content=text/html; charset=gb2312>"
			+content;
		SendMail themail = new SendMail(smtp);
		themail.setNeedAuth(true);
		if (themail.setSubject(title) == false)
			return false;
		if (themail.setBody(mailbody) == false)
			return false;
		if (themail.setTo(to) == false)
			return false;
		if (themail.setFrom(from) == false)
			return false;
		if (themail.addFileAffix(affixPath) == false)
			return false;
		themail.setNamePass(name, password);
		if (themail.sendout() == false)
			return false;
		return true;
	}

	/**
	 * 
	 * Just do it as this
	 * 
	 */
	public static void main(String[] args) {
		SendMail.doSend("s60sign有用户提出问题", "问题为:", "server@328g.com", "chcbz@sina.com", "server@328g.com", "328g_service", "smtp.328g.com");
	}
}