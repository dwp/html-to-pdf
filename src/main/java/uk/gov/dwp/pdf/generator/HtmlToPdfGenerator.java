package uk.gov.dwp.pdf.generator;

import uk.gov.dwp.pdf.exception.PdfaGeneratorException;

import java.util.Map;

/**
 * Interface for an html to pdf generator.
 */
public interface HtmlToPdfGenerator {
  /**
   * Returns an accessible pdf from an html input.
   * It will perform a sanity check on the input html
   * to ensure that all the fonts declared in the html's font family
   * are present in the font map.
   *
   * @param html             - The input html
   * @param colourProfile    The desired colour profile
   * @param fontMap          The font map containing the mapping between
   *                         the font family from the html and the actual font bytes
   * @param conformanceLevel The pdf conformance level
   * @return
   */
  byte[] createPdfDocument(String html,
                           byte[] colourProfile,
                           Map<String, byte[]> fontMap,
                           PdfConformanceLevel conformanceLevel) throws PdfaGeneratorException;
}
