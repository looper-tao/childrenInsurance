package com.newtank.libra.children.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by looper on 2017/9/25.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST) // 400
public class OperationFailedException extends Exception {

  public OperationFailedException(String message) {
    super(message);
  }

}
