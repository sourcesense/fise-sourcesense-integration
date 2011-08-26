package com.sourcesense.iksproject.enhance;

import java.io.Serializable;

/**
 * A page tag
 */
public class Tag implements Serializable{
  private String content;
  private Double relevance;

  public Tag(String content, Double relevance) {
    this.content = content;
    this.relevance = relevance;
  }

  public Tag(String content) {
    this.content = content;
    this.relevance = 0.0d;
  }

  public Double getRelevance() {
    return relevance;
  }

  public void setRelevance(Double relevance) {
    this.relevance = relevance;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return content;
  }
}
