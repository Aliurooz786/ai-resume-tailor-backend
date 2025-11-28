package com.urooz.resumetailor.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeData {
    private String fullName;
    private String contactInfo;
    private String linkedin;
    private String github;
    private String summary;

    private List<ExperienceEntry> experience;
    private List<ProjectEntry> projects;

    private List<SkillCategory> skills;

    private List<EducationEntry> education;

    @Data
    public static class SkillCategory {
        private String category;
        private String values;
    }

    @Data
    public static class ExperienceEntry {
        private String company;
        private String role;
        private String duration;
        private List<String> bullets;
    }

    @Data
    public static class ProjectEntry {
        private String name;
        private String duration;
        private String techStack;
        private List<String> bullets;
    }

    @Data
    public static class EducationEntry {
        private String institution;
        private String degree;
        private String duration;
        private String gpa;
    }
}