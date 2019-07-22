package com.docutools.test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Some static functions to generate test data.
 *
 * @since 1.0
 * @author amp
 */
public class AlternativeFacts {

    public static UUID uuid() {
        return UUID.randomUUID();
    }

    public static String uuidStr() {
        return uuid().toString();
    }

    public static String firstName() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }

    public static List<String> firstNames = Arrays.asList(
            "Alex", "Munish", "Raj", "Mohit", "Max", "Gerhard", "Fabio"
    );

    public static String lastName() {
        return lastNames.get(random.nextInt(lastNames.size()));
    }

    public static List<String> lastNames = Arrays.asList(
            "Johnson", "Allmayer-Beck", "Müller", "Obama", "Trump", "Kásmimir"
    );

    public static String email() {
        return String.format(emails.get(random.nextInt(emails.size())), UUID.randomUUID());
    }

    private static final AtomicInteger emailCounter = new AtomicInteger(1);

    public static List<String> emails = Arrays.asList(
            "freddy%s.mercury@gmail.com",
            "barack%s.obama@yahoo.de",
            "%sevasales@mcdonalds.at",
            "invalid%s@docu-tools.com"
    );

    public static String phone() {
        return phones.get(random.nextInt(phones.size()));
    }

    public static List<String> phones = Arrays.asList(
            "+436693839122",
            "+40113401233",
            "08003334646",
            "0800666999"
    );

    public static String jobTitle() {
        return jobTitles.get(random.nextInt(jobTitles.size()));
    }

    public static List<String> jobTitles = Arrays.asList(
            "Architect", "Engineer", "Project Leader", "Civil Engineer"
    );

    public static String randomString() {
        return randomString(100);
    }

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i < length; i++) {
            sb.append(allSymbols.charAt(random.nextInt(allSymbols.length())));
        }
        return sb.toString();
    }

    public static final String allSymbols =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "1234567890" +
                    "°^!\"§$%&/()=?`\\¡“¶¢[]|{}≠¿'+*±#'‘-_.:…,;∞<>≤„" +
                    " \t \n \r\n";

    public static String language() {
        return languages.get(random.nextInt(languages.size()));
    }

    public static List<String> languages = Arrays.asList("de", "en", "ru");

    public static String cc() {
        return countries.get(random.nextInt(countries.size()));
    }

    public static List<String> countries = Arrays.asList("de", "at", "ch", "ru", "es", "uk", "it");

    public static String timeZone() {
        Set<String> timeZones = ZoneId.getAvailableZoneIds();
        int size = timeZones.size();
        return timeZones.toArray(new String[size])[random.nextInt(size)];
    }

    public static String organisationName() {
        return organisationNames.get(random.nextInt(organisationNames.size()));
    }

    public static final List<String> organisationNames = Arrays.asList(
            "Strabag AG", "Friends GmbH", "Forever Limited", "A1 Telekom Austria AG", "EU Frankenstein", "Radatz OG"
    );

    public static String randomPassword() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 3; i++) {
            sb.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
            sb.append(Character.toLowerCase(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length()))));
            sb.append(numbers.charAt(random.nextInt(numbers.length())));
            sb.append(specialSymbols.charAt(random.nextInt(specialSymbols.length())));
        }
        return sb.toString();
    }

    public static final String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String numbers = "1234567890";
    public static final String specialSymbols = "+-.:;,";

    public static byte[] randomByteArray(int size) {
        byte[] array = new byte[size];
        random.nextBytes(array);
        return array;
    }

    public static LocalDateTime notToday() {
        return LocalDateTime.now().plusDays(random.nextInt(31));
    }

    public static <T> Collection<T> takeSample(List<T> collection) {
        int sampleSize = random.nextInt(collection.size()) + 1;
        List<T> samples = new ArrayList<>();
        List<T> copy = new ArrayList<>(collection);
        for(int i = 0; i < sampleSize; i++) {
            int index = random.nextInt(copy.size());
            T sample = copy.get(index);
            samples.add(sample);
            copy.remove(index);
        }
        return samples;
    }

    public static Random random = new Random();

}
