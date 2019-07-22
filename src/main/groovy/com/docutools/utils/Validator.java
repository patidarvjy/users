package com.docutools.utils;

import java.util.regex.Pattern;

public class Validator {


    public static boolean isValidEmail(String email) {
        return email.contains("@");

    }

    public static boolean isSustainUser(String email) {
        return email.endsWith("@docu-tools.com") || email.endsWith("@docu-tools.works") || email.endsWith("@partsch.ninja");
    }

    private static final String EMAIL_PATTERN =
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


    public static boolean verifyEmailWithValidPattern(String email) {

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        return pattern.matcher(email).matches();

    }


}
