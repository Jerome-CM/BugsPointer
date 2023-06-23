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


    private final BugService bugService;

    public MailService(BugService bugService) {
        this.bugService = bugService;
    }

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
                "                   <a href='"+ ADRESSE +"confirmRegister/"+ publicKey + "' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 18px; color: white; text-decoration: none; background-color: orange;'>Confirmer</a>" +
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
                        "                   <a href='"+ ADRESSE +"features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 18px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
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
                        "                   <a href='"+ ADRESSE +"/features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 18px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
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

    public Response sendMailLostPassword(String to, String publicKey, String token) {

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
                        "                   <a href='"+ ADRESSE +"resetPassword/"+ publicKey + "?token=" + token + "' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 18px; color: white; text-decoration: none; background-color: orange;'>Réinitialiser mon mot de passe</a>" +
                        "               </td>" +
                        "           </tr>" +
                        "       </table>" +
                        "       <p>Ou copier et coller l'URL dans votre navigateur : </p>" +
                        "       <a href='"+ ADRESSE +"resetPassword/"+ publicKey + "?token=" + token + "'>"+ ADRESSE +"resetPassword/"+ publicKey +"</a>" +
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
            return new Response(EnumStatus.OK, null, "Un mail valable 15 minutes pour réinitialiser votre mot de passe vient de vous êtes envoyé");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.info("error from mail sender : " + e.getMessage());
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }

    public Response sendMailTest(String to, Bug bugTest) {


        // Paramètres du destinataire
        String subject = "Bugspointer - Votre rapport de la page de test";

        // Contenu HTML de l'email
        String htmlContent =
                "<html style='background-color:#F0F1F3; font-size:16px' >" +
                        "  <body >" +
                        "    <h1> Améliorez votre site web avec Bugspointer - Obtenez des détails approfondis sur vos rapports de bugs ! </h1>" +
                        "    <p>Cher utilisateur de Bugspointer, <br>" +
                        "    Nous avons bien reçu votre rapport de bug depuis la page de test de notre site bugspointer.com, et nous tenons à vous remercier pour votre intérêt</p><br><br>" +
                        "      <table width='65%' align='center' style='background-color: #FFFFFF;'>" +
                        "        <thead>" +
                        "        <tr>" +
                        "          <th>Voici un aperçu de ce que vous obtiendrez pour une utilisation gratuite :</th>" +
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
                        "            <table border='0' cellpadding='0' cellspacing='0' align='center'>" +
                        "              <tr>" +
                        "                <td align='center' style='padding: 10px;'>" +
                        "                  <a href='"+ ADRESSE +"features/' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 18px; color: white; text-decoration: none; background-color: orange;'>Je veux en voir plus</a>" +
                        "                </td>" +
                        "              </tr>" +
                        "            </table>" +
                        "          </td>" +
                        "        </tr>" +
                        "        <tr>" +
                        "        </tbody>" +
                        "        <thead>" +
                        "            <tr>" +
                        "              <th>Voici un aperçu de ce que vous obtiendrez en mettant à niveau votre abonnement : </th>" +
                        "          </tr>" +
                        "         </thead>" +
                        "          <tbody>" +
                        "            <tr>" +
                        "               <td>" +
                        "                   <h1>Nouveau Bug</h1>" +
                        "                   <table>" +
                        "                       <tr>" +
                                "                <th width='170px' text-align='right'>URL concernée : </th>" +
                                "                <td>"+ bugTest.getUrl() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th width='170px' text-align='right'>Date du rapport : </th>" +
                                "                <td>"+ bugTest.getDateCreation() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th width='170px' text-align='right'>Description du bug : </th>" +
                                "                <td>" + bugTest.getDescription() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th width='170px' text-align='right'>OS utilisateur : </th>" +
                                "                <td>"  + bugTest.getOs() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th width='170px' text-align='right'>Navigateur : </th>" +
                                "                <td>"  + bugTest.getBrowser() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th width='170px' text-align='right'>Taille de l'écran : </th>" +
                                "                <td>"  + bugTest.getScreenSize() +"</td>" +
                                "              </tr>" +
                                "              <tr>" +
                                "                <th width='170px' text-align='right'>Code HTML sélectionné : </th>" +
                                "                <td><pre><code>"  + bugService.codeBlockFormatter(bugTest.getCodeLocation()) +"</code></pre></td>" +
                                "              </tr>" +
                                "               <tr>" +
                                "               <th colspan=2>La balise pointé par vos soins est maintenant identifiable par la classe : bugspointer-pointed-balise</th>" +
                                "              </tr>" +
                                "            </table>" +
                                "            <p>Imaginez la valeur que vous pourriez obtenir en ayant accès à ces détails précis pour tous vos rapports de bugs. Notre offre payante vous permettra de prendre des décisions éclairées, d'améliorer rapidement votre site et de fournir une expérience utilisateur exceptionnelle. <br><br>" +
                                "            Vous pouvez également consulter votre Dashboard personnalisé pour avoir une vue d'ensemble complète de tous vos rapports de bugs et de leurs détails. C'est un outil puissant qui vous aidera à suivre les progrès de résolution des problèmes." +
                                "            <table align='center' border='0' cellpadding='0' cellspacing='0' >" +
                                "              <tr>" +
                                "                <td align='center' style='padding: 10px;'>" +
                                "                  <a href='"+ ADRESSE +"app/private/dashboard' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 18px; color: white; text-decoration: none; background-color: orange;'>Voir mon Dashboard</a>" +
                                "                </td>" +
                                "              </tr><br><br>" +
                                "              <tr>" +
                                "               <th>Merci d'avoir testé notre application, aidez-nous à nous améliorer encore plus en évaluant nos services grâce au bouton ci-dessous : </th>" +
                                "              </tr>" +
                                "            </table>" +
                                "          </td>" +
                        "               </tr>" +

                        "                  <tr>" +
                        "                   <td>" +
                        /*"                       <table border='0' cellpadding='0' cellspacing='0' >" +*/
                        "                       <tr>" +
                        "                       <td align='center' style='padding: 10px'>" +
                        "                           <a href='"+ ADRESSE +"sondage' style='display: inline-block; padding: 10px 20px; border-radius: 5px; font-size: 18px; color: white; text-decoration: none; background-color: #00E676;' box-shadow='0 0 5px lightgrey'>Donner mon avis</a>" +
                        "                       </td>" +
                        "                   </tr>" +
/*                        "            </table>" +*/
                        "        <tr>" +
                        "         <td> " +
                    "               <p>Si vous avez des questions ou avez besoin d'une assistance supplémentaire, n'hésitez pas à nous contacter. Notre équipe est là pour vous aider.\n" +
                    "               <br><br>" +
                    "               Cordialement,<br>" +
                    "               L'équipe Bugspointer</p>" +
                        "         </td>" +
                        "        </tr>" +
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

