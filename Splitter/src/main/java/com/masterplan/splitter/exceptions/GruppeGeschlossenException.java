package com.masterplan.splitter.exceptions;

public class GruppeGeschlossenException extends Exception {

  public GruppeGeschlossenException() {}

  public GruppeGeschlossenException(String message) {
    super(message);
  }

  public GruppeGeschlossenException(String message, Throwable cause) {
    super(message, cause);
  }

  public GruppeGeschlossenException(Throwable cause) {
    super(cause);
  }
}
