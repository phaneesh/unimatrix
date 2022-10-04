package io.raven.db;

public class UniMatrixException extends RuntimeException {

  public UniMatrixException(Exception exception) {
    super(exception);
  }

  public UniMatrixException(String message) {
    super(message);
  }
}
