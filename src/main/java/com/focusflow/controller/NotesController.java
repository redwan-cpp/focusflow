package com.focusflow.controller;

import com.focusflow.model.Note;
import com.focusflow.model.NoteCategory;
import com.focusflow.model.User;
import com.focusflow.model.Project;
import com.focusflow.repository.NoteRepository;
import com.focusflow.repository.NoteCategoryRepository;
import com.focusflow.repository.UserRepository;
import com.focusflow.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notes")
public class NotesController {

    @Autowired
    private NoteRepository noteRepository;

    // Category UI removed; keep repository unused for now to avoid schema migrations
    @Autowired(required = false)
    private NoteCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping
    public String notesPage(Model model, Principal principal,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) Long projectId,
                           @RequestParam(required = false) String tag,
                           @RequestParam(required = false) String sort) {
        
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<Note> notes;
        List<Project> projects = projectRepository.findAllByUser(user);

        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            notes = noteRepository.findByUserAndSearchTerm(user, search.trim());
        } else if (projectId != null) {
            notes = noteRepository.findByUserAndProjectIdOrderByUpdatedAtDesc(user, projectId);
        } else if (tag != null && !tag.trim().isEmpty()) {
            notes = noteRepository.findByUserAndTag(user, tag.trim());
        } else {
            // Default sorting
            if ("pinned".equals(sort)) {
                notes = noteRepository.findByUserAndIsPinnedTrueOrderByUpdatedAtDesc(user);
            } else if ("favorite".equals(sort)) {
                notes = noteRepository.findByUserAndIsFavoriteTrueOrderByUpdatedAtDesc(user);
            } else {
                notes = noteRepository.findByUserOrderByIsPinnedDescUpdatedAtDesc(user);
            }
        }

        // Get all unique tags from user's notes
        Set<String> allTags = notes.stream()
                .flatMap(note -> note.getTags().stream())
                .collect(Collectors.toSet());

        model.addAttribute("notes", notes);
        model.addAttribute("projects", projects);
        model.addAttribute("allTags", allTags);
        model.addAttribute("user", user);
        model.addAttribute("search", search);
        model.addAttribute("selectedProjectId", projectId);
        model.addAttribute("selectedTag", tag);
        model.addAttribute("sort", sort);
        model.addAttribute("newNote", new Note());

        return "notes";
    }

    @PostMapping("/add")
    public String addNote(@ModelAttribute Note note, Principal principal, RedirectAttributes redirectAttributes,
                          @RequestParam(name = "projectId", required = false) Long projectId) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        note.setUser(user);

        // Category handling removed from UI; ensure null
        note.setCategory(null);

        if (projectId != null) {
            projectRepository.findById(projectId).ifPresent(note::setProject);
        }
        
        // Parse tags from comma-separated string
        if (note.getContent() != null && note.getContent().contains("#")) {
            Set<String> tags = Arrays.stream(note.getContent().split("\\s+"))
                    .filter(word -> word.startsWith("#"))
                    .map(tag -> tag.substring(1))
                    .collect(Collectors.toSet());
            note.setTags(tags);
        }

        noteRepository.save(note);
        redirectAttributes.addFlashAttribute("success", "Note created successfully!");
        return "redirect:/notes";
    }

    @PostMapping("/{id}/edit")
    public String editNote(@PathVariable Long id, @ModelAttribute Note note, Principal principal, RedirectAttributes redirectAttributes,
                           @RequestParam(name = "projectId", required = false) Long projectId) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Note> existingNote = noteRepository.findById(id);
        if (existingNote.isPresent() && existingNote.get().getUser().getId().equals(user.getId())) {
            Note noteToUpdate = existingNote.get();
            noteToUpdate.setTitle(note.getTitle());
            noteToUpdate.setContent(note.getContent());
            // Category handling removed from UI; ensure null
            noteToUpdate.setCategory(null);
            noteToUpdate.setColorTag(note.getColorTag());
            noteToUpdate.setFontSize(note.getFontSize());
            noteToUpdate.setFontFamily(note.getFontFamily());

            if (projectId != null) {
                projectRepository.findById(projectId).ifPresent(noteToUpdate::setProject);
            } else {
                noteToUpdate.setProject(null);
            }

            // Parse tags from content
            if (note.getContent() != null && note.getContent().contains("#")) {
                Set<String> tags = Arrays.stream(note.getContent().split("\\s+"))
                        .filter(word -> word.startsWith("#"))
                        .map(tag -> tag.substring(1))
                        .collect(Collectors.toSet());
                noteToUpdate.setTags(tags);
            }

            noteRepository.save(noteToUpdate);
            redirectAttributes.addFlashAttribute("success", "Note updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Note not found or access denied!");
        }

        return "redirect:/notes";
    }

    @PostMapping("/{id}/delete")
    public String deleteNote(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Optional<Note> note = noteRepository.findById(id);
        if (note.isPresent() && note.get().getUser().getId().equals(user.getId())) {
            noteRepository.delete(note.get());
            redirectAttributes.addFlashAttribute("success", "Note deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Note not found or access denied!");
        }

        return "redirect:/notes";
    }

    @PostMapping("/{id}/toggle-pin")
    @ResponseBody
    public String togglePin(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "error";
        }

        Optional<Note> note = noteRepository.findById(id);
        if (note.isPresent() && note.get().getUser().getId().equals(user.getId())) {
            Note noteToUpdate = note.get();
            noteToUpdate.setIsPinned(!noteToUpdate.getIsPinned());
            noteRepository.save(noteToUpdate);
            return noteToUpdate.getIsPinned() ? "pinned" : "unpinned";
        }

        return "error";
    }

    @PostMapping("/{id}/toggle-favorite")
    @ResponseBody
    public String toggleFavorite(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return "error";
        }

        Optional<Note> note = noteRepository.findById(id);
        if (note.isPresent() && note.get().getUser().getId().equals(user.getId())) {
            Note noteToUpdate = note.get();
            noteToUpdate.setIsFavorite(!noteToUpdate.getIsFavorite());
            noteRepository.save(noteToUpdate);
            return noteToUpdate.getIsFavorite() ? "favorited" : "unfavorited";
        }

        return "error";
    }

    // Category management endpoints removed
}
