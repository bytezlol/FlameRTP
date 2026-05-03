package de.flamesmp.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ColorUtil {

    private static final Pattern HEX_PATTERN =
            Pattern.compile("(?:&#|#|<#)([0-9A-Fa-f]{6})>?");

    private static final Pattern LEGACY_PATTERN =
            Pattern.compile("[§&]([0-9a-fk-orA-FK-OR])");

    private static final char[] LEGACY_CHARS = "0123456789abcdefklmnor".toCharArray();
    private static final String[] MINI_TAGS = {
            "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>",
            "<dark_red>", "<dark_purple>", "<gold>", "<gray>",
            "<dark_gray>", "<blue>", "<green>", "<aqua>",
            "<red>", "<light_purple>", "<yellow>", "<white>",
            "<obf>", "<b>", "<st>", "<u>", "<i>", "<reset>"
    };

    private static String translate(final String input) {
        if (input == null) return null;

        String result = input;

        final Matcher hexMatcher = HEX_PATTERN.matcher(result);
        final StringBuilder hexBuilder = new StringBuilder();

        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuilder, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(hexBuilder);
        result = hexBuilder.toString();

        final Matcher legacyMatcher = LEGACY_PATTERN.matcher(result);
        final StringBuilder legacyBuilder = new StringBuilder();
        while (legacyMatcher.find()) {
            final char code = Character.toLowerCase(legacyMatcher.group(1).charAt(0));
            final int index = new String(LEGACY_CHARS).indexOf(code);
            if (index >= 0 && index < MINI_TAGS.length) {
                legacyMatcher.appendReplacement(legacyBuilder,
                        Matcher.quoteReplacement(MINI_TAGS[index]));
            } else {
                legacyMatcher.appendReplacement(legacyBuilder, "");
            }
        }
        legacyMatcher.appendTail(legacyBuilder);
        result = legacyBuilder.toString();

        return result;
    }

    public static Component parse(final String input) {
        return MiniMessage.miniMessage()
                .deserialize(translate(input))
                .decoration(TextDecoration.ITALIC, false);
    }
}