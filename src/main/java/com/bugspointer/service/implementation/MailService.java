package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Bug;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

    private static final String ADRESSE = "http://bugspointer.com/";

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
                "                   <a href='"+ ADRESSE +"confirmRegister/"+ publicKey + "' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Confirmer</a>" +
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
            return new Response(EnumStatus.OK, null, "Nous vous avons envoyé un e-mail à l'adresse : " + to);
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
                        "       <p>Description du bug : <br>" + newBug.getDescription() +"</p><br>" +
                        "       <p>Afin d'avoir plus d'informations sur ce bug, vous pouvez changer votre abonnement en cliquant sur le bouton ci-dessous:" +
                        "       <table border='0' cellpadding='0' cellspacing='0' >" +
                        "           <tr>" +
                        "               <td align='center' style='padding: 10px;'>" +
                        "                   <a href='"+ ADRESSE +"features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
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
            return new Response(EnumStatus.OK, null, "Mail gratuit avec détails envoyé");

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
                        "       <p>Vous avez atteint la limite gratuite d'un rapport de bug tout les 30 jours</p><br>" +
                        "       <p>Afin de connaitre les informations sur ce bug, changer d'offre en cliquant sur le bouton ci-dessous:" +
                        "       <table border='0' cellpadding='0' cellspacing='0' >" +
                        "           <tr>" +
                        "               <td align='center' style='padding: 10px;'>" +
                        "                   <a href='"+ ADRESSE +"/features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
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
            return new Response(EnumStatus.OK, null, "Mail gratuit sans détails envoyé");

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
                        "                   <a href='"+ ADRESSE +"resetPassword/"+ publicKey + "' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Réinitialiser mon mot de passe</a>" +
                        "               </td>" +
                        "           </tr>" +
                        "       </table>" +
                        "       <p>Ou copier et coller l'URL dans votre navigateur : </p>" +
                        "       <a href='"+ ADRESSE +"resetPassword/"+ publicKey + "'>"+ ADRESSE +"resetPassword/"+ publicKey +"</a>" +
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

    public Response sendMailTest(String to, Bug bugTest) {


        // Paramètres du destinataire
        String subject = "Bugspointer - Nouveau Bug (Test)";

        // Contenu HTML de l'email
        String htmlContent =
                "<html>" +
                        "  <body>" +
                        "    <h1> Vous venez d'envoyer un rapport de bug depuis la page de test du site bugspointer.com </h1>" +
                        "      <table>" +
                        "        <thead>" +
                        "        <tr>" +
                        "          <th>Pour une utilisation gratuite vous recevrez ce type de mail :</th>" +
                        "          </tr>" +
                        "        </thead>" +
                        "        <tbody>" +
                        "        <tr>" +
                        "          <td>" +
                        "            <h1>Nouveau Bug</h1><br>" +
                        "            <p>Un utilisateur vient de déclarer un nouveau bug sur votre site " +
                        "            <p>URL concernée : "+ bugTest.getUrl() +"</p><br>" +
                        "            <p>Description du bug : <br>" + bugTest.getDescription() +"</p><br>" +
                        "            <p>Afin d'avoir plus d'informations sur ce bug, vous pouvez changer votre abonnement en cliquant sur le bouton ci-dessous:" +
                        "            <table border='0' cellpadding='0' cellspacing='0' >" +
                        "              <tr>" +
                        "                <td align='center' style='padding: 10px;'>" +
                        "                  <a href='"+ ADRESSE +"features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
                        "                </td>" +
                        "              </tr>" +
                        "            </table>" +
                        "          </td>" +
                        "        </tr>" +
                        "        <tr>" +
                        "        </tbody>" +
                        "        <thead>" +
                        "            <tr>" +
                        "              <th>Voici le niveau de détail que vous recevrez pour une utilisation payante : </th>" +
                        "          </tr>" +
                        "         </thead>" +
                        "          <tbody>" +
                        "            <tr>" +
                        "               <td>" +
                        "                   <h1>Nouveau Bug</h1>" +
                        "                   <table>" +
                        "                       <tr>" +
                                "                <th>URL concernée : </th>" +
                                "                <td>"+ bugTest.getUrl() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th>Date du rapport : </th>" +
                                "                <td>"+ bugTest.getDateCreation() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th>Description du bug : </th>" +
                                "                <td>" + bugTest.getDescription() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th>OS utilisateur : </th>" +
                                "                <td>"  + bugTest.getOs() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th>Browser utilisateur : </th>" +
                                "                <td>"  + bugTest.getBrowser() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th>Taille écran utilisateur : </th>" +
                                "                <td>"  + bugTest.getScreenSize() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th>Code HTML sélectionné : </th>" +
                                "                <td><code>"  + bugTest.getCodeLocation() +"</code></td>" +
                                "              </tr>" +
                        "                   </table>" +
                                "            <p>Vous pouvez retrouver ces détails dans votre Dashboard en cliquant sur le bouton ci-dessous:" +
                                "            <table border='0' cellpadding='0' cellspacing='0' >" +
                                "              <tr>" +
                                "                <td align='center' style='padding: 10px;'>" +
                                "                  <a href='"+ ADRESSE +"app/private/dashboard' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Voir mon Dashboard</a>" +
                                "                </td>" +
                                "              </tr>" +
                                "            </table>" +
                                "          </td>" +
                        "               </tr>" +
                        "               <tr>" +
                        "                   <td>" +
                        "                       <table border='0' cellpadding='0' cellspacing='0' >" +
                        "                       <tr>" +
                        "                       <td align='center' style='padding: 10px;'>" +
                        "                           <a href='"+ ADRESSE +"sondage' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 20px; color: white; text-decoration: none; background-color: orange;'>Donner mon avis</a>" +
                        "                       </td>" +
                        "                   </tr>" +
                        "            </table>" +
                        "       </tbody>" +
                        "      </table>" +
                        "  </body>" +
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
            return new Response(EnumStatus.OK, null, "Mail avec le bug sur page de test envoyé");

        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("error from mail sender : " + e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }
}

