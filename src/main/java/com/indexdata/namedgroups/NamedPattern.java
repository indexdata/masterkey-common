package com.indexdata.namedgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedPattern {
    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<([^>]+)>");


	private Pattern pattern;
	private String namedPattern;
	private List<String> groupNames;

    public static NamedPattern compile(String regex) {
        return new NamedPattern(regex, 0);
    }

    public static NamedPattern compile(String regex, int flags) {
        return new NamedPattern(regex, flags);
    }

    private NamedPattern(String regex, int i) {
    	namedPattern = regex;
    	pattern = buildStandardPattern(regex);
    	groupNames = extractGroupNames(regex);
	}

	public int flags() {
		return pattern.flags();
	}

	public NamedMatcher matcher(CharSequence input) {
		return new NamedMatcher(this, input);
	}

	Pattern pattern() {
		return pattern;
	}

	public String standardPattern() {
		return pattern.pattern();
	}

	public String namedPattern() {
		return namedPattern;
	}

	public List<String> groupNames() {
		return groupNames;
	}

	public String[] split(CharSequence input, int limit) {
		return pattern.split(input, limit);
	}

	public String[] split(CharSequence input) {
		return pattern.split(input);
	}

	public String toString() {
		return namedPattern;
	}

	static List<String> extractGroupNames(String namedPattern) {
		List<String> names = new ArrayList<String>();
        int tt=0;
        int start=0;
        for (int i=0; i<namedPattern.length(); i++) {
          char c = namedPattern.charAt(i);
          switch (c) {
            case '(' : tt = 1; break;
            case '?' : if (tt == 1) tt = 2; break;
            case '<' : if (tt == 2) tt = 3; start = i; break;
            case '>' :
              if (tt == 3) {
                names.add(namedPattern.substring(start+1, i));
                tt = 0;
              }
              break;
            default:
              //do not rememebr groups that start with '(?'
              if (tt == 1) { 
                names.add(""); tt = 0;
              }
              else if (tt != 3) tt = 0;
          }
        }
		return names;
	}

	static Pattern buildStandardPattern(String namedPattern) {
        String stripped = NAMED_GROUP_PATTERN.matcher(namedPattern).replaceAll("(");
		return Pattern.compile(stripped);
	}

}
