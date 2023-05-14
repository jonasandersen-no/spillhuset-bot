package com.bjoggis.spillhuset.type;

public enum Activity {
  DEV("Dev mode"),
  PRODUCTION("Chilling at bjoggis.com");


  private final String comment;

  Activity(String comment) {
    this.comment = comment;
  }

  public String getComment() {
    return comment;
  }
}
