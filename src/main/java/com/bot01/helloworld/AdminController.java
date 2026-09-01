package com.bot01.helloworld;

import com.bot01.helloworld.model.Enquiry;
import com.bot01.helloworld.repository.EnquiryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    private final EnquiryRepository enquiryRepository;

    public AdminController(EnquiryRepository enquiryRepository) {
        this.enquiryRepository = enquiryRepository;
    }

    // ==============================
    // LOGIN PAGE
    // ==============================

    @GetMapping("/admin/login")
    public String loginPage() {
        return "login";
    }

    // ==============================
    // ADMIN DASHBOARD
    // ==============================

    @GetMapping("/admin")
    public String dashboard(
            HttpSession session,
            Model model) {

        model.addAttribute(
                "enquiries",
                enquiryRepository.findAll()
        );

        model.addAttribute(
                "totalEnquiries",
                enquiryRepository.count()
        );

        long newCount = enquiryRepository
                .findAll()
                .stream()
                .filter(e -> "NEW".equals(e.getStatus()))
                .count();

        long inProgressCount = enquiryRepository
                .findAll()
                .stream()
                .filter(e -> "IN PROGRESS".equals(e.getStatus()))
                .count();

        long completedCount = enquiryRepository
                .findAll()
                .stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()))
                .count();

        model.addAttribute(
                "newCount",
                newCount
        );

        model.addAttribute(
                "inProgressCount",
                inProgressCount
        );

        model.addAttribute(
                "completedCount",
                completedCount
        );

        return "admin";
    }

    // ==============================
    // VIEW ENQUIRY DETAILS
    // ==============================

    @GetMapping("/admin/enquiry/{id}")
    public String viewEnquiry(
            @PathVariable Long id,
            Model model) {

        Enquiry enquiry =
                enquiryRepository.findById(id).orElse(null);

        if (enquiry == null) {
            return "redirect:/admin";
        }

        model.addAttribute(
                "enquiry",
                enquiry
        );

        return "enquiry-details";
    }

    // ==============================
    // UPDATE STATUS
    // ==============================

    @PostMapping("/admin/status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Enquiry enquiry =
                enquiryRepository.findById(id).orElse(null);

        if (enquiry != null) {
            enquiry.setStatus(status);
            enquiryRepository.save(enquiry);
        }

        return "redirect:/admin";
    }

    // ==============================
    // DELETE ENQUIRY
    // ==============================

    @PostMapping("/admin/enquiry/delete/{id}")
    public String deleteEnquiry(
            @PathVariable Long id) {

        if (enquiryRepository.existsById(id)) {
            enquiryRepository.deleteById(id);
        }

        return "redirect:/admin";
    }

    // ==============================
    // LOGOUT
    // ==============================

    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/admin/login";
    }
}
