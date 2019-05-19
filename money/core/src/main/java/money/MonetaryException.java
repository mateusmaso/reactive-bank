package money;

public abstract class MonetaryException extends RuntimeException {
  private static final long serialVersionUID = 1025388271622292075L;
  private String errorCode;

  public MonetaryException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return this.errorCode;
  }
}