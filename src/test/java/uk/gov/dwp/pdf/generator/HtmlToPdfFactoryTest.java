package uk.gov.dwp.pdf.generator;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HtmlToPdfFactoryTest {

  @Test
  public void shouldReturnAnInstanceOfHtmlToAccessiblePdfGenerator() {
    assertThat(HtmlToPdfFactory.create() instanceof HtmlToAccessiblePdfGenerator, is(true));
  }
}
