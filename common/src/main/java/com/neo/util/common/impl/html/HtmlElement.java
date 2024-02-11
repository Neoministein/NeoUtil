package com.neo.util.common.impl.html;

import java.util.stream.IntStream;

public record HtmlElement(String content) implements CharSequence {
    @Override
    public int length() {
        return content.length();
    }

    @Override
    public char charAt(int index) {
        return content.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return content.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return content.chars();
    }

    @Override
    public IntStream codePoints() {
        return content.codePoints();
    }

    @Override
    public String toString() {
        return content;
    }
}
