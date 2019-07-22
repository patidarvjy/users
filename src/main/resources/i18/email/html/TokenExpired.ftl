<!DOCTYPE html>
<html>
<body>
<p>Hello ${user.name},</p>
<p>This email is a confirmation to let you know that your registration confirmation token has expired.</p>
<p>Please go to <a href="${links.invite}/${user.verificationToken}">${links.invite}/${user.verificationToken}</a> to verify this email address.</p>
<p>Kind regards,</p>
<p>The docu tools Team</p>
</body>
</html>