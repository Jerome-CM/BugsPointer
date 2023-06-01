package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Bug;
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
    private String host;

    @Value("${mail.port}")
    private int port;

    @Value("${mail.user}")
    private String user;

    @Value("${mail.password}")
    private String password;

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
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            // Envoi du message
            Transport.send(message);

            log.info("email register sent at : {}", to);
            return new Response(EnumStatus.OK, null, "Vous avez un nouvel e-mail");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("error from mail sender : " + e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }

    public Response sendMailNewBugDetail(String to, Bug newBug) {


        // Paramètres du destinataire
        String subject = "Bugspointer - Nouveau Bug";

        // Contenu HTML de l'email
        String htmlContent =
                "<html>" +
                        "   <body>" +
                        "       <h1>Nouveau Bug</h1><br>" +
                        "       <p>Un utilisateur vient de déclarer un nouveau bug sur votre site " +
                        "       <p>URL concernée : "+ newBug.getUrl() +"</p><br>" +
                        "       <p>Description donnée : <br>" + newBug.getDescription() +"</p><br>" +
                        "       <p>Afin d'avoir plus d'information, n'hésitez pas à vous abonner en cliquant sur le bouton, ci-dessous:" +
                        "       <table border='0' cellpadding='0' cellspacing='0' >" +
                        "           <tr>" +
                        "               <td align='center' style='padding: 10px;'>" +
                        "                   <a href='https://www.bugspointer.com/features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
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
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            // Envoi du message
            Transport.send(message);

            log.info("email new bug sent at : {}", to);
            return new Response(EnumStatus.OK, null, "Mail envoyé");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("error from mail sender : " + e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }

    public Response sendMailNewBugNoDetail(String to) {


        // Paramètres du destinataire
        String subject = "Bugspointer - Nouveau Bug";

        // Contenu HTML de l'email
        String htmlContent =
                "<html>" +
                        "   <body>" +
                        "       <h1>Nouveau Bug</h1><br>" +
                        "       <p>Un utilisateur vient de déclarer un nouveau bug sur votre site " +
                        "       <p>Vous avez atteint la limite d'un rapport de bug tout les 30 jours</p><br>" +
                        "       <p>Afin de connaitre les informations sur ce bug, abonnez-vous en cliquant sur le bouton, ci-dessous:" +
                        "       <table border='0' cellpadding='0' cellspacing='0' >" +
                        "           <tr>" +
                        "               <td align='center' style='padding: 10px;'>" +
                        "                   <a href='https://www.bugspointer.com/features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
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
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            // Envoi du message
            Transport.send(message);

            log.info("email new bug sent at : {}", to);
            return new Response(EnumStatus.OK, null, "Mail envoyé");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("error from mail sender : " + e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }

    public Response sendMailLostPassword(String to, String publicKey) {

        // Paramètres du destinataire
        String subject = "Bugspointer - Réinitisation mot de passe";

        // Contenu HTML de l'email
        String htmlContent =
                "<html>" +
                        "   <body>" +
                        "       <h1>Réinitialiser le mot de passe</h1><br>" +
                        "       <p>Bonjour,<br> " +
                        "       <p>Vous avez oublié votre mot de passe ?</p><br>" +
                        "       <p>Réinitialiser votre mot de passe en cliquant sur le bouton, ci-dessous:" +
                        "       <table border='0' cellpadding='0' cellspacing='0' >" +
                        "           <tr>" +
                        "               <td align='center' style='padding: 10px;'>" +
                        "                   <a href='https://www.bugspointer.com/resetPassword/"+ publicKey + "' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Réinitialiser mon mot de passe</a>" +
                        "               </td>" +
                        "           </tr>" +
                        "       </table>" +
                        "       <p>Ou copier et coller l'URL dans votre navigateur : </p>" +
                        "       <a href='https://www.bugspointer.com/resetPassword/"+ publicKey + "'>https://www.bugspointer.com/resetPassword/"+ publicKey +"</a>" +
                        "       <br>" +
                        "       <p>Si vous n'avez pas demandé un nouveau mot de passe, veuillez ignorer ce message" +
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
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            // Envoi du message
            Transport.send(message);

            log.info("email new bug sent at : {}", to);
            return new Response(EnumStatus.OK, null, "Un mail pour réinitialiser votre mot de passe vient de vous êtes envoyé");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("error from mail sender : " + e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }
}

