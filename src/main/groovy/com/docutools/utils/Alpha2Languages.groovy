package com.docutools.utils

import com.xlson.groovycsv.CsvParser

/**
 * Utility class for validating strings if they contain
 * <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">ISO 639-1 two-letter language codes</a>.
 */
@Singleton
class Alpha2Languages {

    private static final CSV_RESOURCE_PATH = '/alpha2-languages.csv'

    private Set<String> alpha2Codes = loadResource()

    private Set<String> loadResource() {
        def reader = new InputStreamReader(getClass().getResourceAsStream(CSV_RESOURCE_PATH))
        def set = CsvParser.parseCsv(reader)
            .collect {it.alpha2}
            .toSet()
        reader.close()
        set
    }

    /**
     * Checks whether the given string is a valid ISO 639-1 two-letter language code.
     *
     * @param test the string that shall be tested (ignoring the case).
     * @return {@code true} when the string is a valid language code.
     */
    def boolean isValid(String test) {
        alpha2Codes.contains(test?.toLowerCase())
    }

}
