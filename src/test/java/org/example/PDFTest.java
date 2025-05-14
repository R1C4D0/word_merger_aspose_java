package org.example;


import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;

public class PDFTest {
    public static void main(String[] args) {
        // 定义输入 PDF 文件路径
        String inputPdfPath = "src/test/java/org/example/07_易焯平_120L020427_基于联合优化的单帧编码显微成像技术.docx.pdf"; // 请将 "input.pdf" 替换为你的 PDF 文件路径

        // 定义输出 DOCX 文件路径
        String outputDocxPath = "src/test/java/org/example/07_易焯平_120L020427_基于联合优化的单帧编码显微成像技术" +
                ".docx.pdf.docx"; // 请将 "output.docx" 替换为你希望保存的 DOCX 文件路径

        try {
            // 1. 加载 PDF 文档
            // 创建一个 Document 对象，并加载输入的 PDF 文件。
            Document pdfDocument = new Document(inputPdfPath);
            System.out.println("PDF 文件加载成功：" + inputPdfPath);

            // 2. 将 PDF 保存为 DOCX 格式
            // 调用 Document 对象的 save 方法，并指定输出文件路径和 SaveFormat.DOCX。
            // 这会将 PDF 文档的内容转换为 DOCX 格式并保存到指定路径。
            pdfDocument.save(outputDocxPath, SaveFormat.DocX);
            System.out.println("PDF 文件成功转换为 DOCX：" + outputDocxPath);

        } catch (Exception e) {
            // 捕获并打印在转换过程中可能发生的任何异常。
            System.err.println("转换过程中发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

}
