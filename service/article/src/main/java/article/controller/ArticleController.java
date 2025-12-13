package article.controller;

import article.dto.ArticleRequest;
import article.dto.ArticleResponse;
import article.service.ArticleService;
import common.dto.ApiResponse;
import common.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final JwtTokenProvider jwtTokenProvider;

    private record UserInfo(Long userId, String nickname) {
    }

    private UserInfo resolveUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new UserInfo(1L, "테스트사용자");
        }
        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsername(token);
        // In MSA, we would call User service here. For now, mock it.
        return new UserInfo(1L, username);
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> createArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ArticleRequest request) {
        UserInfo user = resolveUser(authHeader);
        Long articleId = articleService.createArticle(user.userId(), user.nickname(), request);
        return ApiResponse.created(Map.of("articleId", articleId));
    }

    @GetMapping("/{articleId}")
    public ApiResponse<ArticleResponse> getArticle(@PathVariable Long articleId) {
        ArticleResponse response = articleService.getArticle(articleId);
        return ApiResponse.ok(response);
    }

    @GetMapping
    public ApiResponse<List<ArticleResponse>> getArticles(
            @RequestParam Long boardId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long lastArticleId) {
        List<ArticleResponse> responses = articleService.getArticles(boardId, pageSize, lastArticleId);
        return ApiResponse.ok(responses);
    }

    @PutMapping("/{articleId}")
    public ApiResponse<Map<String, Long>> updateArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long articleId,
            @Valid @RequestBody ArticleRequest request) {
        UserInfo user = resolveUser(authHeader);
        Long updatedId = articleService.updateArticle(user.userId(), articleId, request);
        return ApiResponse.ok(Map.of("articleId", updatedId));
    }

    @DeleteMapping("/{articleId}")
    public ApiResponse<Void> deleteArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long articleId) {
        UserInfo user = resolveUser(authHeader);
        articleService.deleteArticle(user.userId(), articleId);
        return ApiResponse.ok(null);
    }
}
