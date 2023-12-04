package net.fileme.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MimeEnum {
    // image
    JPG(true, "image/jpeg"),
    GIF(true, "image/gif"),
    PNG(true, "image/png"),

    // archive
    ZIP(false, "application/zip"),

    // programming
    JSON(false, "application/json"),
    XML(false, "application/xml"),
    HTML(false, "text/html"),
    JS(false, "application/javascript"),
    CSS(false, "text/css"),
    SQL(false, "application/sql"),
    LOG(false, "text/plain"),

    // common type
    PDF(false, "application/pdf"),
    TXT(false, "text/plain"),
    CSV(false, "text/csv"),

    // Microsoft Office
    DOC(false, "application/msword"),
    XLS(false, "application/vnd.ms-excel"),
    DOCX(false, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLSX(false, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PPT(false, "application/vnd.ms-powerpoint"),
    PPTX(false, "application/vnd.openxmlformats-officedocument.presentationml.presentation");

    public final boolean allowPreview;
    public final String mimeType;

}
