package uk.gov.dwp.pdf.exception;

/**
 * Generic PDF generator exception.
 */
public class PdfaGeneratorException extends Exception {

  private static final long serialVersionUID = -7938858806844240510L;

  /**
   * Constructor.
   */
  public PdfaGeneratorException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor.
   */
  public PdfaGeneratorException(final String message) {
    super(message);
  }
}
