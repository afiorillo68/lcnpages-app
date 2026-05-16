package lab.localcloudnative.lcnpages.service;

import lab.localcloudnative.lcnpages.domain.Page;
import lab.localcloudnative.lcnpages.dto.PageRequest;
import lab.localcloudnative.lcnpages.repository.PageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PageService {

    private final PageRepository pageRepository;

    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public List<Page> listPages(String ownerId) {
        return pageRepository.findByOwnerId(ownerId);
    }

    public List<Page> searchByTitle(String ownerId, String query) {
        return pageRepository.findByOwnerIdAndTitleContainingIgnoreCase(ownerId, query);
    }

    public List<Page> searchByTag(String ownerId, String tag) {
        return pageRepository.findByOwnerIdAndTagsContaining(ownerId, tag);
    }

    public Page getPage(String ownerId, String id) {
        return pageRepository.findByOwnerIdAndId(ownerId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
    }

    public Page createPage(String ownerId, PageRequest request) {
        Page page = new Page();
        page.setOwnerId(ownerId);
        page.setTitle(request.title());
        page.setSlug(toSlug(request.title()));
        page.setContentMarkdown(request.contentMarkdown());
        page.setParentId(request.parentId());
        page.setTags(request.tags() != null ? request.tags() : new ArrayList<>());
        return pageRepository.save(page);
    }

    public Page updatePage(String ownerId, String id, PageRequest request) {
        Page page = getPage(ownerId, id);
        page.setTitle(request.title());
        page.setSlug(toSlug(request.title()));
        page.setContentMarkdown(request.contentMarkdown());
        page.setParentId(request.parentId());
        page.setTags(request.tags() != null ? request.tags() : new ArrayList<>());
        return pageRepository.save(page);
    }

    public void deletePage(String ownerId, String id) {
        Page page = getPage(ownerId, id);
        pageRepository.delete(page);
    }

    // slug: lowercase, non-alphanumeric (except space/dash) removed, spaces→dash,
    //       collapse multiple dashes, trim leading/trailing dashes. Fallback "untitled".
    private String toSlug(String title) {
        if (title == null || title.isBlank()) {
            return "untitled";
        }
        return title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 \\-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+|-+$", "");
    }
}
