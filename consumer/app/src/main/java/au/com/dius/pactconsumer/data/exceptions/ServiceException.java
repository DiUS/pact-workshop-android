package au.com.dius.pactconsumer.data.exceptions;

public class ServiceException extends RuntimeException {

  public ServiceException() { }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
