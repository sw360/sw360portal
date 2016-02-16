package com.siemens.sw360.mail;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.siemens.sw360.datahandler.common.CommonUtils;
import org.apache.log4j.Logger;

/**
 * Provides the possiblity to send mail from SW360
 *
 * @author birgit.heydenreich@tngtech.com
 */
public class MailUtil {

    private static final Logger log = Logger.getLogger(MailUtil.class);

    public static final String MAIL_PROPERTIES_FILE_PATH = "/sw360.properties";
    private Properties loadedProperties;
    private Session session;

    private String from;
    private String host;
    private String port;
    private String isAuthenticationNecessary;
    private String login;
    private String password;

    private String subject;
    private String text;

    public MailUtil() {
        loadedProperties = CommonUtils.loadProperties(MailUtil.class, MAIL_PROPERTIES_FILE_PATH);
        setBasicProperties();
        setSession();
    }

    private void setBasicProperties() {
        from = loadedProperties.getProperty("MailUtil_from", "");
        host = loadedProperties.getProperty("MailUtil_host", "localhost");
        port = loadedProperties.getProperty("MailUtil_port", "25");
        isAuthenticationNecessary = loadedProperties.getProperty("MailUtil_isAuthenticationNecessary", "true");
        login = loadedProperties.getProperty("MailUtil_login", "");
        password = loadedProperties.getProperty("MailUtil_password", "");
    }

    private void setSession() {
        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        properties.setProperty("mail.smtp.auth", isAuthenticationNecessary);
        properties.setProperty("mail.smtp.starttls.enable", "true");

        if (Boolean.parseBoolean(isAuthenticationNecessary)) {
            Authenticator auth = new SMTPAuthenticator(login, password);
            session = Session.getInstance(properties, auth);
        } else {
            session = Session.getDefaultInstance(properties);
        }
    }

    public void sendMail(String recipient, String subjectNameInPropertiesFile, String textNameInPropertiesFile) {
        loadSubjectAndText(subjectNameInPropertiesFile,textNameInPropertiesFile);
        sendMailWithSubjectAndText(recipient);
    }

    public void sendMail(Set<String> recipients, String subjectNameInPropertiesFile, String textNameInPropertiesFile) {
        loadSubjectAndText(subjectNameInPropertiesFile,textNameInPropertiesFile);
        for (String recipient : recipients) {
            sendMailWithSubjectAndText(recipient);
        }
    }

    private void loadSubjectAndText(String subjectKeyInPropertiesFile, String textKeyInPropertiesFile){
        subject=loadedProperties.getProperty(subjectKeyInPropertiesFile,"");
        text=loadedProperties.getProperty("defaultBegin","")
            +loadedProperties.getProperty(textKeyInPropertiesFile,"")
            +loadedProperties.getProperty("defaultEnd","");
    }

    private void sendMailWithSubjectAndText(String recipient) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);

            log.info("Sent message successfully....");

        } catch (MessagingException mex) {
            log.error(mex.getMessage());
        }
    }

    private class SMTPAuthenticator extends Authenticator {
        private PasswordAuthentication authentication;

        public SMTPAuthenticator(String login, String password) {
            authentication = new PasswordAuthentication(login, password);
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return authentication;
        }
    }
}
