package lab.localcloudnative.lcnpages.dto;

import java.util.List;

public record PageRequest(
        String title,
        String contentMarkdown,
        String parentId,
        List<String> tags
) {
}
