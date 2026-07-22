package com.example.texttopdftool.model;

public class DocumentTemplate {

	private final String id;
	private final String displayName;
	private final float marginTop;
	private final float marginBottom;
	private final float marginLeft;
	private final float marginRight;
	private final float lineSpacing;
	private final String alignment;
	private final int fontSize;
	private final boolean codeMode;

	public DocumentTemplate(String id, String displayName, float marginTop, float marginBottom, float marginLeft,
			float marginRight, float lineSpacing, String alignment, int fontSize, boolean codeMode) {
		this.id = id;
		this.displayName = displayName;
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		this.lineSpacing = lineSpacing;
		this.alignment = alignment;
		this.fontSize = fontSize;
		this.codeMode = codeMode;
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public float getMarginTop() {
		return marginTop;
	}

	public float getMarginBottom() {
		return marginBottom;
	}

	public float getMarginLeft() {
		return marginLeft;
	}

	public float getMarginRight() {
		return marginRight;
	}

	public float getLineSpacing() {
		return lineSpacing;
	}

	public String getAlignment() {
		return alignment;
	}

	public int getFontSize() {
		return fontSize;
	}

	public boolean isCodeMode() {
		return codeMode;
	}
}
