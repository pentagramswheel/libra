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

  /** List of subjects matched (he, she, they etc) */
  private final List<String> subjects = new ArrayList<>();

  /** List of objects matched (him, her, them etc) */
  private final List<String> objects = new ArrayList<>();

  /** List of possessives matched (his, hers, their etc) */
  private final List<String> possessives = new ArrayList<>();

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

    // Use the three common pronouns (it and neo pronoun users, we still love you)
    boolean allMatched = allRegex.matcher(pronouns).find();

    // Use they for unknown/ask
    if (theyRegex.matcher(pronouns).find() || askRegex.matcher(pronouns).find() || allMatched) {
      subjects.add("they");
      objects.add("them");
      possessives.add("their");
      return;
    }

    if (heRegex.matcher(pronouns).find() || allMatched) {
      subjects.add("he");
      objects.add("him");
      possessives.add("his");
    }

    if (sheRegex.matcher(pronouns).find() || allMatched) {
      subjects.add("she");
      objects.add("her");
      possessives.add("hers");
    }
    
    if (itRegex.matcher(pronouns).find()) {
      subjects.add("it");
      objects.add("it");
      possessives.add("its");
    }
  }

  /** Get the full list of subjects matched (he, she, they etc) */
  public List<String> getSubjectsList() {
    return subjects;
  }
  
  /** Get the full list of objects matched (him, her, them etc) */
  public List<String> getObjectsList() {
    return objects;
  }

  /** Get the full list of possessives matched (his, hers, their etc) */
  public List<String> getPossessivesList() {
    return possessives;
  }

  /***
   * Get a random subject from the list of valid subjects.
   * If there are no valid subjects, returns "they".
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
   * Get a random subject from the list of valid subjects with the starting letter capitalized.
   * If there are no valid subjects, returns "They".
   */
  public String getSubjectTitleCase() {
    return toTitleCase(getSubject());
  }

  /**
   * Get a random object from the list of valid objects.
   * If there are no valid objects, returns "them".
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
   * Get a random object from the list of valid subjects with the starting letter capitalized.
   * If there are no valid subjects, returns "Them".
   */
  public String getObjectTitleCase() {
    return toTitleCase(getObject());
  }

  /**
   * Get a random possessive from the list of valid possessives.
   * If there are no valid possessives, returns "their".
   */
  public String getPossessive() {
    switch (possessives.size()) {
      case 0:
        return "their";
      case 1:
        return possessives.get(0);
      default:
        return possessives.get(rand.nextInt(possessives.size()));
    }
  }

  /***
   * Get a random possessive from the list of valid subjects with the starting letter capitalized.
   * If there are no valid subjects, returns "Their".
   */
  public String getPossessiveTitleCase() {
    return toTitleCase(getPossessive());
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
