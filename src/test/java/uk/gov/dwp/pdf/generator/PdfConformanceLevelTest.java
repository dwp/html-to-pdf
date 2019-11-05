package uk.gov.dwp.pdf.generator;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PdfConformanceLevelTest {
  
  @Test
  public void imposesConformance() {
    PdfRendererBuilder builder = Mockito.mock(PdfRendererBuilder.class);
    ArgumentCaptor<PdfRendererBuilder.PdfAConformance> conformance 
        = ArgumentCaptor.forClass(PdfRendererBuilder.PdfAConformance.class);
    
    PdfConformanceLevel.PDFA_1_A.imposeOn(builder);
    
    Mockito.verify(builder, Mockito.times(1)).usePdfAConformance(conformance.capture());
    
    assertThat(conformance.getValue(), equalTo(PdfRendererBuilder.PdfAConformance.PDFA_1_A));
  }
  
  @Test
  public void imposePdfUa (){
    PdfRendererBuilder builder = Mockito.mock(PdfRendererBuilder.class);
    ArgumentCaptor<Boolean> conformance 
        = ArgumentCaptor.forClass(Boolean.class);
    
    PdfConformanceLevel.PDF_UA.imposeOn(builder);
    
    Mockito.verify(builder, Mockito.times(1)).usePdfUaAccessbility(conformance.capture());
    
    assertTrue(conformance.getValue());
  }
  
}
