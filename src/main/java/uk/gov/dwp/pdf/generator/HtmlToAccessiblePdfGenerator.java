package uk.gov.dwp.pdf.generator;

import com.openhtmltopdf.pdfboxout.PdfBoxFontResolver;
import com.openhtmltopdf.pdfboxout.PdfBoxRenderer;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.pdf.exception.PdfaGeneratorException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the HtmlToPdfGenerator to create PDFs
 * at various conformance levels using the openhtmltopdf library.
 */
@SuppressWarnings({"PMD.CommentDefaultAccessModifier", "PMD.AvoidCatchingGenericException"})
class HtmlToAccessiblePdfGenerator implements HtmlToPdfGenerator {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      HtmlToAccessiblePdfGenerator.class.getName());

  @Override
  public byte[] createPdfDocument(final String html,
                                  final byte[] colourProfile,
                                  final Map<String, byte[]> fontMap,
                                  final PdfConformanceLevel conformanceLevel)
      throws PdfaGeneratorException {

    try {
      byte[] pdf;

      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

        final PdfRendererBuilder pdfBuilder = new PdfRendererBuilder()
            .defaultTextDirection(PdfRendererBuilder.TextDirection.LTR)
            .useColorProfile(colourProfile)
            .useSVGDrawer(new BatikSVGDrawer())
            .withHtmlContent(html, null)
            .useFastMode()
            .toStream(outputStream);

        setConformanceLevel(pdfBuilder, conformanceLevel);

        verifyFontApplication(fontMap, html, conformanceLevel);

        final PdfBoxRenderer pdfBoxRenderer = pdfBuilder.buildPdfRenderer();
        populateFontResolver(pdfBoxRenderer.getFontResolver(), fontMap);
        pdfBoxRenderer.createPDF();
        pdf = outputStream.toByteArray();
        LOGGER.info("successfully generated pdf");
      }

      return pdf;

    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      LOGGER.debug(e.getClass().getName(), e);
      throw new PdfaGeneratorException(e.getMessage(), e);
    }
  }

  private void setConformanceLevel(final PdfRendererBuilder pdfBuilder,
                                   final PdfConformanceLevel conformanceLevel) {
    if (conformanceLevel == null) {
      throw new IllegalArgumentException("Conformance level must not be null");
    }

    if (conformanceLevel == PdfConformanceLevel.PDF_UA) {
      LOGGER.info("building a PDF/UA accessible pdf");
      pdfBuilder.usePdfUaAccessbility(true);
    } else {
      LOGGER.info("building pdf to comply with conformance level {}", conformanceLevel);
      pdfBuilder.usePdfAConformance(
          PdfRendererBuilder.PdfAConformance.valueOf(conformanceLevel.name()));
    }
  }

  private void verifyFontApplication(final Map<String, byte[]> fontMap,
                                     final String html,
                                     final PdfConformanceLevel conformanceLevel)
      throws PdfaGeneratorException {

    if (html != null && conformanceLevel != PdfConformanceLevel.NONE) {
      LOGGER.debug("validate that all fonts in the document are contained in the font map");

      for (final String fontHtml : Arrays.stream(html.split("\n"))
          .filter(line -> line.contains("font-family"))
          .map(String::trim)
          .collect(Collectors.toList())) {
        boolean fontMissing = true;

        for (final String item : fontMap.keySet()) {
          if (fontHtml.contains(item)) {
            LOGGER.debug("successfully found embedded font {}'", item);
            fontMissing = false;
            break;
          }
        }

        if (fontMissing) {
          throw new PdfaGeneratorException(
              String.format("html element requests %s. "
                      + "It is not passed in the font map, cannot encode.",
                  fontHtml.replaceAll(";", "").trim()));
        }
      }
    }
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void populateFontResolver(final PdfBoxFontResolver fontResolver,
                                    final Map<String, byte[]> fontMap) {
    for (final Map.Entry<String, byte[]> entry : fontMap.entrySet()) {
      fontResolver
          .addFont(
              () ->
                  new ByteArrayInputStream(entry.getValue()), entry.getKey(), null, null, false);
      LOGGER.debug("adding font '{}' to font map", entry.getKey());
    }
  }
}