package com.bugspointer.service.implementation;

import be.woutschoovaerts.mollie.exception.MollieException;
import com.bugspointer.dto.CustomerDTO;
import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.Bug;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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

    @Value("${mail.from.noreply:noreply@bugspointer.com}")
    private String noReplyAddress;

    @Value("${mail.from.contact:contact@bugspointer.com}")
    private String contactAddress;

    @Value("${mail.from.name:BugsPointer}")
    private String fromName;

    private static final String ADRESSE = "https://bugspointer.com/";

    private final BugService bugService;

    private final CustomerService customerService;

    public MailService(BugService bugService, CustomerService customerService) {
        this.bugService = bugService;
        this.customerService = customerService;
    }

    private String buildEmail(String preheader, String eyebrow, String title, String bodyContent, String buttonLabel, String buttonUrl, String footerNote) {
        String safePreheader = escapeHtml(preheader);
        String safeEyebrow = escapeHtml(eyebrow);
        String safeTitle = escapeHtml(title);
        String button = "";
        String linkFallback = "";
        String note = "";

        if (buttonLabel != null && buttonUrl != null) {
            String safeButtonLabel = escapeHtml(buttonLabel);
            String safeButtonUrl = escapeHtml(buttonUrl);
            button =
                    "<tr><td align='left' style='padding:10px 0 8px 0;'>" +
                    "<table role='presentation' border='0' cellpadding='0' cellspacing='0'><tr>" +
                    "<td bgcolor='#00E676' style='border-radius:8px;'>" +
                    "<a href='" + safeButtonUrl + "' target='_blank' style='display:inline-block;padding:14px 20px;border-radius:8px;background-color:#00E676;color:#08110D;font-family:Arial,sans-serif;font-size:15px;font-weight:bold;line-height:20px;text-decoration:none;'>" + safeButtonLabel + "</a>" +
                    "</td>" +
                    "</tr></table>" +
                    "</td></tr>";
            linkFallback =
                    "<tr><td style='padding:16px 0 0 0;color:#6B7280;font-family:Arial,sans-serif;font-size:12px;line-height:18px;'>" +
                    "Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br>" +
                    "<a href='" + safeButtonUrl + "' target='_blank' style='color:#0F8F54;text-decoration:underline;word-break:break-all;'>" + safeButtonUrl + "</a>" +
                    "</td></tr>";
        }

        if (footerNote != null && !footerNote.trim().isEmpty()) {
            note =
                    "<tr><td style='padding:18px 0 0 0;color:#6B7280;font-family:Arial,sans-serif;font-size:12px;line-height:18px;'>" +
                    escapeHtml(footerNote) +
                    "</td></tr>";
        }

        return "<!doctype html>" +
                "<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<meta name='x-apple-disable-message-reformatting'>" +
                "<title>" + safeTitle + "</title></head>" +
                "<body style='margin:0;padding:0;background-color:#F3F7F4;'>" +
                "<div style='display:none;max-height:0;overflow:hidden;color:#F3F7F4;opacity:0;'>" + safePreheader + "</div>" +
                "<table role='presentation' width='100%' border='0' cellpadding='0' cellspacing='0' style='background-color:#F3F7F4;'>" +
                "<tr><td align='center' style='padding:28px 12px;'>" +
                "<table role='presentation' width='100%' border='0' cellpadding='0' cellspacing='0' style='width:100%;max-width:640px;background-color:#FFFFFF;border:1px solid #DDE7E0;border-radius:12px;'>" +
                "<tr><td style='padding:28px 28px 8px 28px;'>" +
                "<table role='presentation' width='100%' border='0' cellpadding='0' cellspacing='0'>" +
                "<tr><td style='color:#08110D;font-family:Arial,sans-serif;font-size:20px;font-weight:bold;line-height:24px;'>BugsPointer</td>" +
                "<td align='right' style='color:#0F8F54;font-family:Arial,sans-serif;font-size:12px;font-weight:bold;line-height:18px;text-transform:uppercase;'>" + safeEyebrow + "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "<tr><td style='padding:18px 28px 0 28px;'>" +
                "<h1 style='margin:0;color:#08110D;font-family:Arial,sans-serif;font-size:26px;font-weight:bold;line-height:32px;'>" + safeTitle + "</h1>" +
                "</td></tr>" +
                "<tr><td style='padding:18px 28px 28px 28px;color:#26352E;font-family:Arial,sans-serif;font-size:15px;line-height:24px;'>" +
                bodyContent +
                "<table role='presentation' width='100%' border='0' cellpadding='0' cellspacing='0'>" +
                button +
                linkFallback +
                note +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "<table role='presentation' width='100%' border='0' cellpadding='0' cellspacing='0' style='width:100%;max-width:640px;'>" +
                "<tr><td align='center' style='padding:16px 8px 0 8px;color:#6B7280;font-family:Arial,sans-serif;font-size:12px;line-height:18px;'>" +
                "Email transactionnel envoyé par BugsPointer. Contact : " + escapeHtml(contactAddress) +
                "</td></tr></table>" +
                "</td></tr></table>" +
                "</body></html>";
    }

    private String paragraph(String content) {
        return "<p style='margin:0 0 14px 0;'>" + escapeHtml(content) + "</p>";
    }

    private String rawParagraph(String content) {
        return "<p style='margin:0 0 14px 0;'>" + content + "</p>";
    }

    private String detailsTable(String[][] rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("<table role='presentation' width='100%' border='0' cellpadding='0' cellspacing='0' style='margin:10px 0 18px 0;border:1px solid #DDE7E0;border-radius:8px;'>");
        for (String[] row : rows) {
            builder.append("<tr>");
            builder.append("<td style='padding:10px 12px;border-bottom:1px solid #EEF3EF;color:#536158;font-family:Arial,sans-serif;font-size:13px;line-height:20px;width:38%;'>").append(escapeHtml(row[0])).append("</td>");
            builder.append("<td style='padding:10px 12px;border-bottom:1px solid #EEF3EF;color:#08110D;font-family:Arial,sans-serif;font-size:13px;line-height:20px;word-break:break-word;'>").append(escapeHtml(row[1])).append("</td>");
            builder.append("</tr>");
        }
        builder.append("</table>");
        return builder.toString();
    }

    private String codeBlock(String content) {
        return "<pre style='margin:0 0 16px 0;padding:12px;white-space:pre-wrap;word-break:break-word;background-color:#F6F8F7;border:1px solid #DDE7E0;border-radius:8px;color:#26352E;font-family:Consolas,Monaco,monospace;font-size:12px;line-height:18px;'>" + escapeHtml(content) + "</pre>";
    }

    private String emailUrl(String path) {
        return ADRESSE + path;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String htmlToText(String htmlContent) {
        return htmlContent
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</h1>", "\n")
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n\\s+", "\n")
                .trim();
    }

    private Response sendNoReplyMail(String to, String subject, String htmlContent, String successMessage, String logContext) {
        try {
            sendHtmlMail(noReplyAddress, to, subject, htmlContent);
            log.info("{} sent at : {}", logContext, to);
            return new Response(EnumStatus.OK, null, successMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error from mail sender for {} to {} : {}", logContext, to, e.getMessage(), e);
            return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
        }
    }

    private void sendHtmlMail(String from, String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.trust", host);

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };

        Session session = Session.getInstance(properties, auth);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from, fromName));
        message.setReplyTo(new Address[]{new InternetAddress(contactAddress, fromName)});
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, "UTF-8");

        MimeMultipart multipart = new MimeMultipart("alternative");
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(htmlToText(htmlContent), "UTF-8");
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        Transport.send(message);
    }

    /**
     * Envoie un mail pour confirmer un compte
     * @param to
     * @param publicKey
     * @return
     */
    public Response sendMailRegister(String to, String publicKey) {

        // Paramètres du destinataire
        String subject = "BugsPointer - Confirmer votre inscription";

        String confirmationUrl = emailUrl("confirmRegister/" + publicKey);
        String htmlContent = buildEmail(
                "Confirmez votre adresse e-mail pour activer votre espace BugsPointer.",
                "Activation",
                "Confirmez votre inscription",
                paragraph("Bonjour,") +
                        paragraph("Votre compte BugsPointer est presque prêt. Pour l'activer, confirmez simplement votre adresse e-mail.") +
                        paragraph("Ce contrôle protège votre espace et garantit que les prochains rapports de bugs arriveront bien au bon endroit."),
                "Confirmer mon inscription",
                confirmationUrl,
                "Si vous n'êtes pas à l'origine de cette inscription, vous pouvez ignorer ce message."
        );

        return sendNoReplyMail(
                to,
                subject,
                htmlContent,
                "Nous vous avons envoyé un e-mail de confirmation à l'adresse : " + to,
                "Email register"
        );
    }

    /**
     * Envoie un mail sur un compte FREE, + de 30 jours depuis le dernier bug
     * @param to
     * @param newBug
     * @return
     */
    public Response sendMailNewBugDetail(String to, Bug newBug) {

        // Paramètres du destinataire
        String subject = "BugsPointer - Nouveau bug";

        String htmlContent = buildEmail(
                "Un nouveau bug vient d'être signalé sur votre site.",
                "Nouveau rapport",
                "Nouveau bug signalé",
                paragraph("Un utilisateur vient de déclarer un nouveau bug sur votre site.") +
                        detailsTable(new String[][]{
                                {"URL concernée", newBug.getUrl()},
                                {"Description", newBug.getDescription()}
                        }) +
                        paragraph("Pour accéder aux détails complets et centraliser vos rapports dans le dashboard, vous pouvez découvrir le plan Target."),
                "Voir les fonctionnalités",
                emailUrl("features/"),
                null
        );

        return sendNoReplyMail(to, subject, htmlContent, "Mail gratuit avec détails envoyé", "Email new bug detail");
    }

    /**
     * Envoie un mail sur un compte FREE, moins de 30 jours depuis le dernier bug
     * @param to
     * @return
     */
    public Response sendMailNewBugNoDetail(String to) {


        // Paramètres du destinataire
        String subject = "BugsPointer - Nouveau bug";

        String htmlContent = buildEmail(
                "Un nouveau bug a été signalé, mais votre limite gratuite est atteinte.",
                "Nouveau rapport",
                "Nouveau bug signalé",
                paragraph("Un utilisateur vient de déclarer un nouveau bug sur votre site.") +
                        paragraph("Votre plan gratuit inclut un rapport détaillé tous les 30 jours. Pour voir toutes les informations de ce nouveau signalement, passez au plan Target."),
                "Voir les détails du plan",
                emailUrl("features/"),
                null
        );

        return sendNoReplyMail(to, subject, htmlContent, "Mail gratuit sans détails envoyé", "Email new bug no detail");
    }

    public Response sendMailLostPassword(String to, String publicKey, String token) {

        // Paramètres du destinataire
        String subject = "BugsPointer - Réinitialisation du mot de passe";

        String resetUrl = emailUrl("resetPassword/" + publicKey + "/" + token);
        String htmlContent = buildEmail(
                "Votre lien de réinitialisation BugsPointer est valable 15 minutes.",
                "Sécurité",
                "Réinitialisez votre mot de passe",
                paragraph("Bonjour,") +
                        paragraph("Vous avez demandé à réinitialiser le mot de passe de votre compte BugsPointer. Ce lien est valable 15 minutes.") +
                        paragraph("Choisissez un nouveau mot de passe en utilisant le bouton ci-dessous."),
                "Réinitialiser mon mot de passe",
                resetUrl,
                "Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer ce message."
        );

        return sendNoReplyMail(
                to,
                subject,
                htmlContent,
                "Un mail valable 15 minutes pour réinitialiser votre mot de passe vient de vous êtes envoyé",
                "Email reset password"
        );
    }

    public Response sendMailTest(String to, Bug bugTest) {


        // Paramètres du destinataire
        String subject = "BugsPointer - Votre rapport de la page de test";

        String htmlContent = buildEmail(
                "Votre rapport de test BugsPointer a bien été reçu.",
                "Page de test",
                "Votre rapport de test est prêt",
                paragraph("Bonjour,") +
                        paragraph("Nous avons bien reçu le rapport envoyé depuis la page de test. Voici un aperçu du niveau de détail disponible dans BugsPointer.") +
                        detailsTable(new String[][]{
                                {"URL concernée", bugTest.getUrl()},
                                {"Date du rapport", Utility.dateFormator(bugTest.getDateCreation(), "dd/MM/yyyy HH:mm:ss")},
                                {"Description", bugTest.getDescription()},
                                {"OS utilisateur", bugTest.getOs()},
                                {"Navigateur", bugTest.getBrowser()},
                                {"Taille de l'écran", bugTest.getScreenSize()}
                        }) +
                        paragraph("La balise pointée est identifiable avec la classe bugspointer-pointed-balise.") +
                        rawParagraph("<strong style='color:#08110D;'>Code HTML sélectionné</strong>") +
                        codeBlock(bugService.codeBlockFormatter(bugTest.getCodeLocation())) +
                        paragraph("Le dashboard vous permet de retrouver ces informations pour chaque signalement et de suivre leur résolution au fil du temps."),
                "Voir mon dashboard",
                emailUrl("authentication"),
                "Merci d'avoir testé BugsPointer. Vous pouvez aussi nous envoyer votre avis depuis " + emailUrl("pollUser") + "."
        );

        return sendNoReplyMail(to, subject, htmlContent, "Mail avec le bug sur page de test envoyé", "Email test report");
    }

    public Response sendMailNewMandate(CustomerDTO customer) throws MollieException {
        log.warn("customer in mail new mandate (besoin de : {}", customer);

        HashMap<String, String> contentData = customerService.getDataToMandateForCustomer(customer);
        log.warn("contentDataResponse : {}", contentData);
        if(contentData.get("status").equals("OK")) {

            // Paramètres du destinataire
            String subject = "BugsPointer - Mandat de prélèvement SEPA";

            String htmlContent = buildEmail(
                    "Confirmation de votre mandat de prélèvement SEPA BugsPointer.",
                    "Paiement",
                    "Mandat SEPA confirmé",
                    paragraph("Bonjour,") +
                            paragraph("Nous vous confirmons le mandat de prélèvement que vous venez de signer sur BugsPointer.") +
                            detailsTable(new String[][]{
                                    {"Référence", contentData.get("reference")},
                                    {"IBAN", contentData.get("iban")},
                                    {"BIC", contentData.get("bic")},
                                    {"Date de signature", contentData.get("dateSignature")},
                                    {"Date du prochain paiement", contentData.get("dateNextPayment")},
                                    {"Mandat valide jusqu'au", contentData.get("dateExpiration")}
                            }) +
                            paragraph("Vous pouvez révoquer ce mandat à tout moment depuis la partie Account de votre dashboard."),
                    "Ouvrir mon compte",
                    emailUrl("authentication"),
                    null
            );

            return sendNoReplyMail(
                    customer.getMail(),
                    subject,
                    htmlContent,
                    "Détail du mandat envoyer à " + customer.getMail(),
                    "Email details mandate"
            );
        }
        log.error("error from mail sender mandate details to {}", customer.getCompanyName());
        return new Response(EnumStatus.ERROR, null, "Oups, il y a eu une erreur !");
    }


    public Response sendMailChangePlan(EnumPlan plan, String mail) {

        // Paramètres du destinataire
        String subject = "BugsPointer - Confirmation de l'abonnement";

        String htmlContent = buildEmail(
                "Votre abonnement BugsPointer a bien été pris en compte.",
                "Abonnement",
                "Votre plan est activé",
                paragraph("Bonjour,") +
                        paragraph("Votre souscription au plan " + plan + " a bien été prise en compte.") +
                        paragraph("Vous pouvez maintenant retrouver votre espace et continuer à suivre vos signalements depuis le dashboard."),
                "Ouvrir le dashboard",
                emailUrl("authentication"),
                null
        );

        return sendNoReplyMail(mail, subject, htmlContent, "Votre souscription a bien été prise en compte, merci", "Email new subscribe");
    }


    public Response sendMailNewBugForNotification(String mail) {

        // Paramètres du destinataire
        String subject = "BugsPointer - Nouveau bug signalé";

        String htmlContent = buildEmail(
                "Un utilisateur vient de signaler un bug sur votre site.",
                "Notification",
                "Vous avez un nouveau bug",
                paragraph("Un utilisateur vient de signaler un bug sur votre site.") +
                        paragraph("Connectez-vous à votre dashboard pour consulter le rapport et organiser son suivi."),
                "Voir le bug",
                emailUrl("authentication"),
                "Vous pouvez désactiver ces notifications depuis Dashboard > Notifications."
        );

        return sendNoReplyMail(mail, subject, htmlContent, "", "Email new bug report");
    }

}
