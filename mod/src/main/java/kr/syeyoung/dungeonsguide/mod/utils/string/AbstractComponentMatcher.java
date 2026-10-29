/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.utils.string;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractComponentMatcher implements ComponentMatcher {
    static final int WHITE = 0xFFFFFF;
    static final int OBFUSCATED = 1, BOLD = 2, STRIKETHROUGH = 4, UNDERLINED = 8, ITALIC = 16;

    protected static final int MATCHED = -1, FAILED = -2;

    record Spec(boolean any, int rgb, int flags, FontDescription font, Integer shadow) {
        boolean matches(int rgb, int flags, FontDescription font, Integer shadow) {
            return any || this.rgb == rgb && this.flags == flags && this.font.equals(font) && Objects.equals(this.shadow, shadow);
        }
    }

    private record Part(String text, Style style) {}

    record ParsedPattern(boolean anchoredStart, boolean anchoredEnd, int[] codePoints, Spec[] specs) {}

    private static final Spec PLAIN = new Spec(false, WHITE, 0, FontDescription.DEFAULT, null);
    private static final Spec ANY = new Spec(true, WHITE, 0, FontDescription.DEFAULT, null);

    private static final ChatFormatting[] FORMATTING_BY_CODE = new ChatFormatting[128];
    static {
        for (ChatFormatting formatting : ChatFormatting.values()) {
            FORMATTING_BY_CODE[formatting.getChar()] = formatting;
            FORMATTING_BY_CODE[Character.toUpperCase(formatting.getChar())] = formatting;
        }
    }

    protected final int[] codePoints;
    protected final Spec[] specs;

    private Component[] stackNodes = new Component[8];
    private int[] stackNextSibling = new int[8];
    private int[] stackRgb = new int[8];
    private int[] stackFlags = new int[8];
    private FontDescription[] stackFont = new FontDescription[8];
    private Integer[] stackShadow = new Integer[8];

    private int[] textCodePoints = new int[32];
    private int[] textRgb = new int[32];
    private int[] textFlags = new int[32];

    protected AbstractComponentMatcher(int[] codePoints, Spec[] specs) {
        this.codePoints = codePoints;
        this.specs = specs;
    }

    protected abstract int step(int state, int cp, int rgb, int flags, FontDescription font, Integer shadow);

    protected boolean accepts(int position, int cp, int rgb, int flags, FontDescription font, Integer shadow) {
        return codePoints[position] == cp && specs[position].matches(rgb, flags, font, shadow);
    }


    static ParsedPattern parse(String pattern) {
        int start = 0, end = pattern.length();
        boolean anchoredStart = pattern.startsWith("^");
        if (anchoredStart) start++;
        boolean anchoredEnd = end > start && pattern.charAt(end - 1) == '$' && !escaped(pattern, end - 1);
        if (anchoredEnd) end--;

        List<Integer> codePoints = new ArrayList<>();
        List<Spec> specs = new ArrayList<>();
        Spec spec = PLAIN;
        for (int i = start; i < end; ) {
            int cp = pattern.codePointAt(i);
            if (cp == '§') {
                if (i + 1 >= end) throw new IllegalArgumentException("Dangling § in " + pattern);
                if (pattern.charAt(i + 1) == '[') {
                    int close = pattern.indexOf(']', i + 2);
                    if (close == -1 || close >= end) throw new IllegalArgumentException("Unclosed §[ in " + pattern);
                    spec = parseSpec(pattern.substring(i + 2, close), pattern);
                    i = close + 1;
                } else if (pattern.charAt(i + 1) == '?') {
                    spec = ANY;
                    i += 2;
                } else {
                    spec = parseSpec(String.valueOf(pattern.charAt(i + 1)), pattern);
                    i += 2;
                }
                continue;
            }
            if (cp == '\\') {
                if (i + 1 >= end) throw new IllegalArgumentException("Dangling \\ in " + pattern);
                i++;
                cp = pattern.codePointAt(i);
            } else if (cp == '^' || cp == '$') {
                throw new IllegalArgumentException("Unescaped " + (char) cp + " inside " + pattern);
            }
            codePoints.add(cp);
            specs.add(spec);
            i += Character.charCount(cp);
        }
        return new ParsedPattern(anchoredStart, anchoredEnd, codePoints.stream().mapToInt(Integer::intValue).toArray(), specs.toArray(new Spec[0]));
    }

    private static boolean escaped(String pattern, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && pattern.charAt(i) == '\\'; i--) backslashes++;
        return backslashes % 2 == 1;
    }

    private static Spec parseSpec(String body, String pattern) {
        String[] parts = body.split(",", -1);
        int rgb = WHITE, flags = 0;
        FontDescription font = FontDescription.DEFAULT;
        Integer shadow = null;
        int properties = parts[0].indexOf('=') == -1 ? 1 : 0;
        for (char code : (properties == 1 ? parts[0] : "").toCharArray()) {
            ChatFormatting formatting = ChatFormatting.getByCode(code);
            if (formatting == null) throw new IllegalArgumentException("Unknown style code " + code + " in " + pattern);
            if (formatting == ChatFormatting.RESET) continue;
            if (formatting.isColor()) rgb = formatting.getColor();
            else flags |= flag(formatting);
        }
        for (int i = properties; i < parts.length; i++) {
            int equals = parts[i].indexOf('=');
            if (equals == -1) throw new IllegalArgumentException("Bad style property " + parts[i] + " in " + pattern);
            String key = parts[i].substring(0, equals), value = parts[i].substring(equals + 1);
            switch (key) {
                case "color" -> {
                    if (!value.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("Bad color " + value + " in " + pattern);
                    rgb = Integer.parseInt(value.substring(1), 16);
                }
                case "bgcolor" -> {
                    if (value.matches("#[0-9a-fA-F]{6}")) shadow = 0xFF000000 | Integer.parseInt(value.substring(1), 16);
                    else if (value.matches("#[0-9a-fA-F]{8}")) shadow = (int) Long.parseLong(value.substring(1), 16);
                    else throw new IllegalArgumentException("Bad bgcolor " + value + " in " + pattern);
                }
                case "font" -> font = new FontDescription.Resource(Identifier.parse(value));
                default -> throw new IllegalArgumentException("Unknown style property " + key + " in " + pattern);
            }
        }
        return new Spec(false, rgb, flags, font, shadow);
    }

    private static int flag(ChatFormatting formatting) {
        return switch (formatting) {
            case OBFUSCATED -> OBFUSCATED;
            case BOLD -> BOLD;
            case STRIKETHROUGH -> STRIKETHROUGH;
            case UNDERLINE -> UNDERLINED;
            case ITALIC -> ITALIC;
            default -> 0;
        };
    }

    private static ChatFormatting formattingByCode(char code) {
        return code < FORMATTING_BY_CODE.length ? FORMATTING_BY_CODE[code] : null;
    }

    private static int applyFlag(int flags, Boolean value, int bit) {
        if (value == null) return flags;
        return value ? flags | bit : flags & ~bit;
    }

    private void push(Component component, int depth, int rgb, int flags, FontDescription font, Integer shadow) {
        Style style = component.getStyle();
        if (style.color != null) rgb = style.color.getValue();
        flags = applyFlag(flags, style.obfuscated, OBFUSCATED);
        flags = applyFlag(flags, style.bold, BOLD);
        flags = applyFlag(flags, style.strikethrough, STRIKETHROUGH);
        flags = applyFlag(flags, style.underlined, UNDERLINED);
        flags = applyFlag(flags, style.italic, ITALIC);
        if (style.font != null) font = style.font;
        if (style.shadowColor != null) shadow = style.shadowColor;

        if (depth == stackNodes.length) {
            int capacity = depth * 2;
            stackNodes = Arrays.copyOf(stackNodes, capacity);
            stackNextSibling = Arrays.copyOf(stackNextSibling, capacity);
            stackRgb = Arrays.copyOf(stackRgb, capacity);
            stackFlags = Arrays.copyOf(stackFlags, capacity);
            stackFont = Arrays.copyOf(stackFont, capacity);
            stackShadow = Arrays.copyOf(stackShadow, capacity);
        }
        stackNodes[depth] = component;
        stackRgb[depth] = rgb;
        stackFlags[depth] = flags;
        stackFont[depth] = font;
        stackShadow[depth] = shadow;
    }

    private void clear(int depth) {
        for (int i = 0; i < depth; i++) stackNodes[i] = null;
    }

    protected int walkForward(Component root) {
        push(root, 0, WHITE, 0, FontDescription.DEFAULT, null);
        stackNextSibling[0] = 0;
        int state = feedNodeForward(0, 0);
        int depth = 1;
        while (depth > 0 && state >= 0) {
            int top = depth - 1;
            List<Component> siblings = stackNodes[top].getSiblings();
            int next = stackNextSibling[top];
            if (next < siblings.size()) {
                stackNextSibling[top] = next + 1;
                push(siblings.get(next), depth, stackRgb[top], stackFlags[top], stackFont[top], stackShadow[top]);
                stackNextSibling[depth] = 0;
                state = feedNodeForward(depth, state);
                depth++;
            } else {
                stackNodes[top] = null;
                depth--;
            }
        }
        clear(depth);
        return state;
    }

    protected int walkBackward(Component root) {
        push(root, 0, WHITE, 0, FontDescription.DEFAULT, null);
        stackNextSibling[0] = root.getSiblings().size() - 1;
        int state = 0;
        int depth = 1;
        while (depth > 0 && state >= 0) {
            int top = depth - 1;
            int next = stackNextSibling[top];
            if (next >= 0) {
                stackNextSibling[top] = next - 1;
                Component sibling = stackNodes[top].getSiblings().get(next);
                push(sibling, depth, stackRgb[top], stackFlags[top], stackFont[top], stackShadow[top]);
                stackNextSibling[depth] = sibling.getSiblings().size() - 1;
                depth++;
            } else {
                state = feedNodeBackward(top, state);
                stackNodes[top] = null;
                depth--;
            }
        }
        clear(depth);
        return state;
    }

    private int feedNodeForward(int index, int state) {
        Component component = stackNodes[index];
        if (component.getContents() instanceof PlainTextContents text)
            return feedForward(text.text(), state, stackRgb[index], stackFlags[index], stackFont[index], stackShadow[index]);
        for (Part part : visitParts(component, index)) {
            Style style = part.style();
            state = feedForward(part.text(), state, rgbOf(style), flagsOf(style), style.getFont(), style.getShadowColor());
            if (state < 0) return state;
        }
        return state;
    }

    private int feedNodeBackward(int index, int state) {
        Component component = stackNodes[index];
        if (component.getContents() instanceof PlainTextContents text)
            return feedBackward(text.text(), state, stackRgb[index], stackFlags[index], stackFont[index], stackShadow[index]);
        List<Part> parts = visitParts(component, index);
        for (int i = parts.size() - 1; i >= 0; i--) {
            Style style = parts.get(i).style();
            state = feedBackward(parts.get(i).text(), state, rgbOf(style), flagsOf(style), style.getFont(), style.getShadowColor());
            if (state < 0) return state;
        }
        return state;
    }

    private List<Part> visitParts(Component component, int index) {
        int flags = stackFlags[index];
        Style resolved = Style.EMPTY.withColor(TextColor.fromRgb(stackRgb[index]))
                .withObfuscated((flags & OBFUSCATED) != 0).withBold((flags & BOLD) != 0)
                .withStrikethrough((flags & STRIKETHROUGH) != 0).withUnderlined((flags & UNDERLINED) != 0)
                .withItalic((flags & ITALIC) != 0).withFont(stackFont[index]);
        resolved = stackShadow[index] == null ? resolved.withoutShadow() : resolved.withShadowColor(stackShadow[index]);
        List<Part> parts = new ArrayList<>();
        component.getContents().visit((partStyle, text) -> {
            parts.add(new Part(text, partStyle));
            return Optional.empty();
        }, resolved);
        return parts;
    }

    private static int rgbOf(Style style) {
        return style.getColor() == null ? WHITE : style.getColor().getValue();
    }

    private static int flagsOf(Style style) {
        return (style.isObfuscated() ? OBFUSCATED : 0) | (style.isBold() ? BOLD : 0)
                | (style.isStrikethrough() ? STRIKETHROUGH : 0) | (style.isUnderlined() ? UNDERLINED : 0)
                | (style.isItalic() ? ITALIC : 0);
    }

    private int feedForward(String text, int state, int rgb, int flags, FontDescription font, Integer shadow) {
        int baseRgb = rgb, baseFlags = flags;
        for (int i = 0, length = text.length(); i < length; ) {
            char ch = text.charAt(i);
            if (ch == '§') {
                if (i + 1 >= length) break;
                ChatFormatting formatting = formattingByCode(text.charAt(i + 1));
                if (formatting == ChatFormatting.RESET) {
                    rgb = baseRgb;
                    flags = baseFlags;
                } else if (formatting != null && formatting.isColor()) {
                    rgb = formatting.getColor();
                    flags = 0;
                } else if (formatting != null) {
                    flags |= flag(formatting);
                }
                i += 2;
                continue;
            }
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            state = step(state, cp, rgb, flags, font, shadow);
            if (state < 0) return state;
        }
        return state;
    }

    private int feedBackward(String text, int state, int rgb, int flags, FontDescription font, Integer shadow) {
        if (text.indexOf('§') == -1) {
            for (int i = text.length(); i > 0; ) {
                int cp = text.codePointBefore(i);
                i -= Character.charCount(cp);
                state = step(state, cp, rgb, flags, font, shadow);
                if (state < 0) return state;
            }
            return state;
        }

        int count = 0, baseRgb = rgb, baseFlags = flags;
        for (int i = 0, length = text.length(); i < length; ) {
            char ch = text.charAt(i);
            if (ch == '§') {
                if (i + 1 >= length) break;
                ChatFormatting formatting = formattingByCode(text.charAt(i + 1));
                if (formatting == ChatFormatting.RESET) {
                    rgb = baseRgb;
                    flags = baseFlags;
                } else if (formatting != null && formatting.isColor()) {
                    rgb = formatting.getColor();
                    flags = 0;
                } else if (formatting != null) {
                    flags |= flag(formatting);
                }
                i += 2;
                continue;
            }
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (count == textCodePoints.length) {
                textCodePoints = Arrays.copyOf(textCodePoints, count * 2);
                textRgb = Arrays.copyOf(textRgb, count * 2);
                textFlags = Arrays.copyOf(textFlags, count * 2);
            }
            textCodePoints[count] = cp;
            textRgb[count] = rgb;
            textFlags[count] = flags;
            count++;
        }
        for (int i = count - 1; i >= 0; i--) {
            state = step(state, textCodePoints[i], textRgb[i], textFlags[i], font, shadow);
            if (state < 0) return state;
        }
        return state;
    }
}
