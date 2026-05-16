package lab.localcloudnative.lcnpages.controller;

import lab.localcloudnative.lcnpages.domain.Page;
import lab.localcloudnative.lcnpages.repository.PageRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
public class PageController {

    private final PageRepository pageRepository;

    public PageController(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @GetMapping
    public List<Page> listPages(@AuthenticationPrincipal Jwt jwt) {
        String ownerId = jwt.getSubject();
        return pageRepository.findByOwnerId(ownerId);
    }
}
