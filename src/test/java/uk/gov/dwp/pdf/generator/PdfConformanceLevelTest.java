package uk.gov.dwp.pdf.generator;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PdfConformanceLevelTest {

  @Test
  public void imposesConformance() {
    PdfRendererBuilder builder = mock(PdfRendererBuilder.class);
    ArgumentCaptor<PdfRendererBuilder.PdfAConformance> conformance
        = ArgumentCaptor.forClass(PdfRendererBuilder.PdfAConformance.class);

    PdfConformanceLevel.PDFA_1_A.imposeOn(builder);

    verify(builder).usePdfAConformance(conformance.capture());

    assertThat(conformance.getValue(), equalTo(PdfRendererBuilder.PdfAConformance.PDFA_1_A));
  }

  @Test
  public void imposePdfUa() {
    PdfRendererBuilder builder = mock(PdfRendererBuilder.class);
    ArgumentCaptor<Boolean> conformance
        = ArgumentCaptor.forClass(Boolean.class);

    PdfConformanceLevel.PDF_UA.imposeOn(builder);

    verify(builder).usePdfUaAccessbility(conformance.capture());

    assertTrue(conformance.getValue());
  }
}
