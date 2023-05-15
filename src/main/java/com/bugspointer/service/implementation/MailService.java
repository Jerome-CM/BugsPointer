package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;


@Service
public class ElasticMailSender {

    public Response sendMailRegister() {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("bugspointertest@gmail.com", "Example.com Admin"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("bouteveillejerome@hotmail.fr", "Mr. User"));
            msg.setSubject("Your Example.com account has been activated");
            msg.setText("This is a test");
            Transport.send(msg);
            return new Response(EnumStatus.OK, null, "Message sended");
        } catch (AddressException e) {
            return new Response(EnumStatus.ERROR, null, "Message address failed" + e.getMessage());
        } catch (MessagingException e) {
            return new Response(EnumStatus.ERROR, null, "Message Messaging failed" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            return new Response(EnumStatus.ERROR, null, "Message Unsupported failed" + e.getMessage());
        }
    }
}

