package lab.localcloudnative.lcnpages.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pages")
public class Page {
    @Id
    private String id;

    @Indexed
    private String ownerId;       // sub claim del JWT (Keycloak user id)

    @Indexed
    private String parentId;      // null = root page

    private String title;
    private String slug;          // URL-friendly version of title
    private String contentMarkdown;
    private List<String> tags = new ArrayList<>();
    private List<String> linkedPages = new ArrayList<>();  // wikilink β
    private List<String> backlinks = new ArrayList<>();    // wikilink β
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getContentMarkdown() { return contentMarkdown; }
    public void setContentMarkdown(String contentMarkdown) { this.contentMarkdown = contentMarkdown; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getLinkedPages() { return linkedPages; }
    public void setLinkedPages(List<String> linkedPages) { this.linkedPages = linkedPages; }

    public List<String> getBacklinks() { return backlinks; }
    public void setBacklinks(List<String> backlinks) { this.backlinks = backlinks; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
