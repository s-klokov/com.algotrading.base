package com.algotrading.base.lib;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Класс для отправки сообщений по email.
 */
public class EmailSender {

    public final String login;
    private final Session session;

    public EmailSender(final String login,
                       final String password,
                       final String smtpAddress,
                       final String smtpPort,
                       final boolean useSsl,
                       final boolean useTls) {
        this.login = login;
        session = Session.getInstance(new Properties() {{
                                          put("mail.smtp.auth", "true");
                                          if (useSsl) {
                                              put("mail.smtp.socketFactory.port", smtpPort);
                                              put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                                              put("mail.smtp.socketFactory.fallback", "false");
                                          }
                                          if (useTls) {
                                              put("mail.smtp.starttls.enable", "true");
                                          }
                                          put("mail.smtp.host", smtpAddress);
                                          put("mail.smtp.port", smtpPort);
                                      }},
                                      new Authenticator() {
                                          @Override
                                          protected PasswordAuthentication getPasswordAuthentication() {
                                              return new PasswordAuthentication(login, password);
                                          }
                                      });
    }

    /**
     * Получить объект для отправки сообщений по email из файла конфигурации, имеющего вид:<br>
     * login<br>
     * password<br>
     * smtpAddress<br>
     * smtpPort<br>
     * ssl/tls-строка<br>,
     * где последняя строка содержит подстроку "ssl", если требуется использование ssl, и/или подстроку "tls",
     * если требуется ипользование tls, или отсутствует, если использование ssl/tls не требуется.
     *
     * @param configFileName имя файла с конфигурацией
     * @return объект для отправки сообщений по email
     * @throws IOException если произошла ошибка ввода-вывода при чтении конфигурационного файла
     */
    public static EmailSender newInstance(final String configFileName) throws IOException {
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configFileName), StandardCharsets.UTF_8))) {
            final String login = br.readLine();
            if (login == null) {
                throw new IOException("Login is null");
            }
            final String password = br.readLine();
            if (password == null) {
                throw new IOException("Password is null");
            }
            final String smtpAddress = br.readLine();
            if (smtpAddress == null) {
                throw new IOException("smtpAddress is null");
            }
            final String smtpPort = br.readLine();
            if (smtpPort == null) {
                throw new IOException("smtpPort is null");
            }
            String useSslTls = br.readLine();
            boolean useSsl = false;
            boolean useTls = false;
            if (useSslTls != null) {
                useSslTls = useSslTls.toLowerCase();
                useSsl = useSslTls.contains("ssl");
                useTls = useSslTls.contains("tls");
            }
            return new EmailSender(login, password, smtpAddress, smtpPort, useSsl, useTls);
        }
    }

    /**
     * Отправить email без вложений.
     *
     * @param from    отправитель
     * @param to      получатель
     * @param subject тема
     * @param text    текст
     * @throws MessagingException если при отправке сообщения произошла ошибка
     */
    public void sendEmail(final String from,
                          final String to,
                          final String subject,
                          final String text) throws MessagingException {
        sendEmail(from, to, subject, text, null);
    }

    /**
     * Отправить email с вложениями.
     *
     * @param from           отправитель
     * @param to             получатель
     * @param subject        тема
     * @param text           текст
     * @param attachmentList список имён файлов-вложений
     * @throws MessagingException если при отправке сообщения произошла ошибка
     */
    public void sendEmail(final String from,
                          final String to,
                          final String subject,
                          final String text,
                          final List<String> attachmentList) throws MessagingException {
        final Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        final InternetAddress[] address = {new InternetAddress(to)};
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(text);
        if (attachmentList != null && !attachmentList.isEmpty()) {
            final Multipart multipart = new MimeMultipart();
            final BodyPart textPart = new MimeBodyPart();
            textPart.setText(text);
            multipart.addBodyPart(textPart);
            int counter = 0;
            for (final String fileName : attachmentList) {
                final File file = new File(fileName);
                if (file.exists() && file.isFile()) {
                    final MimeBodyPart messageBodyPart = new MimeBodyPart();
                    final String attachmentName = file.getName();
                    final DataSource source = new FileDataSource(fileName);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(attachmentName);
                    multipart.addBodyPart(messageBodyPart);
                    counter++;
                }
            }
            if (counter > 0) {
                msg.setContent(multipart);
            }
        }
        Transport.send(msg);
    }
}
