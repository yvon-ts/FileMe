package net.fileme.domain;

import lombok.Data;

@Data
public class EmailDetails {
    private String receiver;
    private String subject;
    private String text;
}
