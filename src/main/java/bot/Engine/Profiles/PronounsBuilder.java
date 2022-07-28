package bot.Engine.Profiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author  Slate
 * Date:    July 2022
 * Project: Libra
 * Module:  PronounsBuilder.java
 * Purpose: Handler code for providing grammatical pronouns from arbitrary text.
 */
public class PronounsBuilder {
  /** RNG */
  private static final Random rand = new Random();
  /** Pattern that matches "he" in the text as a whole word */
  private static final Pattern heRegex = Pattern.compile("(^|\\W)(he)(\\W|$)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  /** Pattern that matches "she" in the text as a whole word */
  private static final Pattern sheRegex = Pattern.compile("(^|\\W)(she)(\\W|$)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  /** Pattern that matches "they" in the text as a whole word */
  private static final Pattern theyRegex = Pattern.compile("(^|\\W)(they)(\\W|$)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  /** Pattern that matches "it" in the text as a whole word */
  private static final Pattern itRegex = Pattern.compile("(^|\\W)(it)(\\W|$)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  /** Pattern that matches ""all" or "any" (pronouns)" in the text */
  private static final Pattern allRegex = Pattern.compile("^all$|(^|\\W)((pronouns? ?([ :]) ?(all|any))|((all|any) pronouns?))(\\W|$)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  /** Pattern that matches ""ask" (for pronouns)" in the text */
  private static final Pattern askRegex = Pattern.compile("^ask$|(^|\\W)((pronouns? ?([ :]) ?(ask))|(ask (for )?pronouns?))(\\W|$)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  /** List of pronouns matched as subjects (he, she, they, it, etc) */
  private final List<String> subjects = new ArrayList<>();

  /** List of pronouns matched as objects (him, her, them, it, etc) */
  private final List<String> objects = new ArrayList<>();

  /** List of pronouns matched as possessive pronouns (his, hers, theirs, its, etc) */
  private final List<String> possessivePronouns = new ArrayList<>();

  /** List of pronouns matched as possessive adjectives (his, her, their, its, etc) */
  private final List<String> possessiveAdjectives = new ArrayList<>();

  /**
   * Construct the PronounsBuilder by the Player's profile.
   * @param playerProfile The Player's profile.
   */
  public PronounsBuilder(PlayerInfo playerProfile) {
    this(playerProfile.getPronouns());
  }

  /**
   * Construct the PronounsBuilder by the raw pronoun string.
   * @param pronouns The pronouns string.
   */
  public PronounsBuilder(String pronouns) {
    // Early out if the pronouns are not set.
    if (pronouns == null || pronouns.trim().isEmpty()) {
      return;
    }

    // Use the three common person pronouns (it and neo pronoun users, we still love you)
    boolean allMatched = allRegex.matcher(pronouns).find();

    // Use they for unknown/ask
    if (theyRegex.matcher(pronouns).find() || askRegex.matcher(pronouns).find() || allMatched) {
      subjects.add("they");
      objects.add("them");
      possessivePronouns.add("theirs");
      possessiveAdjectives.add("their");
      return;
    }

    if (heRegex.matcher(pronouns).find() || allMatched) {
      subjects.add("he");
      objects.add("him");
      possessivePronouns.add("his");
      possessiveAdjectives.add("his");
    }

    if (sheRegex.matcher(pronouns).find() || allMatched) {
      subjects.add("she");
      objects.add("her");
      possessivePronouns.add("hers");
      possessiveAdjectives.add("her");
    }
    
    if (itRegex.matcher(pronouns).find()) {
      subjects.add("it");
      objects.add("it");
      possessivePronouns.add("its");
      possessiveAdjectives.add("its");
    }
  }

  /** Get the full list of pronouns matched as subjects (he, she, they, it, etc) */
  public List<String> getSubjectsList() {
    return subjects;
  }
  
  /** Get the full list of pronouns matched as objects (him, her, them, it, etc) */
  public List<String> getObjectsList() {
    return objects;
  }

  /** Get the full list of pronouns matched as possessive pronouns (his, hers, theirs, its, etc) */
  public List<String> getPossessivePronounsList() {
    return possessivePronouns;
  }

  /** Get the full list of pronouns matched as possessive adjectives (his, her, their, its, etc) */
  public List<String> getPossessiveAdjectivesList() {
    return possessiveAdjectives;
  }

  /***
   * Get a random subject pronoun valid for this instance [he, she, they, it, etc].
   * If there are none valid, returns "they".
   *
   * <p>
   * Note on English: subjects *usually go at the start* and before the verb.
   * Subject pronouns replace the subject in the sentence.
   * <p>
   * e.g. When he was young and she was younger; "he" and "she" are subject pronouns.
   */
  public String getSubject() {
    switch (subjects.size()) {
      case 0:
        return "they";
      case 1:
        return subjects.get(0);
      default:
        return subjects.get(rand.nextInt(subjects.size()));
    }
  }
  
  /***
   * Get a random subject pronoun valid for this instance with the starting letter capitalized.
   * If there are none valid, returns "They".
   */
  public String getSubjectTitleCase() {
    return toTitleCase(getSubject());
  }

  /**
   * Get a random object pronoun valid for this instance [him, her, them, it, etc].
   * If there are none valid, returns "them".
   * 
   * <p>
   * Note on English: objects *usually go at the end* and after the verb. They are often in form "to [noun]".
   * Object pronouns replace the object in the sentence.
   * <p>
   * e.g. The jellyfish shocked him; the captain shouted to her. "Him" and "her" are object pronouns (the jellyfish and the captain are subjects).
   */
  public String getObject() {
    switch (objects.size()) {
      case 0:
        return "them";
      case 1:
        return objects.get(0);
      default:
        return objects.get(rand.nextInt(objects.size()));
    }
  }

  /***
   * Get a random object pronoun valid for this instance with the starting letter capitalized.
   * If there are none valid, returns "Them".
   */
  public String getObjectTitleCase() {
    return toTitleCase(getObject());
  }

  /**
   * Get a random possessive pronoun valid for this instance [his, hers, theirs, its, etc].
   * If there are none valid, returns "theirs".
   * 
   * <p>
   * Note on English: possessive pronouns *usually go at the end* and are *not* before a noun. 
   * They indicate the ownership of the preceding noun.
   * <p>
   * e.g. The game is ours to take: "ours" is a possessive pronoun ("the game" is the owned noun).
   * e.g. The captain shouted to the platoon of hers: "hers" is a possessive pronoun ("platoon" is the owned noun).
   */
  public String getPossessivePronoun() {
    switch (possessivePronouns.size()) {
      case 0:
        return "theirs";
      case 1:
        return possessivePronouns.get(0);
      default:
        return possessivePronouns.get(rand.nextInt(possessivePronouns.size()));
    }
  }

  /***
   * Get a random possessive pronoun valid for this instance with the starting letter capitalized.
   * If there are none valid, returns "Theirs".
   */
  public String getPossessivePronounTitleCase() {
    return toTitleCase(getPossessivePronoun());
  }

  /**
   * Get a random possessive adjective valid for this instance [his, her, their, its, etc].
   * If there are none valid, returns "their".
   * 
   * <p>
   * Note on English: possessive adjectives *usually go at the start* and *are* before a noun. 
   * They indicate the ownership of the noun following.
   * <p>
   * e.g. Our game, their loss; "our" and "their" are possessive adjectives ("game" and "loss" are nouns following).
   * <p>
   * e.g. The captain shouted to her platoon: "her" is a possessive adjective.
   */
  public String getPossessiveAdjective() {
    switch (possessiveAdjectives.size()) {
      case 0:
        return "their";
      case 1:
        return possessiveAdjectives.get(0);
      default:
        return possessiveAdjectives.get(rand.nextInt(possessiveAdjectives.size()));
    }
  }

  /***
   * Get a random possessive adjective valid for this instance with the starting letter capitalized.
   * If there are none valid, returns "Their".
   */
  public String getPossessiveAdjectiveTitleCase() {
    return toTitleCase(getPossessiveAdjective());
  }

  /***
   * Private helper function for capitalizing the first letter of a string.
   */
  private static String toTitleCase(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
