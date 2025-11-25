package com.iamvickyav.dockerdemo;


import org.springframework.stereotype.Service;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;

@Service
public class RtfToHtmlService {

    public String convertRtfToHtml(InputStream rtfInputStream) {
        try {
            RTFEditorKit rtfEditorKit = new RTFEditorKit();
            Document doc = rtfEditorKit.createDefaultDocument();
            System.out.println("Loool");

            try (Reader reader = new InputStreamReader(rtfInputStream)) {
                rtfEditorKit.read(reader, doc, 0);
            }

            HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
            StringWriter writer = new StringWriter();
            htmlEditorKit.write(writer, doc, 0, doc.getLength());

            return writer.toString();
        } catch (IOException | BadLocationException e) {
            throw new RuntimeException("Failed to convert RTF to HTML", e);
        }
    }
}
