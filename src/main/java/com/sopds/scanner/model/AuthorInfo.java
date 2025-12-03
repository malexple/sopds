package com.sopds.scanner.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorInfo {

    private String firstName;
    private String middleName;
    private String lastName;

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastName != null && !lastName.isBlank()) {
            sb.append(lastName.trim());
        }
        if (firstName != null && !firstName.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(firstName.trim());
        }
        if (middleName != null && !middleName.isBlank()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(middleName.trim());
        }
        return sb.length() > 0 ? sb.toString() : "Unknown Author";
    }
}
