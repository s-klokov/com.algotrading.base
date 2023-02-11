package com.algotrading.base.lib;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Класс для отправки сообщений по email.
 */
public class EmailSender {

    public final String login;
    private final Session session;

    /**
     * Создать экземпляр класса для отправки почты.
     *
     * @param propertiesFileName имя файла с настройками
     * @return экземпляр класса для отправки почты
     * @throws IOException если произошла ошибка ввода-вывода при чтении файла настроек
     */
    public static EmailSender newInstance(final String propertiesFileName) throws IOException {
        final Properties properties = new Properties();
        try (final BufferedReader br = Files.newBufferedReader(Path.of(propertiesFileName))) {
            properties.load(br);
        }
        return new EmailSender(properties);
    }

    /**
     * Создать экземпляр класса для отправки почты.
     * <p>
     * Пример файла настроек:
     * <pre>
     * mail.smtp.user = user@yandex.ru
     * mail.smtp.password = pa$$W0rd
     * mail.transport.protocol = smtps
     * mail.smtp.host = smtp.yandex.ru
     * mail.smtp.port = 465
     * mail.smtp.ssl.enable = true
     * mail.smtp.auth = true
     * mail.smtp.ssl.trust=yandex.ru (для самоподписанных сертификатов)
     * </pre>
     *
     * @param properties файл с настройками
     */
    public EmailSender(final Properties properties) {
        login = Objects.requireNonNull(properties.getProperty("mail.smtp.user"));
        final String password = Objects.requireNonNull(properties.getProperty("mail.smtp.password"));
        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(login, password);
            }
        });
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
