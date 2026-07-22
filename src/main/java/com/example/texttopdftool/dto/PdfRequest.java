package com.example.texttopdftool.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PdfRequest {

	@JsonAlias("title")
	private String fileName = "";
	private String heading = "";
	private String content = "";
	private int fontSize = 14;
	private String pageSize = "A4";
	private float marginTop = 36f;
	private float marginBottom = 36f;
	private float marginLeft = 36f;
	private float marginRight = 36f;
	private float lineSpacing = 1.2f;
	private String alignment = "LEFT";
	private String template = "notes";
	private String orgName;
	private String language = "en";

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getHeading() {
		return heading;
	}

	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public String getPageSize() {
		return pageSize;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public float getMarginTop() {
		return marginTop;
	}

	public void setMarginTop(float marginTop) {
		this.marginTop = marginTop;
	}

	public float getMarginBottom() {
		return marginBottom;
	}

	public void setMarginBottom(float marginBottom) {
		this.marginBottom = marginBottom;
	}

	public float getMarginLeft() {
		return marginLeft;
	}

	public void setMarginLeft(float marginLeft) {
		this.marginLeft = marginLeft;
	}

	public float getMarginRight() {
		return marginRight;
	}

	public void setMarginRight(float marginRight) {
		this.marginRight = marginRight;
	}

	public float getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
