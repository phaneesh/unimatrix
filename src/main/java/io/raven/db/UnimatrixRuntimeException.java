package io.raven.db;

import lombok.Builder;

public class UnimatrixRuntimeException extends RuntimeException {

  @Builder(builderMethodName = "from", builderClassName = "UniMatrixRuntimeExceptionFromExceptionBuilder")
  public UnimatrixRuntimeException(Exception exception) {
    super(exception);
  }

  @Builder(builderMethodName = "fromMessage", builderClassName = "UniMatrixRuntimeExceptionFromMessageBuilder")
  public UnimatrixRuntimeException(String message) {
    super(message);
  }
}
