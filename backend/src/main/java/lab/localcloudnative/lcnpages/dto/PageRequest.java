package lab.localcloudnative.lcnpages.dto;

import org.bson.types.ObjectId;

import java.util.List;

public record PageRequest(
        String title,
        String contentMarkdown,
        ObjectId parentId,
        List<String> tags
) {
}
