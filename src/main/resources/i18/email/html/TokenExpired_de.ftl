<!DOCTYPE html>
<html>
<body>
<p>Hallo ${user.name},</p>
<p>Diese Email ist eine Information, dass  Ihr Email Bestätigungs Token abgelaufen ist.</p>
<p>Bitte nutzen Sie diesen Link: <a href="${links.register}/${user.verificationToken}">${links.register}/${user.verificationToken}</a> um die Email erneut zu verifizieren.</p>
<p>Mit freundlichen Gr&uuml;&szlig;en,</p>
<p>Ihr docu tools Team.</p>
</body>
</html>