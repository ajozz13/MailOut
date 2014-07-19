
package com.ibcinc.development.utilities.email;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 *
 * @author Alberto Ochoa
 */
public class SecureTransportEmail extends StandardEmail{

    public static final String SSL_TRANSPORT = "SSL";
    public static final String TSL_TRANSPORT = "TLS";
    private String SEL_TRANSPORT;

    protected Session mailSession;

    private SecureTransportEmail(){
        //super();
    }

    public SecureTransportEmail(String _SEL_TRANSPORT){
        SEL_TRANSPORT = _SEL_TRANSPORT;
    }

    public SecureTransportEmail(String theRecipients, String message, String subject, String from, String _SEL_TRANSPORT)
	  throws Exception{
            super(theRecipients, message, subject, from);
            SEL_TRANSPORT = _SEL_TRANSPORT;
    }

    public void send() throws Exception{
        Message msg = prepareHeader();
        if(hasAttachment){
        //sendWithAttachment();
            msg.setContent(getAttachmentMultiPart());
        }else{
            msg.setContent(getMessage(),"text/plain");
        }

        if(SEL_TRANSPORT.equals(SSL_TRANSPORT)){
            Transport transport = mailSession.getTransport();
            transport.connect(getSMTPServer(), this.smtpPort, this.userName, this.pass);
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
        }else{
            //TLS Transport here...
            Transport.send(msg);
        }
    }

    public Message prepareHeader() throws Exception{
        Properties props = new Properties();

        if(SEL_TRANSPORT.equals(SSL_TRANSPORT)){
            props.put("mail.transport.protocol", "smtps");
            props.put("mail.smtps.host", getSMTPServer());
            props.put("mail.smtps.auth", "true");
            //mailSession = Session.getDefaultInstance(props);
            mailSession = Session.getInstance(props);
        }else{
            //TLS
            props.put("mail.smtp.host", getSMTPServer());
            props.put("mail.smtp.port", getSMTPPort());
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.protocols","SSLv3 TLSv1");

            Authenticator auth = new SMTPAuthenticator(userName, pass);
            mailSession = Session.getInstance(props, auth);
        }

        

        if(isDebugMode()){
                System.out.println("Message From: " + from);
                System.out.println("Subject: " + subject);
                if(SEL_TRANSPORT.equals(SSL_TRANSPORT)){
                    System.out.println("Using SSL Host: " + props.get("mail.smtps.host"));
                }else{ //TLS
                    System.out.println("Using TLS Host: " + props.get("mail.smtp.host"));
                }
                System.out.print("To: ");
                for(int i=0; i<recipients.length;i++){
                        System.out.println(recipients[i]);
                }
                System.out.println("Message: " + message);
                mailSession.setDebug(true);
        }

        MimeMessage msg = new MimeMessage(mailSession);
        InternetAddress msgFrom = new InternetAddress(getFrom());
        msg.setFrom(msgFrom);
        msg.setSubject(getSubject());
        if(getHasHeader()){
            msg.addHeader(header,headerValue);
        }
        msg.setRecipients(Message.RecipientType.TO, getRecipients());
        return msg;
    }

}
