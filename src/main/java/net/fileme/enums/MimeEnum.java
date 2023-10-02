package net.fileme.enums;

import lombok.Getter;

@Getter
public enum MimeEnum {

    // image
    JPG(true)
    ,GIF(true)
    ,PNG(true)

    // archive
    ,ZIP(false)

    // programming
    ,JSON(false)
    ,XML(false)
    ,HTML(false)
    ,JS(false)
    ,CSS(false)
    ,SQL(false)
    ,LOG(false)

    // common type
    ,PDF(false)
    ,TXT(false)
    ,CSV(false)

    // Microsoft Office
    ,DOC(false)
    ,XLS(false)
    ,DOCX(false)
    ,XLSX(false)
    ,PPT(false)
    ,PPTX(false);

    public final boolean allowPreview;
    MimeEnum(boolean allowPreview){
        this.allowPreview = allowPreview;
    }

}
