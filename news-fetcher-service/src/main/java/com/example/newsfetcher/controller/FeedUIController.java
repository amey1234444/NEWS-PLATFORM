package com.example.newsfetcher.controller;

import com.example.newsfetcher.entity.Feed;
import com.example.newsfetcher.repository.FeedRepository;
import com.example.newsfetcher.service.FeedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/feeds")
public class FeedUIController {

    private final FeedRepository feedRepository;
    private final FeedService feedService;

    public FeedUIController(FeedRepository feedRepository, FeedService feedService) {
        this.feedRepository = feedRepository;
        this.feedService = feedService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("feeds", feedRepository.findAll());
        return "feeds/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("feed", new Feed());
        return "feeds/form";
    }

    @PostMapping
    public String save(Feed feed, RedirectAttributes attrs) {
        if (feed.getNextAttemptAt() == null) feed.setNextAttemptAt(LocalDateTime.now());
        feedRepository.save(feed);
        attrs.addFlashAttribute("message", "Saved");
        return "redirect:/admin/feeds";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        feedRepository.findById(id).ifPresent(f -> model.addAttribute("feed", f));
        return "feeds/form";
    }

    @PostMapping("/{id}/run")
    public String runNow(@PathVariable Long id, RedirectAttributes attrs) {
        feedRepository.findById(id).ifPresent(feed -> {
            int published = feedService.fetchSingle(feed);
            attrs.addFlashAttribute("message", "published=" + published);
        });
        return "redirect:/admin/feeds";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        feedRepository.deleteById(id);
        attrs.addFlashAttribute("message", "deleted");
        return "redirect:/admin/feeds";
    }
}
