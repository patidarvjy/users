<!DOCTYPE html>
<html>
<body>
<p>Hello ${user.name},</p>
<p>This email is a confirmation to let you know that the email address that provides you access to docu tools has been
    changed on ${changeDate} from ${fromEmail} to ${toEmail} .</p>
<p>Please press the following link to verify your docu tools account! Verify your account here.</p>
<p>Or copy the following link into your browser’s address line: </p>
<a href="${links.changeEmail}/${verificationToken}">${links.changeEmail}/${verificationToken}</a>
<p>As a security precaution, this notification has been sent to all your email addresses known to docu tools.</p>
<p>Please do not reply to this email; it was automatically generated. If you have any questions, please contact the docu
    tools service via mail or the chat on our website.</p>
<p>Kind regards,</p>
<p>The docu tools Team</p>
</body>
</html>