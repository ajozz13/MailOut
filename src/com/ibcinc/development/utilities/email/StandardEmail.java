// $Id: StandardEmail.java,v 1.8 2013-03-08 22:40:17 aochoa Exp $

package com.ibcinc.development.utilities.email;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;
import java.util.*;
import java.io.File;

public class StandardEmail extends Thread{
	
	protected InternetAddress[] recipients;
	protected String message;
	protected String subject;
	protected String from;
	protected String header;
	protected String headerValue;
	protected boolean hasHeader = false;
	protected boolean debug = false;
	protected boolean hasAttachment = false;
	private boolean AUTH = false;
	protected String userName = "";
	protected String pass = "";
	protected String smtpServer = "smtp.server.com";
        protected int smtpPort = 25;
	protected ArrayList<File> files_to_attach;
	private Exception messageException = null;
	protected boolean hasMessageConfirmation = false;
	
	public StandardEmail(){
	}
	
	public StandardEmail(String theRecipients, String message, String subject, String from)
	  throws Exception{
		setRecipients(theRecipients);
		setMessage(message);
		setSubject(subject);
		setFrom(from);
	}	
	
	//Insert recipients separated by ; or by ,
	public void setRecipients(String theRecipients) throws Exception{
		try{
			boolean hasSeparators = false;	
			String emailAddress;
			String def_separator = ";"; 
			
			if(theRecipients.indexOf(";") > 0){
				hasSeparators = true;
			}else if(theRecipients.indexOf(",") > 0){
				def_separator = ",";
				hasSeparators = true;
			}
			
			if(hasSeparators){
				StringTokenizer st = new StringTokenizer(theRecipients,def_separator);
				recipients = new InternetAddress[st.countTokens()];
				for(int i = 0; i<recipients.length;i++){
					emailAddress = st.nextToken().trim();
					if(isValidEmailAddress(emailAddress)){
						recipients[i] = new InternetAddress(emailAddress);
					}else{
						throw new Exception("Email Address " + emailAddress + " is invalid.");	
					}
				}
			}else{
				recipients = new InternetAddress[1];
				if(isValidEmailAddress(theRecipients)){
					recipients[0] = new InternetAddress(theRecipients);
				}else{
					throw new Exception("Email Address " + theRecipients + " is invalid.");	
				}	
			}
		}catch(Exception e){
			System.out.println("COULD NOT SET RECIPIENTS " + theRecipients);
			e.printStackTrace();
			throw(e);	
		}
	}
	
	public void setEmailHeader(String header, String headerValue){
		this.header = header;
		this.headerValue = headerValue;
		this.hasHeader = true;
	}
	
	public void setFrom(String from){
		this.from = from;
	}
	
	public void setSubject(String subject){
		this.subject = subject;	
	}
	
	public void setMessage(String message){
		this.message = message;	
	}
	
	public InternetAddress[] getRecipients(){
		return recipients;	
	}
	
	public String getMessage(){
		return message;	
	}
	
	public String getSubject(){
		return subject;	
	}
	
	public String getFrom(){
		return from;	
	}
	
	public void attachFiles(ArrayList<File> files){
		hasAttachment = true;
		files_to_attach = files;	
	}
	
	public void send() throws Exception{
		Message msg = prepareHeader();

                if(hasAttachment){
		//sendWithAttachment();
                    msg.setContent(getAttachmentMultiPart());
		}else{
                    msg.setContent(getMessage(),"text/plain");
                }

		//finally send the message
		Transport.send(msg);
	}
	
//	private void sendWithAttachment() throws Exception{
        protected MimeMultipart getAttachmentMultiPart() throws Exception{
	//	Message msg = prepareHeader();
		
		//>>>PREPARE MIME MULTIPART
		MimeMultipart mp = new MimeMultipart();
		
		//>>>>>>WRITE MESSAGE
		MimeBodyPart text = new MimeBodyPart();
		text.setDisposition(Part.INLINE);
		text.setContent(getMessage(),"text/plain");
		mp.addBodyPart(text);
		
		//>>>>>>ATTACH ATTACHMENTS...
		//for(int i=0; i<files_to_attach.size(); i++){
                for(File file : files_to_attach){
			MimeBodyPart file_part = new MimeBodyPart();
			//File file = (File)files_to_attach.elementAt(i);
			FileDataSource fds = new FileDataSource(file);
			DataHandler dh = new DataHandler(fds);
			file_part.setFileName(file.getName());
			file_part.setDisposition(Part.ATTACHMENT);
			file_part.setDescription("FILE="+file.getName());
			file_part.setDataHandler(dh);
			mp.addBodyPart(file_part);
		}
                return mp;
                //SET CONTENT WITH MULTIPARTS
	//	msg.setContent(mp);
		
		//finally send the message
	//	Transport.send(msg);
			
	}
	
	public Message prepareHeader() throws Exception{
		Properties Emailprops = new Properties();
                Emailprops.put("mail.smtp.host",getSMTPServer());
                Emailprops.put("mail.smtp.port", getSMTPPort());
		Session session;

		if(AUTH){
                        Emailprops.put("mail.smtp.auth","true");
                        Authenticator pauth = new SMTPAuthenticator(userName,pass);
			//session = Session.getDefaultInstance(Emailprops,pauth);
                        session = Session.getInstance(Emailprops, pauth);
		}else{
			//session = Session.getDefaultInstance(Emailprops,null);
                        session = Session.getInstance(Emailprops);
		}

                if(isDebugMode()){
			System.out.println("Message From: " + from);
			System.out.println("Subject: " + subject);
                        System.out.println("Using: " + Emailprops.get("mail.smtp.host"));
                        if(AUTH){
                            System.out.println("with Authentication");
			}
			System.out.print("To: ");
			for(int i=0; i<recipients.length;i++){
				System.out.println(recipients[i]);
			}
			System.out.println("Message: " + message);
                        session.setDebug(true);
		}

		Message msg = new MimeMessage(session);
		InternetAddress msgFrom = new InternetAddress(getFrom());
		msg.setFrom(msgFrom);
		msg.setSubject(getSubject());
		if(getHasHeader()){
			msg.addHeader(header,headerValue);	
		}
		msg.setRecipients(Message.RecipientType.TO, getRecipients());
		return msg;
	}
	
	public void run(){
		try{
                    send();
		}catch(Exception e){
			System.out.println("Exception caught @ StandardEmail.run()");
			e.printStackTrace();
			setMessageConfirmation(e);
		}
	}
	
	public void setMessageConfirmation(Exception e){
		hasMessageConfirmation = true;
		messageException = e;
	}
	
	public Exception getMessageConfirmation(){
		return messageException;	
	}
	
	public boolean hasException(){
		return hasMessageConfirmation;	
	}
	
	public void setDebug(boolean debug){
		this.debug = debug;	
	}
	
	public boolean isDebugMode(){
		return debug;	
	}

        public void setSMTPPort(int _port){
            this.smtpPort = _port;
        }

        public String getSMTPPort(){
            return String.valueOf(smtpPort);
        }

	public void setSMTPServer(String smtpServer){
		this.smtpServer = smtpServer;	
	}
	
	public String getSMTPServer(){
		return smtpServer;	
	}
	
	public boolean getHasHeader(){
		return hasHeader;	
	}
	
	public void setUserNameAndPass(String userName, String pass){
		AUTH = true;
		this.userName = userName;
		this.pass = pass;
	}
	
	public static boolean isValidEmailAddress(String anEmailAddress){
		if(anEmailAddress == null) return false;
		boolean result = true;
		try{
			InternetAddress emailAddr = new InternetAddress(anEmailAddress);
			if (!hasNameAndDomain(anEmailAddress)){
				result = false;
			}
		}
		catch(Exception ex){
			result = false;
		}finally{
			return result;
		}
	}
	
	private static boolean hasNameAndDomain(String anEmailAddress){
		StringTokenizer tok = new StringTokenizer(anEmailAddress, "@");
		return (tok.countTokens() == 2) ? true:false;
	}
	
	/*
	 *	Simple authenticator to do simple auth when the smtp server requires it.
	 **/
	
	protected class SMTPAuthenticator extends Authenticator{
		
		private String uName;
		private String passw;
		
		public SMTPAuthenticator(String uName, String passw){
			this.uName = uName;
			this.passw = passw;
		}
		
		public PasswordAuthentication getPasswordAuthentication(){
			return new PasswordAuthentication(uName,passw);	
		}
	}
}
