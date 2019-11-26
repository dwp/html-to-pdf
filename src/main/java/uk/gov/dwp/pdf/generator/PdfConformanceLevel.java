package uk.gov.dwp.pdf.generator;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The PDF conformance levels supported.
 */
@SuppressWarnings("PMD.CommentDefaultAccessModifier")
public enum PdfConformanceLevel {
  NONE,
  PDFA_1_A,
  PDFA_1_B,
  PDFA_2_A,
  PDFA_2_B,
  PDFA_2_U,
  PDFA_3_A,
  PDFA_3_B,
  PDFA_3_U,
  PDF_UA {
    @Override
    public void imposeOn(final PdfRendererBuilder builder) {
      LOGGER.info("building a PDF/UA accessible pdf");
      builder.usePdfUaAccessbility(true);
    }
  };

  static final Logger LOGGER = LoggerFactory.getLogger(PdfConformanceLevel.class.getName());

  /**
   * Sets the conformance level.
   */
  public void imposeOn(final PdfRendererBuilder builder) {
    LOGGER.info("building pdf to comply with conformance level {}", this.name());
    builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.valueOf(this.name()));
  }
}
