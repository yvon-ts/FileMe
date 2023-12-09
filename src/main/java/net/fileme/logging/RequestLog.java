package net.fileme.logging;

import lombok.Data;

@Data
public class RequestLog {
    private Long userId = 0L;
    private String route;
    private Object param;
//    private Object ret;
//    private String description;
//    private LocalDateTime createTime;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("USER: ")
                .append(userId == 0 ? "anonymous" : userId)
                .append(" | REQUEST: ")
                .append(route)
                .append(" | PARAM: ")
                .append(param);

        return builder.toString();
    }
}
