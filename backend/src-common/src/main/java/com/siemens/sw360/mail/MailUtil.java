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
    private String enableStarttls;
    private String login;
    private String password;
    private String enableSsl;
    private String enableDebug;


    public MailUtil() {
        loadedProperties = CommonUtils.loadProperties(MailUtil.class, MAIL_PROPERTIES_FILE_PATH);
        setBasicProperties();
        setSession();
    }

    private void setBasicProperties() {
        from = loadedProperties.getProperty("MailUtil_from", "");
        host = loadedProperties.getProperty("MailUtil_host", "");
        port = loadedProperties.getProperty("MailUtil_port", "25");
        enableStarttls = loadedProperties.getProperty("MailUtil_enableStarttls", "false");
        enableSsl = loadedProperties.getProperty("MailUtil_enableSsl", "false");
        isAuthenticationNecessary = loadedProperties.getProperty("MailUtil_isAuthenticationNecessary", "true");
        login = loadedProperties.getProperty("MailUtil_login", "");
        password = loadedProperties.getProperty("MailUtil_password", "");
        enableDebug = loadedProperties.getProperty("MailUtil_enableDebug", "false");
    }

    private void setSession() {
        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);
        properties.setProperty("mail.smtp.auth", isAuthenticationNecessary);
        properties.setProperty("mail.smtp.starttls.enable", enableStarttls);
        properties.setProperty("mail.smtp.ssl.enable", enableSsl);

        properties.setProperty("mail.debug", enableDebug);

        if (isAuthenticationNecessary!="false") {
            Authenticator auth = new SMTPAuthenticator(login, password);
            session = Session.getInstance(properties, auth);
        } else {
            session = Session.getDefaultInstance(properties);
        }
    }

    public void sendMail(String recipient, String subjectNameInPropertiesFile, String textNameInPropertiesFile) {
        if (isMailingEnabledAndValid()) {
            MimeMessage messageWithSubjectAndText = makeMessageWithSubjectAndText(subjectNameInPropertiesFile, textNameInPropertiesFile);
            sendMailWithSubjectAndText(recipient, messageWithSubjectAndText);
        }
    }

    public void sendMail(Set<String> recipients, String subjectNameInPropertiesFile, String textNameInPropertiesFile) {
        if (!isMailingEnabledAndValid()) {
            MimeMessage messageWithSubjectAndText = makeMessageWithSubjectAndText(subjectNameInPropertiesFile, textNameInPropertiesFile);
            for (String recipient : recipients) {
                sendMailWithSubjectAndText(recipient, messageWithSubjectAndText);
            }
        }
    }

    private boolean isMailingEnabledAndValid() {
        if (host == "") {
            return false; //e-mailing is disabled
        }
        if (isAuthenticationNecessary!="false" && login == "") {
            log.error("Cannot send emails: authentication necessary, but login is not set.");
            return false;
        }
        return true;

    }

    private MimeMessage makeMessageWithSubjectAndText(String subjectKeyInPropertiesFile, String textKeyInPropertiesFile) {
        MimeMessage message = new MimeMessage(session);

        String subject = loadedProperties.getProperty(subjectKeyInPropertiesFile, "");
        String text = loadedProperties.getProperty("defaultBegin", "")
                + loadedProperties.getProperty(textKeyInPropertiesFile, "")
                + loadedProperties.getProperty("defaultEnd", "");
        try {
            message.setSubject(subject);
            message.setText(text);
        } catch (MessagingException mex) {
            log.error(mex.getMessage());
        }

        return message;
    }

    private void sendMailWithSubjectAndText(String recipient, MimeMessage message) {
        try {
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            Transport.send(message);

            log.info("Sent message successfully to user "+recipient+".");

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
