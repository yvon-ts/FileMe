package net.fileme.domain.dto;

import lombok.Data;

@Data
public class EmailDto {
    private String receiver;
    private String subject;
    private String text;
}
