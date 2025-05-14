package org.example.core;

import com.aspose.words.*;
import org.example.pojo.ChapterConfig;
import org.example.pojo.MergeTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Import your JSON template POJOs and other necessary classes

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class DocumentMerger {
    private static final Logger logger = LoggerFactory.getLogger(DocumentMerger.class);
    private MergeTemplate config; // Your JSON template POJO

    public DocumentMerger(MergeTemplate templateConfig) {
        this.config = templateConfig;
    }

    public String execute(String outputDir) throws Exception {
        Document outputDoc = new Document(); // Create the destination document
        // The DocumentBuilder is not needed for appendDocument, 
        // but might be useful for other operations like inserting placeholders or breaks.
        DocumentBuilder builder = new DocumentBuilder(outputDoc);
        outputDoc.removeAllChildren(); // Start with a clean document
        // ----> 新增代码：确保 outputDoc 至少有一个 Section <----
        if (outputDoc.getSections().getCount() == 0) {
            Section newSection = new Section(outputDoc);
            outputDoc.appendChild(newSection);
            // 通常，新创建的 Section 会自动包含一个 Body。
            // 如果需要，还可以检查并添加 Body:
            // if (newSection.getBody() == null) {
            //    Body body = new Body(outputDoc);
            //    newSection.appendChild(body);
            // }
            logger.debug("Ensured output document has an initial section after removeAllChildren.");
        }
        // ----> 新增代码结束 <----

        // Sort chapters based on 'order_in_merged_document'
        List<ChapterConfig> chapters = config.getChaptersToMerge();
        Collections.sort(chapters, Comparator.comparingInt(ChapterConfig::getOrderInMergedDocument));

        for (ChapterConfig chapter : chapters) {
            String sourceDocumentPath = chapter.getSourceDocumentPath();
            String fileExtension = sourceDocumentPath.substring(sourceDocumentPath.lastIndexOf(".")).toLowerCase();

            // The Python version currently only supports .docx
            // As per requirements, .doc and .pdf are also desired inputs.
            // Aspose.Words can load various formats. For .doc, direct loading is fine.
            // For .pdf, Aspose.Words can load it, but fidelity might vary.
            // The requirements mention using LibreOffice for .doc to .docx conversion on Linux.
            // If you stick to that, you'll need to implement that conversion step externally or via Java's ProcessBuilder.
            // However, Aspose.Words can handle .doc directly.

            if (!".docx".equals(fileExtension) && !".doc".equals(fileExtension)) {
                // Add handling for PDF if needed, or skip as per Python's current logic
                logger.warn("Skipping non-Word file (or unhandled format): {}", sourceDocumentPath);
                continue;
            }

            try {
                processChapter(chapter, outputDoc, builder); // Pass builder if needed for placeholders/breaks
            } catch (Exception e) {
                String onChapterNotFound = chapter.getOnChapterNotFound() != null ?
                        chapter.getOnChapterNotFound() :
                        config.getDefaultOnChapterNotFound() != null ?
                                config.getDefaultOnChapterNotFound() : "error";

                String msg = String.format("Failed to process chapter %s from %s: %s",
                        chapter.getChapterIdentifierValue(), sourceDocumentPath, e.getMessage());

                if ("skip".equalsIgnoreCase(onChapterNotFound)) {
                    logger.warn(msg, e);
                    continue;
                } else if ("insert_placeholder".equalsIgnoreCase(onChapterNotFound)) {
                    logger.warn(msg + ". Inserting placeholder.", e);
                    // Use DocumentBuilder to insert placeholder text
                    builder.moveToDocumentEnd(); // Ensure builder is at the correct position
                    builder.writeln(String.format("[PLACEHOLDER: Chapter %s from %s not found or failed to process]",
                            chapter.getChapterIdentifierValue(), sourceDocumentPath));
                    continue;
                } else { // "error"
                    logger.error(msg, e);
                    throw e; // Re-throw to stop processing
                }
            }
        }

        // Set document properties
        if (config.getOutputDocumentProperties() != null) {
            if (config.getOutputDocumentProperties().getTitle() != null) {
                outputDoc.getBuiltInDocumentProperties().setTitle(config.getOutputDocumentProperties().getTitle());
            }
            if (config.getOutputDocumentProperties().getAuthor() != null) {
                outputDoc.getBuiltInDocumentProperties().setAuthor(config.getOutputDocumentProperties().getAuthor());
            }
        }

        // Determine output filename and path
        String prefix = config.getOutputFilenamePrefix() != null ? config.getOutputFilenamePrefix() : "MergedDocument";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputFilename = String.format("%s_%s.docx", prefix, timestamp); // Default to docx

        String effectiveOutputDir = config.getOutputDirectory() != null ? config.getOutputDirectory() : outputDir;
        new File(effectiveOutputDir).mkdirs(); // Ensure directory exists
        String outputPath = Paths.get(effectiveOutputDir, outputFilename).toString();

        outputDoc.save(outputPath);
        logger.info("Merged document saved: {}", outputPath);
        return outputPath;
    }

    private void processChapter(ChapterConfig chapter, Document outputDoc, DocumentBuilder builder) throws Exception {
        String srcPath = chapter.getSourceDocumentPath();
        if (srcPath == null || srcPath.isEmpty()) {
            throw new IllegalArgumentException("Source document path is null or empty for chapter: " + chapter.getChapterIdentifierValue());
        }        // Load options can be used for password-protected documents

        // Load options can be used for password-protected documents
        LoadOptions loadOptions = new LoadOptions();
        if (chapter.getSourceDocumentPassword() != null && !chapter.getSourceDocumentPassword().isEmpty()) {
            loadOptions.setPassword(chapter.getSourceDocumentPassword());
        }

        if (chapter.getSourceDocumentPassword() != null && !chapter.getSourceDocumentPassword().isEmpty()) {
            loadOptions.setPassword(chapter.getSourceDocumentPassword());
        }
        Document srcDoc = new Document(srcPath, loadOptions);

        // ---- Chapter Extraction Logic ----
        // This is the most complex part and needs to replicate the Python logic 
        // for _find_chapter_by_name and _find_chapter_by_number.
        // Aspose.Words for Java provides similar APIs to traverse the document node tree.

        Node startNode = null;
        Node endNode = null; // Node *before* which to stop copying

        if ("name".equalsIgnoreCase(chapter.getChapterIdentifierType())) {
            // Implement findChapterByName logic
            // Iterate through paragraphs, check style (e.g., "Heading 1", "Heading 2") and text content.
            // Paragraphs can be accessed via: srcDoc.getChildNodes(NodeType.PARAGRAPH, true)
            NodeCollection<Paragraph> paragraphs = srcDoc.getChildNodes(NodeType.PARAGRAPH, true);
            int headingLevel = -1; // To store the level of the found heading

            for (int i = 0; i < paragraphs.getCount(); i++) {
                Paragraph para = (Paragraph) paragraphs.get(i);
                String paraText = para.getText().trim();
                String styleName = para.getParagraphFormat().getStyle().getName();

                boolean nameMatch;
                if (chapter.isChapterIdentifierCaseSensitive()) {
                    nameMatch = paraText.equals(chapter.getChapterIdentifierValue());
                } else {
                    nameMatch = paraText.equalsIgnoreCase(chapter.getChapterIdentifierValue());
                }

                if (styleName.startsWith("Heading") && nameMatch) {
                    startNode = para;
                    headingLevel = getHeadingLevelFromStyle(styleName); // Helper method needed

                    // Determine endNode: next heading of same or higher level
                    for (int j = i + 1; j < paragraphs.getCount(); j++) {
                        Paragraph nextPara = (Paragraph) paragraphs.get(j);
                        String nextStyleName = nextPara.getParagraphFormat().getStyle().getName();
                        if (nextStyleName.startsWith("Heading")) {
                            int nextHeadingLevel = getHeadingLevelFromStyle(nextStyleName);
                            if (nextHeadingLevel <= headingLevel) {
                                endNode = nextPara;
                                break;
                            }
                        }
                    }
                    break; // Found the starting chapter heading
                }
            }

        } else if ("number".equalsIgnoreCase(chapter.getChapterIdentifierType())) {
            // Implement findChapterByNumber logic
            // Count paragraphs with "Heading 1" style (or configurable heading style as per requirements doc)
            // The Python code defaults to "Heading 1"
            NodeCollection<Paragraph> paragraphs = srcDoc.getChildNodes(NodeType.PARAGRAPH, true);
            int headingCount = 0;
            int targetNumber = Integer.parseInt(chapter.getChapterIdentifierValue());
            String targetHeadingStyle = "Heading 1"; // Make this configurable later

            for (int i = 0; i < paragraphs.getCount(); i++) {
                Paragraph para = (Paragraph) paragraphs.get(i);
                if (para.getParagraphFormat().getStyle().getName().equals(targetHeadingStyle)) {
                    headingCount++;
                    if (headingCount == targetNumber) {
                        startNode = para;
                        // Determine endNode: next "Heading 1"
                        for (int j = i + 1; j < paragraphs.getCount(); j++) {
                            Paragraph nextPara = (Paragraph) paragraphs.get(j);
                            if (nextPara.getParagraphFormat().getStyle().getName().equals(targetHeadingStyle)) {
                                endNode = nextPara;
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported chapter_identifier_type: " + chapter.getChapterIdentifierType());
        }

        if (startNode == null) {
            throw new Exception("Chapter not found: " + chapter.getChapterIdentifierValue() + " in " + srcPath);
        }

        // --- Content Copying ---
        // The Python code iterates node by node and imports them.
        // A more robust way for Aspose.Words might be to use Section-level import if possible,
        // or carefully import nodes. AppendDocument is good for whole documents.
        // For partial content, you need to iterate and import nodes.

        Node currentNode = startNode;
        while (currentNode != null && currentNode != endNode) {
            Node importedNode = outputDoc.importNode(currentNode, true, ImportFormatMode.KEEP_SOURCE_FORMATTING);

            // Adjust heading level if target_heading_level is specified
            // The Python code has a TODO for sub-chapter heading levels.
            if (chapter.getTargetHeadingLevel() != null && chapter.getTargetHeadingLevel() > 0 &&
                    importedNode.getNodeType() == NodeType.PARAGRAPH) {
                Paragraph importedPara = (Paragraph) importedNode;
                if (importedPara.getParagraphFormat().getStyle().getName().startsWith("Heading")) {
                    // This logic needs to be more sophisticated to handle include_subsections correctly.
                    // If include_subsections is true, sub-headings (e.g., Heading 2, Heading 3) 
                    // under a main Heading 1 should be adjusted relative to the new target_heading_level.
                    // For example, if main chapter is H1, becomes target H2, then its H2 subsections -> target H3.
                    // The current Python code applies target_heading_level to *all* headings in the section.
                    // This simplification is reflected here.
                    Style newHeadingStyle = outputDoc.getStyles().getByStyleIdentifier(StyleIdentifier.HEADING_1 + chapter.getTargetHeadingLevel() -1);
                    // Ensure the style exists or create it if necessary based on Heading X.
                    if (newHeadingStyle == null) { // Fallback if specific level doesn't exist by default
                        newHeadingStyle = outputDoc.getStyles().get("Heading " + chapter.getTargetHeadingLevel());
                        // If still null, you might need to create/clone a base heading style.
                    }
                    if (newHeadingStyle != null) {
                        importedPara.getParagraphFormat().setStyle(newHeadingStyle);
                    } else {
                        logger.warn("Could not find or apply target heading style: Heading {}", chapter.getTargetHeadingLevel());
                    }
                }
            }
            outputDoc.getLastSection().getBody().appendChild(importedNode);
            currentNode = currentNode.getNextSibling(); // Move to the next node at the same level
        }

        // Insert page break if configured
        if (chapter.isInsertPageBreakAfter()) {
            // Use the DocumentBuilder passed to the method, ensuring it's at the end.
            builder.moveToDocumentEnd();
            builder.insertBreak(BreakType.PAGE_BREAK);
        }
        // If not the last chapter, consider adding a section break to restart headers/footers,
        // or ensure content flows as desired. Aspose.Words appends content into the last section by default.
        // outputDoc.appendDocument(srcDoc, ImportFormatMode.KEEP_SOURCE_FORMATTING); // This appends the WHOLE srcDoc. Not what we want for chapters.
    }

    // Helper method to parse heading level from style name (e.g., "Heading 1" -> 1)
    private int getHeadingLevelFromStyle(String styleName) {
        if (styleName != null && styleName.startsWith("Heading ")) {
            try {
                return Integer.parseInt(styleName.substring("Heading ".length()));
            } catch (NumberFormatException e) {
                // Fallback for non-standard or unparseable heading names
                return 9; // Default to a high number, considered a low-level heading
            }
        }
        return 9; // Not a heading style or not recognized
    }
}

