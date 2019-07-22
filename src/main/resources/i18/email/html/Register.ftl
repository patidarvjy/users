<!DOCTYPE html>
<html>
<body>
<p>Hello ${user.name},</p>
<p>Thank you for signing up! With your new docu tools account you will be able to manage your daily work flow much
    better.</p>
<p>Please press the following link to verify your docu tools account! Verify your account here.</p>
<p>Or copy the following link into your browser's address line: </p>
<a href="${links.register}/${user.verificationToken}">${links.register}/${user.verificationToken}</a>
<p>On our <a href="${links.help}">help</a> page you will find tutorial videos to get your first project up and running in
    minutes. If you have any
    further questions please feel free to contact via our customer chat on <a href="${links.url}">${links.url}</a>.
</p>
<p>We love to talk and learn about your workflows.</p>
<p>Kind regards,</p>
<p>The docu tools Team</p>
</body>
</html>