package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

@Service
@Slf4j
public class MailService {

    // Paramètres de connexion au serveur SMTP
    @Value("${mail.smtp}")
    private static String host;

    @Value("${mail.port}")
    private static int port;

    @Value("${mail.user}")
    private static String user;

    @Value("${mail.password}")
    private static String password;

    public Response sendMailRegister(String to, String publicKey) {


        // Paramètres du destinataire
        String subject = "Bugspointer - Confirmation d'inscription";

        // Contenu HTML de l'email
        String htmlContent =
                "<html>" +
                "   <body>" +
                "       <h1>Confirmation</h1><br>" +
                "       <p>Merci de votre inscription à Bugspointer " +
                "       <p>Afin de confirmer votre inscription, merci de cliquer sur le bouton ci-dessous:</p>" +
                "       <table border='0' cellpadding='0' cellspacing='0' >" +
                "           <tr>" +
                "               <td align='center' style='padding: 10px;'>" +
                "                   <a href='https://www.bugspointer.com/confirmRegister/"+ publicKey + "' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Confirmer</a>" +
                "               </td>" +
                "           </tr>" +
                "       </table>" +
                "   </body>" +
                "</html>";


        // Configuration des propriétés
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Création de l'authentificateur
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        // Création de la session
        Session session = Session.getInstance(properties, auth);

        try {
            // Création du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html");

            // Envoi du message
            Transport.send(message);

            log.info("email register sent at : {}", to);
            return new Response(EnumStatus.OK, null, "Vous avez un nouvel e-mail ");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("error from mail sender : " + e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }
}

