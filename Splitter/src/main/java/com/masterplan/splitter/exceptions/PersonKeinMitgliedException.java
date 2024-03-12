package com.masterplan.splitter.exceptions;

public class PersonKeinMitgliedException extends Exception {

  public PersonKeinMitgliedException() {}

  public PersonKeinMitgliedException(String message) {
    super(message);
  }

  public PersonKeinMitgliedException(String message, Throwable cause) {
    super(message, cause);
  }

  public PersonKeinMitgliedException(Throwable cause) {
    super(cause);
  }
}
