package io.raven.db;

import lombok.Builder;

public class UniMatrixException extends Exception {

  @Builder(builderMethodName = "from", builderClassName = "UniMatrixExceptionFromExceptionBuilder")
  public UniMatrixException(Exception exception) {
    super(exception);
  }

  @Builder(builderMethodName = "fromMessage", builderClassName = "UniMatrixExceptionFromMessageBuilder")
  public UniMatrixException(String message) {
    super(message);
  }
}
