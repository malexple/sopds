package com.sopds.catalog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class WebController {

    private void addDefaultAttributes(Model model) {
        model.addAttribute("appTitle", "SOPDS Catalog");

        // Ensure breadcrumbs is never null
        if (!model.containsAttribute("breadcrumbs")) {
            model.addAttribute("breadcrumbs", new ArrayList<String>());
        }

        // Ensure breadcrumbs_cat is never null
        if (!model.containsAttribute("breadcrumbs_cat")) {
            model.addAttribute("breadcrumbs_cat", new ArrayList<Map<String, Object>>());
        }

        // Add default stats
        if (!model.containsAttribute("stats")) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("allbooks", 0);
            stats.put("allauthors", 0);
            stats.put("allgenres", 0);
            stats.put("allseries", 0);
            stats.put("lastscan_date", "Never");
            model.addAttribute("stats", stats);
        }
    }

    @GetMapping({"", "/", "/main"})
    public String mainPage(Model model) {
        addDefaultAttributes(model);
        model.addAttribute("content", "sopds_hello");
        model.addAttribute("current", "main");
        return "sopds_main";
    }

    @GetMapping("/catalog")
    public String catalog(
            @RequestParam(required = false) Long cat,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        addDefaultAttributes(model);
        model.addAttribute("appTitle", "SOPDS Catalog - Catalogs");
        model.addAttribute("content", "sopds_catalogs");
        model.addAttribute("current", "catalog");
        model.addAttribute("cat_id", cat);
        model.addAttribute("page", page);

        return "sopds_main";
    }

    @GetMapping("/book")
    public String books(
            @RequestParam(defaultValue = "0") int lang,
            @RequestParam(required = false) String chars,
            Model model) {

        addDefaultAttributes(model);
        model.addAttribute("appTitle", "SOPDS Catalog - Books");
        model.addAttribute("content", "sopds_selectbook");
        model.addAttribute("current", "book");
        model.addAttribute("lang_code", lang);
        model.addAttribute("chars", chars);

        return "sopds_main";
    }

    @GetMapping("/searchbooks")
    public String searchBooks(
            @RequestParam String searchtype,
            @RequestParam String searchterms,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        addDefaultAttributes(model);
        model.addAttribute("appTitle", "SOPDS Catalog - Search Books");
        model.addAttribute("content", "sopds_books");
        model.addAttribute("current", "book");
        model.addAttribute("searchtype", searchtype);
        model.addAttribute("searchterms", searchterms);
        model.addAttribute("page", page);

        return "sopds_main";
    }

    @GetMapping("/error")
    public String errorPage(@RequestParam(required = false) String errormsg, Model model) {
        addDefaultAttributes(model);
        model.addAttribute("appTitle", "SOPDS Catalog - Error");
        model.addAttribute("content", "sopds_error");
        model.addAttribute("errormsg", errormsg);

        return "sopds_main";
    }
}