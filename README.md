# html-to-pdf

Library used to construct a PDF document to various conformance levels using the [openhtmltopdf](https://github.com/danfickle/openhtmltopdf) library.
This project is maintained by the DWP UC Manchester Team.

## build

Standard maven build.
* to package the `jar` file `mvn clean package`

# Usage notes

For the incoming html there are 2 things to consider.  

* The pdf generator requires **XHTML** which requires careful closing of tags (https://www.w3schools.com/html/html_xhtml.asp)
* In order to satisfy the font requirements of PDFA_1_A document all elements need to reference the font that will be embedded.  This is best achieved by adding a `<STYLE>` element to the `<HEAD>` of the html and to apply it for all items (eg body).  The important point is to make sure that all fonts are explicitly specified in the html document.
* If using images it is best to encode the images directly into the html.  eg `<img src="data:image/png;base64,<the-base64-encoded-string-of-the-image>"/>`
* If using images `image-rendering` should be set pixelated or the following error will occur when trying to make any conformance level above NONE :-
    * https://github.com/veraPDF/veraPDF-validation-profiles/wiki/PDFA-Part-1-rules#rule-624-3
    * *"If an Image dictionary contains the Interpolate key, its value shall be false"*

eg.

```html
<html>
    <head>
        <style>
            pre, code, var {
                font-family: 'courier', serif;
            }
            body {
                font-family: 'arial', serif;
            }
            img {
                image-rendering: pixelated;
            }
        </style>
    </head>
    <body>
        <h1>hello world</h1>
        <img
            width="250px" height="250px"
            src="data:image/png;base64,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"
            alt="base64 encoded embedded image"
        />
    </body>
</html>
```

## Common faults

* _fonts not embedded correctly_ :: will result in an error reporting `Index: 0, Size: 0` or `Index 0 out-of-bounds for length 0` which, whilst not a very clear, is because the required font is not present in the embedded list array.  All html tags should have an attached font (both normal and monospaced)
* _links not fully qualified_ :: any references to css or images that have relative paths will fail.  A full, resolvable URL is required.
* _closing tags_ :: XHTML requires all tags to be terminated, this is easily missed.

# Examples
```
byte[] colourProfile = FileUtils.readFileToByteArray(new File("src/test/resources/colours/sRGB.icm"));
String htmlFile = FileUtils.readFileToString(new File("src/test/resources/accessible-test.html"));
Map<String, byte[]> fontMap = new HashMap<>();
fontMap.put("courier", FileUtils.readFileToByteArray(new File("src/test/resources/fonts/courier.ttf")));
fontMap.put("arial", FileUtils.readFileToByteArray(new File("src/test/resources/fonts/arial.ttf")));

HtmlToPdfFactory
    .create() 
    .createPdfDocument(html, colourProfile, fontMap, PdfExtendedConstants.PDF_UA_CONFORMANCE);
```

# Contributing
For more information on how to contribute to this project see: [Contributing](CONTRIBUTING.md)

# Credits
Big thanks to the [ms-html-to-pdfa](https://github.com/dwp/ms-html-to-pdfa.git) project which contained the code used to start this library.