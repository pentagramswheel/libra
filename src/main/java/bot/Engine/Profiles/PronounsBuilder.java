package bot.Engine.Profiles;

import bot.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author  Slate
 * Date:    July 2022
 * Project: Libra
 * Module:  PronounsBuilder.java
 * Purpose: Handler for providing grammatical pronouns
 *          from arbitrary text.
 */
public class PronounsBuilder {

    /** Pattern that matches "he" in the text as a whole word. */
    private static final Pattern HE_REGEX = Pattern.compile(
            "(^|\\W)(he)(\\W|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /** Pattern that matches "she" in the text as a whole word. */
    private static final Pattern SHE_REGEX = Pattern.compile(
            "(^|\\W)(she)(\\W|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /** Pattern that matches "they" in the text as a whole word. */
    private static final Pattern THEY_REGEX = Pattern.compile(
            "(^|\\W)(they)(\\W|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /** Pattern that matches "it" in the text as a whole word. */
    private static final Pattern IT_REGEX = Pattern.compile(
            "(^|\\W)(it)(\\W|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /** Pattern that matches "all" or "any" [pronouns] in a text. */
    private static final Pattern ALL_REGEX = Pattern.compile(
            "^all$|(^|\\W)((pronouns? ?([ :]) ?(all|any))|((all|any) pronouns?))(\\W|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /** Pattern that matches "ask" [for pronouns] in a text. */
    private static final Pattern ASK_REGEX = Pattern.compile(
            "^ask$|(^|\\W)((pronouns? ?([ :]) ?(ask))|(ask (for )?pronouns?))(\\W|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    /** List of pronouns matched as subjects. */
    private List<String> subjects;

    /** List of pronouns matched as objects. */
    private List<String> objects;

    /** List of pronouns matched as possessive pronouns. */
    private List<String> possessivePronouns;

    /** List of pronouns matched as possessive adjectives. */
    private List<String> possessiveAdjectives;

    /**
     * Construct the PronounsBuilder by the raw pronoun string.
     * @param pronouns the pronouns string.
     */
    public PronounsBuilder(String pronouns) {
        if (pronouns == null || pronouns.trim().isEmpty()) {
            return;
        }

        subjects = new ArrayList<>();
        objects = new ArrayList<>();
        possessivePronouns = new ArrayList<>();
        possessiveAdjectives = new ArrayList<>();

        // use the three common person pronouns (it and neo pronoun users, we still love you)
        boolean allMatched = ALL_REGEX.matcher(pronouns).find();

        if (THEY_REGEX.matcher(pronouns).find()
                || ASK_REGEX.matcher(pronouns).find() || allMatched) {
            subjects.add("they");
            objects.add("them");
            possessivePronouns.add("theirs");
            possessiveAdjectives.add("their");
        }

        if (HE_REGEX.matcher(pronouns).find() || allMatched) {
            subjects.add("he");
            objects.add("him");
            possessivePronouns.add("his");
            possessiveAdjectives.add("his");
        }

        if (SHE_REGEX.matcher(pronouns).find() || allMatched) {
            subjects.add("she");
            objects.add("her");
            possessivePronouns.add("hers");
            possessiveAdjectives.add("her");
        }

        if (IT_REGEX.matcher(pronouns).find()) {
            subjects.add("it");
            objects.add("it");
            possessivePronouns.add("its own");
            possessiveAdjectives.add("its");
        }
    }

    /**
     * Construct the PronounsBuilder by the Player's profile.
     * @param playerProfile The Player's profile.
     */
    public PronounsBuilder(PlayerInfo playerProfile) {
        this(playerProfile.getPronouns());
    }

    /**
     * Get the full list of pronouns matched as subjects.
     * e.g. he, she, they, it, etc.
     */
    private List<String> getSubjectsList() {
        return subjects;
    }

    /**
     * Get the full list of pronouns matched as objects.
     * e.g. him, her, them, it, etc.
     */
    private List<String> getObjectsList() {
        return objects;
    }

    /**
     * Get the full list of pronouns matched as possessive pronouns.
     * e.g. his, hers, theirs, its own, etc.
     */
    private List<String> getPossessivePronounsList() {
        return possessivePronouns;
    }

    /**
     * Get the full list of pronouns matched as possessive adjectives.
     * e.g. his, her, their, its, etc.
     */
    private List<String> getPossessiveAdjectivesList() {
        return possessiveAdjectives;
    }

    /**
     * Retrieves a value from one of the builder's lists.
     * @param lst the list to access.
     * @param defaultVal a value to default to.
     */
    private String fromList(List<String> lst, String defaultVal) {
        int size = lst.size();

        switch (size) {
            case 0:
                return defaultVal;
            case 1:
                return lst.get(0);
            default:
                return lst.get(
                        Events.RANDOM_GENERATOR.nextInt(size));
        }
    }

    /**
     * Capitalizes the first letter of a string.
     * @param str the string to capitalize.
     */
    private String toTitleCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Get a valid subject pronoun (he, she, they, it, etc).
     *
     * <p>
     * Note on English: subjects *usually go at the start* and before the verb.
     * Subject pronouns replace the subject in the sentence.
     * <p>
     * e.g. When he was young and she was younger:
     *      "he" and "she" are subject pronouns.
     */
    public String getSubject() {
        return fromList(subjects, "they");
    }

    /** Get a valid, capitalized subject pronoun (He, She, They, It, etc). */
    public String getSubjectTitleCase() {
        return toTitleCase(getSubject());
    }

    /**
     * Get a valid object pronoun (him, her, them, it, etc).
     *
     * <p>
     * Note on English: objects *usually go at the end* and after the verb. They are often in form "to [noun]".
     * Object pronouns replace the object in the sentence.
     * <p>
     * e.g. The jellyfish shocked him; the captain shouted to her:
     *      "Him" and "her" are object pronouns.
     *      (the jellyfish and the captain are subjects)
     */
    public String getObject() {
        return fromList(objects, "them");
    }

    /** Get a valid, capitalized object pronoun (Him, Her, Them, It, etc). */
    public String getObjectTitleCase() {
        return toTitleCase(getObject());
    }

    /**
     * Get a valid possessive pronoun (his, hers, theirs, its own, etc).
     *
     * <p>
     * Note on English: possessive pronouns *usually go at the end* and are *not* before a noun.
     * They indicate the ownership of the preceding noun.
     * <p>
     * e.g. The game is ours to take:
     *      "ours" is a possessive pronoun ("the game" is the owned noun).
     * e.g. The captain shouted to the platoon of hers:
     *      "hers" is a possessive pronoun ("platoon" is the owned noun).
     */
    public String getPossessivePronoun() {
        return fromList(possessivePronouns, "theirs");
    }

    /** Get a valid, capitalized possessive pronoun (His, Hers, Theirs, Its own, etc). */
    public String getPossessivePronounTitleCase() {
        return toTitleCase(getPossessivePronoun());
    }

    /**
     * Get a valid possessive adjective (his, her, their, its, etc).
     *
     * <p>
     * Note on English: possessive adjectives *usually go at the start* and *are* before a noun.
     * They indicate the ownership of the noun following.
     * <p>
     * e.g. Our game, their loss; "our" and "their" are possessive adjectives ("game" and "loss" are nouns following).
     * <p>
     * e.g. The captain shouted to her platoon:
     *      "her" is a possessive adjective.
     */
    public String getPossessiveAdjective() {
        return fromList(possessiveAdjectives, "their");
    }

    /** Get a valid, capitalized possessive adjective (His, Her, Their, Its, etc). */
    public String getPossessiveAdjectiveTitleCase() {
        return toTitleCase(getPossessiveAdjective());
    }
}
