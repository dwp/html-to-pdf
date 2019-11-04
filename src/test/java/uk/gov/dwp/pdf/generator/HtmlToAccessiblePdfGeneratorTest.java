package uk.gov.dwp.pdf.generator;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.impl.VeraPDFMeta;
import com.adobe.xmp.impl.VeraPDFXMPNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.dwp.pdf.exception.PdfaGeneratorException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.dwp.pdf.generator.PdfConformanceLevel.NONE;
import static uk.gov.dwp.pdf.generator.PdfConformanceLevel.PDFA_1_A;
import static uk.gov.dwp.pdf.generator.PdfConformanceLevel.PDFA_1_B;
import static uk.gov.dwp.pdf.generator.PdfConformanceLevel.PDF_UA;

@SuppressWarnings("squid:S1192") // string literals allowed
public class HtmlToAccessiblePdfGeneratorTest {
  private static Map<String, byte[]> defaultFontMap;
  private static byte[] defaultColourProfile;
  private static String htmlFile;

  private HtmlToAccessiblePdfGenerator instance;

  @BeforeClass
  public static void init() throws IOException {
    defaultColourProfile = FileUtils.readFileToByteArray(new File("src/test/resources/colours/sRGB.icm"));
    htmlFile = FileUtils.readFileToString(new File("src/test/resources/successfulHtml.html"));

    defaultFontMap = new HashMap<>();
    defaultFontMap.put("courier", FileUtils.readFileToByteArray(new File("src/test/resources/fonts/courier.ttf")));
    defaultFontMap.put("arial", FileUtils.readFileToByteArray(new File("src/test/resources/fonts/arial.ttf")));

  }

  @Before
  public void setup() {
    instance = new HtmlToAccessiblePdfGenerator();
  }

  @Test
  public void successfullyCreatePdfaBasic() throws IOException, PdfaGeneratorException, XmpParsingException, XMPException {
    byte[] pdf = instance.createPdfDocument(htmlFile, defaultColourProfile, defaultFontMap, PDFA_1_A);
    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(1)));
    assertNotNull(pdfDoc.getDocumentCatalog().getMetadata());

    validateDocumentConformance(pdfDoc, PDFA_1_A);
  }

  @Test
  public void successfullyCreatePdfContainingAnSvg() throws PdfaGeneratorException, IOException, XmpParsingException, XMPException {
    final String htmlWithSvgFile = FileUtils.readFileToString(new File("src/test/resources/htmlWithSvg.html"));
    final byte[] pdf = instance.createPdfDocument(htmlWithSvgFile, defaultColourProfile, defaultFontMap, PDF_UA);

    final Set<COSBase> visited = new HashSet<>();
    final List<COSString> cosStrings = PDDocument.load(pdf).getDocument().getObjects().stream()
        .flatMap(o -> getCOSStrings(o, visited).stream())
        .collect(Collectors.toList());

    assertThat(cosStrings.stream().map(COSString::getASCII).collect(Collectors.toList()), hasItem(is("svg")));
  }

  @Test
  public void successfullyCreateAccessiblePdfUA() throws IOException, PdfaGeneratorException, XmpParsingException, XMPException {
    String accessibleHtml = FileUtils.readFileToString(new File("src/test/resources/accessible-test.html"));

    byte[] pdf = instance.createPdfDocument(accessibleHtml, defaultColourProfile, defaultFontMap, PDF_UA);
    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(2)));
    assertNotNull(pdfDoc.getDocumentCatalog().getMetadata());

    validateDocumentConformance(pdfDoc, PDF_UA);
  }

  @Test
  public void failWithNullConformanceLevel() {
    try {
      instance.createPdfDocument("bad-conformance", defaultColourProfile, defaultFontMap, null);
      fail("should have failed with null conformance");
    } catch (PdfaGeneratorException e) {
      assertThat(e.getMessage(), is(equalTo("Conformance level must not be null")));
    }
  }

  @Test
  public void successfullyCreatePdfaBMultiPage() throws IOException, PdfaGeneratorException, XmpParsingException, XMPException {
    String pageBreakHtml = FileUtils.readFileToString(new File("src/test/resources/pageBreaksHtml.html"));
    byte[] pdf = instance.createPdfDocument(pageBreakHtml, defaultColourProfile, defaultFontMap, PDFA_1_A);
    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(6)));
    assertNotNull(pdfDoc.getDocumentCatalog().getMetadata());

    validateDocumentConformance(pdfDoc, PDFA_1_A);
  }

  @Test
  public void failureWithUndefinedFontInHtml() throws IOException {
    try {
      instance.createPdfDocument(
          FileUtils.readFileToString(new File("src/test/resources/noFontSupplied.html")),
          defaultColourProfile,
          defaultFontMap,
          PDFA_1_A);

      fail("should have thrown an error");

    } catch (PdfaGeneratorException e) {
      assertThat(e.getMessage(), startsWith("html element requests font-family: 'tahoma'"));
    }
  }

  @Test
  public void incorrectFormatWithBadImageRenderingInHtml() throws IOException, PdfaGeneratorException, XmpParsingException, XMPException {
    byte[] pdf = instance.createPdfDocument(
        FileUtils.readFileToString(new File("src/test/resources/imageFailureHtml.html")),
        defaultColourProfile,
        defaultFontMap,
        PDFA_1_A);

    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(1)));
    assertNotNull(pdfDoc.getDocumentCatalog().getMetadata());

    validateDocumentConformance(pdfDoc, PDFA_1_A);
  }

  @Test
  public void changeConformanceLevelIsHandled() throws IOException, PdfaGeneratorException, XmpParsingException, XMPException {
    byte[] pdf = instance.createPdfDocument(htmlFile, defaultColourProfile, defaultFontMap, PDFA_1_B);
    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(1)));
    assertNotNull(pdfDoc.getDocumentCatalog().getMetadata());

    validateDocumentConformance(pdfDoc, PDFA_1_B);
  }

  @Test
  public void noConformanceLevelIsHandled() throws IOException, PdfaGeneratorException {
    byte[] pdf = instance.createPdfDocument(htmlFile, defaultColourProfile, defaultFontMap, NONE);
    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(1)));
    assertNull(pdfDoc.getDocumentCatalog().getMetadata());
  }

  @Test
  public void testSuccessWithOverrideFontForArial() throws IOException, PdfaGeneratorException, XmpParsingException, XMPException {
    defaultFontMap.replace("arial", FileUtils.readFileToByteArray(new File("src/test/resources/fonts/arialbd.ttf")));

    byte[] pdf = instance.createPdfDocument(htmlFile, defaultColourProfile, defaultFontMap, PDFA_1_A);
    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(1)));
    assertNotNull(pdfDoc.getDocumentCatalog().getMetadata());

    validateDocumentConformance(pdfDoc, PDFA_1_A);
  }

  @Test
  public void testFailureWithBadHtml() throws IOException {
    try {
      instance.createPdfDocument(
          FileUtils.readFileToString(new File("src/test/resources/badHtmlFile.html")),
          defaultColourProfile,
          defaultFontMap,
          PDFA_1_A);
      fail("should have thrown an error");

    } catch (PdfaGeneratorException e) {
      assertThat(e.getMessage(), startsWith("Can't load the XML resource"));
    }
  }

  @Test
  public void testFailureWithBadlyNamedFontOverride() throws IOException {
    Map<String, byte[]> fontMap = new HashMap<>();
    fontMap.put("aaarial", FileUtils.readFileToByteArray(new File("src/test/resources/fonts/arialbd.ttf")));

    try {
      instance.createPdfDocument(htmlFile, defaultColourProfile, fontMap, PDFA_1_A);
      fail("should have thrown an error");

    } catch (PdfaGeneratorException e) {
      assertThat(e.getMessage(), startsWith("html element requests font-family: 'courier'"));
    }
  }

  @Test
  public void testFailureWithNullFontOverride() {
    try {
      instance.createPdfDocument(htmlFile, defaultColourProfile, null, NONE);
      fail("should have thrown an error");

    } catch (PdfaGeneratorException e) {
      assertThat(e.getCause().getClass().getName(), is(equalTo(NullPointerException.class.getName())));
    }
  }

  @Test
  public void testSuccessWithBadlyNamedFontOverrideIsOkOnNone() throws IOException, PdfaGeneratorException {
    Map<String, byte[]> fontMap = new HashMap<>();
    fontMap.put("aaarial", FileUtils.readFileToByteArray(new File("src/test/resources/fonts/arialbd.ttf")));
    instance.createPdfDocument(htmlFile, defaultColourProfile, fontMap, NONE);
  }

  @Test
  public void testSuccessWithOverrideColourProfile() throws IOException, XmpParsingException, PdfaGeneratorException, XMPException {
    byte[] colourProfile = FileUtils.readFileToByteArray(new File("src/test/resources/colours/sRGB.icm"));

    byte[] pdf = instance.createPdfDocument(htmlFile, colourProfile, defaultFontMap, PDFA_1_A);
    PDDocument pdfDoc = PDDocument.load(pdf);

    assertThat(pdfDoc.getNumberOfPages(), is(equalTo(1)));
    assertNotNull(pdfDoc.getDocumentCatalog().getMetadata());

    validateDocumentConformance(pdfDoc, PDFA_1_A);
  }

  @Test
  public void testFailureWithBadColourProfileOverride() {
    try {
      instance.createPdfDocument(htmlFile, "i-am-a-colour-profile".getBytes(), defaultFontMap, PDFA_1_A);
      fail("should have thrown an error");

    } catch (PdfaGeneratorException e) {
      assertThat(e.getMessage(), containsString("Invalid ICC Profile Data"));
    }
  }

  private void validateDocumentConformance(PDDocument pdfDoc, PdfConformanceLevel conformance) throws IOException, XMPException, XmpParsingException {
    if (conformance == PDF_UA) {
      InputStream inputStream = pdfDoc.getDocumentCatalog().getMetadata().exportXMPMetadata();
      VeraPDFMeta verMeta = VeraPDFMeta.parse(fixRdfXml(inputStream));

      VeraPDFXMPNode item = verMeta.getProperty("http://www.aiim.org/pdfua/ns/id/", "part");
      assertNotNull("expecting PDFUA conformity", item);

    } else {
      PdfRendererBuilder.PdfAConformance level = PdfRendererBuilder.PdfAConformance.valueOf(conformance.name());

      XMPMetadata xmpMetadata = new DomXmpParser().parse(pdfDoc.getDocumentCatalog().getMetadata().exportXMPMetadata());
      assertThat(String.format("should be conformance level %s", conformance), xmpMetadata.getPDFIdentificationSchema().getConformance(), is(equalTo(level.getConformanceValue())));
      assertThat(String.format("should be part %d", level.getPart()), xmpMetadata.getPDFIdentificationSchema().getPart(), is(equalTo(level.getPart())));
    }
  }

  private InputStream fixRdfXml(final InputStream xmpMetaDataInputStream) throws IOException {
    /*
      Without performing this substitution the VeraPDFMeta.parse() call fails with:
        "com.adobe.xmp.XMPException: Nested content not allowed with rdf:resource or property attributes"
      Simply having the openhtmltopdf-svg-support library included in the project causes this issue, without it
      the 'lang' attribute has the 'xml:' namespace prefix and there is no error - no idea why.
     */
    final String xmpMetaData = IOUtils.toString(xmpMetaDataInputStream);
    return IOUtils.toInputStream(xmpMetaData.replaceAll("<rdf:li lang", "<rdf:li xml:lang"));
  }

  private final List<COSString> getCOSStrings(final COSBase base, final Set<COSBase> visited) {
    if (visited.contains(base)) {
      return Collections.emptyList();
    }
    visited.add(base);

    if (base instanceof COSDictionary) {
      return ((COSDictionary) base).getValues().stream()
          .flatMap(value -> getCOSStrings(value, visited).stream())
          .collect(Collectors.toList());

    } else if (base instanceof COSObject) {
      return getCOSStrings(((COSObject) base).getObject(), visited);

    } else if (base instanceof COSString) {
      return Arrays.asList((COSString) base);
    }

    return Collections.emptyList();
  }

}
