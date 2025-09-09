package org.accountinglib.mail;

/*-
 * #%L
 * NoCommons
 * %%
 * Copyright (C) 2014 - 2023 BEKK open source
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import static org.accountinglib.common.HelperFunctions.distinctByKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.accountinglib.common.StringNumberValidator;
import org.accountinglib.mail.model.Kommunenavn;
import org.accountinglib.mail.model.Kommunenummer;
import org.accountinglib.mail.model.PostInfo;
import org.accountinglib.mail.model.Postnummer;
import org.accountinglib.mail.model.PostnummerKategori;
import org.accountinglib.mail.model.Poststed;

/**
 * Validates:
 * - Postnummer
 * - Poststed
 * - Kommunenummer
 * - Kommunenavn
 *
 * Lookup:
 * - Postnummer
 * - Poststed
 * - Kommunenummer
 * - Kommunenavn
 * - Postnummer kategori
 */
public class MailValidator extends StringNumberValidator {
    private static final int POSTNUMMER_LENGTH = 4;
    private static final int KOMMUNENUMMER_LENGTH = 4;

    private static Map<Postnummer, PostInfo> postInfo = new HashMap<>();

    public static Postnummer getPostnummer(String postnummer) {
        validatePostnummerSyntax(postnummer);
        return new Postnummer(postnummer);
    }

    public static Kommunenummer getKommunenummer(String kommunenummer) {
        validateKommunenummerSyntax(kommunenummer);
        return new Kommunenummer(kommunenummer);
    }

    public static PostnummerKategori getPostnummerKategori(String postnummerKategori) {
        return PostnummerKategori.fromString(postnummerKategori);
    }

    // Setup

    public static void setPostInfo(Map<Postnummer, PostInfo> postInfo) {
        MailValidator.postInfo = postInfo;
    }

    // Validation

    public static boolean isValidPostnummer(String postnummer) {
        try {
            MailValidator.getPostnummer(postnummer);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void validatePostnummerSyntax(String postnummer) {
        validateLengthAndAllDigits(postnummer, POSTNUMMER_LENGTH);
    }

    public static boolean isValidKommunenummer(String kommunenummer) {
        try {
            MailValidator.getKommunenummer(kommunenummer);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void validateKommunenummerSyntax(String kommunenummer) {
        validateLengthAndAllDigits(kommunenummer, KOMMUNENUMMER_LENGTH);
    }

    // Statistics

    public static int getAntallPoststed() {
        return (int) postInfo.entrySet().stream()
            .map(s -> s.getValue())
            .filter(distinctByKey(PostInfo::getPoststed))
            .count();
    }

    public static int getAntallPostnummer() {
        return postInfo.size();
    }

    public static int getAntallKommunenummer() {
        return (int) postInfo.entrySet().stream()
            .map(s -> s.getValue())
            .filter(distinctByKey(PostInfo::getKommunenummer))
            .count();
    }

    // Lookup - Postnummer

    public static PostInfo getPostInfoForPostnummer(String postnummer) {
        PostInfo result = null;
        Postnummer pn = getPostnummer(postnummer);

        if (postInfo.containsKey(pn)) {
            result = postInfo.get(pn);
        }

        return result;
    }

    public static Poststed getPoststedForPostnummer(String postnummer) {
        return getPostInfoForPostnummer(postnummer).getPoststed();
    }

    public static Kommunenavn getKommunenavnForPostnummer(String postnummer) {
        return getPostInfoForPostnummer(postnummer).getKommunenavn();
    }

    public static Kommunenummer getKommunenummerForPostnummer(String postnummer) {
        return getPostInfoForPostnummer(postnummer).getKommunenummer();
    }

    public static PostnummerKategori getPostnummerKategoriForPostnummer(String postnummer) {
        return getPostInfoForPostnummer(postnummer).getPostnummerKategori();
    }

    public static List<Postnummer> getPostnummerForPoststed(String poststed) {
        Poststed p = new Poststed(poststed);

        List<Postnummer> postnummerList =
            postInfo.entrySet().stream()
                .filter(a -> a.getValue().getPoststed().equals(p))
                .map(x -> x.getValue().getPostnummer())
                .collect(Collectors.toList());

        return (postnummerList == null ? new ArrayList<>() : postnummerList);
    }

    // Lookup - Kommunenummer / Kommunenavn

    public static Optional<PostInfo> getPostInfoForKommunenummer(String kommunenummer) {
        return postInfo.entrySet().stream()
            .map(s -> s.getValue())
            .filter(s -> s.getKommunenummer().toString().equals(kommunenummer))
            .findFirst();
    }

    public static Kommunenavn getKommunenavnForKommunenummer(String kommunenummer) {
        Optional<PostInfo> result = getPostInfoForKommunenummer(kommunenummer);

        return result.isPresent() ? result.get().getKommunenavn() : null;
    }

    public static Optional<PostInfo> getPostInfoForKommunenavn(String kommunenavn) {
        return postInfo.entrySet().stream()
            .map(s -> s.getValue())
            .filter(s -> s.getKommunenavn().toString().equalsIgnoreCase(kommunenavn))
            .findFirst();
    }

    public static Kommunenummer getKommunenummerForKommunenavn(String kommunenavn) {
        Optional<PostInfo> result = getPostInfoForKommunenavn(kommunenavn);

        return result.isPresent() ? result.get().getKommunenummer() : null;
    }
}
