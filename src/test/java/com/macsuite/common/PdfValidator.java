package com.macsuite.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.TimeoutError;

public class PdfValidator {
    private static final Logger logger = LoggerFactory.getLogger(PdfValidator.class);

    /**
     * Downloads a PDF using Playwright and validates that expected text exists.
     *
     * @param page            Playwright Page instance
     * @param downloadLocator locator that triggers PDF download
     * @param expectedText    text to validate inside PDF
     */

    public String downloadPdfAndExtractText(Page page, String downloadLocator) {
        page.locator(downloadLocator).first().click();
        Locator downloadButton = page.locator("xpath=//button[@title='Download']").first();
        downloadButton.waitFor(new Locator.WaitForOptions().setTimeout(15000));

        try {
            byte[] pdfBytes = fetchPdfBytes(page, downloadButton);

            // Parse PDF using PDFBox
            try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read or parse PDF", e);
        }

    }

    private byte[] fetchPdfBytes(Page page, Locator downloadButton) throws IOException {
        try {
            Download download = page.waitForDownload(() -> downloadButton.click());
            Path pdfPath = download.path();
            return Files.readAllBytes(pdfPath);
        } catch (TimeoutError e) {
            logger.warn("Download event did not fire. Falling back to PDF response capture.");
            Response pdfResponse = page.waitForResponse(
                    response -> {
                        String contentType = response.headerValue("content-type");
                        String url = response.url();
                        boolean isPdfContent = contentType != null
                                && contentType.toLowerCase().contains("application/pdf");
                        boolean isPdfDownloadUrl = url != null && url.contains("/document/download");
                        return isPdfContent || isPdfDownloadUrl;
                    },
                    () -> downloadButton.click());

            if (pdfResponse == null) {
                throw new RuntimeException("PDF response was not captured after clicking Download");
            }

            return pdfResponse.body();
        }
    }

    public void validatePdfContent(String pdfContent, String expectedText) {
        if (!pdfContent.contains(expectedText)) {
            throw new AssertionError("Expected text not found in PDF. Expected: " + expectedText);
        } else {
            System.out.println("Validation successful: Expected text \"" + expectedText + "\" found in PDF.");
        }

    }

}
