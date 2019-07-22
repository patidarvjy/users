<!DOCTYPE html>
<html>
<body>
<p>Hallo ${user.name},</p>
<p>Um Ihr docu tools Passwort zurückzusetzen, klicken Sie bitte auf den Link oder kopieren Sie den Link in die
    Adressleiste Ihres Browsers.</p>
<a href="${links.forgotPassword}/${user.verificationToken}">${links.forgotPassword}/${user.verificationToken}</a>
<p>Diese Nachricht wurde automatisch generiert. Bei Fragen wenden Sie sich bitte direkt via E-Mail oder Chat an unsere
    Service Abteilung.</p>
<p>Mit freundlichen Grüßen,</p>
<p>Ihr docu tools Team.</p>
</body>
</html>