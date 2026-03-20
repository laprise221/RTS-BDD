package com.rts.model;

public class Step {
    private String keyword;  // Given, When, Then, And, But
    private String text;     // une phrase qui décrit une étape en fonction du keyword
    public Step(String keyword, String text) {
        this.keyword = keyword;
        this.text = text;
    }

    public String getKeyword() { return keyword; }
    public String getText() { return text; }
}
