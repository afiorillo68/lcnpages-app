package lab.localcloudnative.lcnpages.controller;

import lab.localcloudnative.lcnpages.domain.Page;
import lab.localcloudnative.lcnpages.dto.PageRequest;
import lab.localcloudnative.lcnpages.service.PageService;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @GetMapping
    public List<Page> listPages(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tag) {
        String ownerId = jwt.getSubject();
        if (q != null) {
            return pageService.searchByTitle(ownerId, q);
        }
        if (tag != null) {
            return pageService.searchByTag(ownerId, tag);
        }
        return pageService.listPages(ownerId);
    }

    @GetMapping("/{id}")
    public Page getPage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable ObjectId id) {
        return pageService.getPage(jwt.getSubject(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Page createPage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PageRequest request) {
        return pageService.createPage(jwt.getSubject(), request);
    }

    @PutMapping("/{id}")
    public Page updatePage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable ObjectId id,
            @RequestBody PageRequest request) {
        return pageService.updatePage(jwt.getSubject(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePage(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable ObjectId id) {
        pageService.deletePage(jwt.getSubject(), id);
    }
}
