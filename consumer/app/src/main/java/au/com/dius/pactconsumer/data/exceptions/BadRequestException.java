package au.com.dius.pactconsumer.data.exceptions;

public class BadRequestException extends ServiceException {

  public BadRequestException() {

  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

}
