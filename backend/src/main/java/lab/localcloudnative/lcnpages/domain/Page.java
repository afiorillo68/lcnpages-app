package lab.localcloudnative.lcnpages.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pages")
public class Page {
    @Id
    private ObjectId id;

    @Indexed
    private String ownerId;       // sub claim del JWT (Keycloak user id)

    @Indexed
    private ObjectId parentId;    // null = root page

    private String title;
    private String slug;          // URL-friendly version of title
    private String contentMarkdown;
    private List<String> tags = new ArrayList<>();
    private List<ObjectId> linkedPages = new ArrayList<>();  // wikilink β
    private List<ObjectId> backlinks = new ArrayList<>();    // wikilink β
    private Instant createdAt;
    private Instant updatedAt;

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public ObjectId getParentId() { return parentId; }
    public void setParentId(ObjectId parentId) { this.parentId = parentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getContentMarkdown() { return contentMarkdown; }
    public void setContentMarkdown(String contentMarkdown) { this.contentMarkdown = contentMarkdown; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<ObjectId> getLinkedPages() { return linkedPages; }
    public void setLinkedPages(List<ObjectId> linkedPages) { this.linkedPages = linkedPages; }

    public List<ObjectId> getBacklinks() { return backlinks; }
    public void setBacklinks(List<ObjectId> backlinks) { this.backlinks = backlinks; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
