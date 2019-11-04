package uk.gov.dwp.pdf.generator;

/**
 * Factory class for HtmlToPdfGenerator.
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public final class HtmlToPdfFactory {

  private HtmlToPdfFactory() {
  }

  /**
   * Returns the instance of the HtmlToPdfGenerator.
   *
   * @return
   */
  public static HtmlToPdfGenerator create() {
    return new HtmlToAccessiblePdfGenerator();
  }
}

