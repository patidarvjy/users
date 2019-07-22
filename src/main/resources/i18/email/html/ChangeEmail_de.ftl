<!DOCTYPE html>
<html>
<body>
<p>Hallo ${user.name},</p>
<p>Mit dieser E-Mail bestätigen wir die am ${changeDate} angeforderte Änderung Ihrer E-Mail Adresse von ${fromEmail} auf
    ${toEmail}. </p>
<p>Bitte klicken Sie auf den folgenden Link, oder kopieren Sie Ihn in die Adressleiste Ihres Browsers, um die Änderung
    des docu tools Account zu bestätigen.</p>
<a href="${links.changeEmail}/${verificationToken}">${links.changeEmail}/${verificationToken}</a>
<p>Zu Ihrer Sicherheit wurde diese E-Mail an alle Ihrer in docu tools bekannten E-Mail Adressen gesendet.</p>
<p>Diese Nachricht wurde automatisch generiert. Bei Fragen wenden Sie sich bitte direkt via E-Mail oder Chat an unsere
    Service Abteilung.</p>
<p>Mit freundlichen Grüßen,</p>
<p>Ihr docu tools Team.</p>
</body>
</html>