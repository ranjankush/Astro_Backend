package com.example.AiAstrologer.response;

import org.springframework.stereotype.Component;

@Component
    public class RashiResponse {

        private String name;
        private String rashi;
        private String description;

        // Default constructor
        public RashiResponse() {
        }

        // Parameterized constructor
        public RashiResponse(String name, String rashi, String description) {
            this.name = name;
            this.rashi = rashi;
            this.description = description;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRashi() {
            return rashi;
        }

        public void setRashi(String rashi) {
            this.rashi = rashi;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        // Optional: toString() for debugging
        @Override
        public String toString() {
            return "RashiResponse{" +
                    "name='" + name + '\'' +
                    ", rashi='" + rashi + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

