package com.iamvickyav.dockerdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@Controller
public class MyController {

    private final RtfToHtmlService rtfToHtmlService;

    private final LibreOfficeRtfToHtmlService converter;

    @Autowired
    public MyController(RtfToHtmlService rtfToHtmlService, LibreOfficeRtfToHtmlService converter) {
        this.rtfToHtmlService = rtfToHtmlService;
        this.converter = converter;
    }

    @RequestMapping(value = "/index")
    public String hello(Model model, @RequestParam(value="name") String name) {
        model.addAttribute("name", name);
        return "index";
    }

    @GetMapping("/{name}")
    public String viewRtf(@PathVariable("name") String name, Model model) throws IOException {
        ClassPathResource resource = new ClassPathResource("rtf/" + name + ".rtf");
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RTF file not found: " + name);
        }

        try (InputStream in = resource.getInputStream()) {
            String htmlBody = rtfToHtmlService.convertRtfToHtml(in);
            model.addAttribute("name", name);
            model.addAttribute("rtfHtml", htmlBody);
        }

        return "rtfView";
    }

    @GetMapping("lo-rtf/{name}")
    public String loViewRtf(@PathVariable("name") String name, Model model) throws IOException {
        ClassPathResource resource = new ClassPathResource("rtf/" + name + ".rtf");
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RTF file not found: " + name);
        }

        String htmlBody;
        try (InputStream in = resource.getInputStream()) {
            htmlBody = converter.convertRtfToHtml(in);
        }

        model.addAttribute("name", name);
        model.addAttribute("rtfHtml", htmlBody);

        // Renders /WEB-INF/jsp/rtfView.jsp
        return "rtfView";
    }
}


