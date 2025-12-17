package article.service;

import article.dto.ArticleRequest;
import article.dto.ArticleResponse;
import article.model.Article;
import article.repository.ArticleRepository;
import common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Transactional
    public Long createArticle(Long userId, String nickname, ArticleRequest request) {
        Article article = Article.builder()
                .id(snowflakeIdGenerator.nextId())
                .boardId(request.getBoardId())
                .title(request.getTitle())
                .content(request.getContent())
                .userId(userId)
                .writerNickname(nickname)
                .build();

        return articleRepository.save(article).getId();
    }

    public ArticleResponse getArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        return ArticleResponse.from(article);
    }

    public List<ArticleResponse> getArticles(Long boardId, int pageSize, Long lastArticleId) {
        PageRequest pageRequest = PageRequest.of(0, pageSize);
        List<Article> articles;

        if (lastArticleId == null) {
            articles = articleRepository.findAllByBoardIdOrderByIdDesc(boardId, pageRequest);
        } else {
            articles = articleRepository.findAllByBoardIdAndIdLessThan(boardId, lastArticleId, pageRequest);
        }

        return articles.stream()
                .map(ArticleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long updateArticle(Long userId, Long articleId, ArticleRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        if (!article.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        article.update(request.getTitle(), request.getContent());
        return article.getId();
    }

    @Transactional
    public void deleteArticle(Long userId, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        if (!article.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        articleRepository.delete(article);
    }
}
