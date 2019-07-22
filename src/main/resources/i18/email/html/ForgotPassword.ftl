<!DOCTYPE html>
<html>
<body>
<p>Hello ${user.name},</p>
<p>To reset your docu tools password, please click this link, or copy the URL and paste it into the address bar of your
    browser:</p>
<a href="${links.forgotPassword}/${user.verificationToken}">${links.forgotPassword}/${user.verificationToken}</a>
<p>If you have any troubles, don't hesitate to email us or write us via our customer chat on our website.</p>
<p>Kind regards,</p>
<p>The docu tools Team</p>
</body>
</html>