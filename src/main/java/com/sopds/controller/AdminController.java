package com.sopds.controller;

import com.sopds.config.SopdsProperties;
import com.sopds.scanner.LibraryScanner;
import com.sopds.scanner.ScanResult;
import com.sopds.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final ConfigService configService;
    private final SopdsProperties properties;
    private final LibraryScanner libraryScanner;

    @GetMapping("/admin/settings")
    public String settings(Model model) {
        String rootLib = configService.getString("SOPDS_ROOT_LIB", properties.getRootLib());
        model.addAttribute("rootLib", rootLib);
        model.addAttribute("breadcrumbs", List.of("Настройки"));
        model.addAttribute("current", "admin");
        return "sopds_admin_settings";
    }

    @PostMapping("/admin/settings")
    public String saveSettings(@RequestParam("rootLib") String rootLib,
                               RedirectAttributes redirectAttributes) {
        configService.setString("SOPDS_ROOT_LIB", rootLib != null ? rootLib.trim() : "");
        redirectAttributes.addFlashAttribute("successMessage", "Путь к библиотеке сохранён");
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/scan")
    public String scan(RedirectAttributes redirectAttributes) {
        String rootPath = configService.getString("SOPDS_ROOT_LIB", properties.getRootLib());
        try {
            ScanResult result = libraryScanner.scan(rootPath);
            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("scanResult", result);
            } else {
                redirectAttributes.addFlashAttribute("scanError", result.getError());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("scanError", e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}