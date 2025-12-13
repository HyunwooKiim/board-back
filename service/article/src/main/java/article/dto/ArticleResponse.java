package article.dto;

import article.model.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private Long articleId;
    private Long boardId;
    private String title;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;

    public static ArticleResponse from(Article article) {
        return ArticleResponse.builder()
                .articleId(article.getId())
                .boardId(article.getBoardId())
                .title(article.getTitle())
                .content(article.getContent())
                .writerNickname(article.getWriterNickname())
                .createdAt(article.getCreatedAt())
                .build();
    }
}
