package article.repository;

import article.model.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findAllByBoardIdOrderByIdDesc(Long boardId, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.boardId = :boardId AND a.id < :lastArticleId ORDER BY a.id DESC")
    List<Article> findAllByBoardIdAndIdLessThan(@Param("boardId") Long boardId,
            @Param("lastArticleId") Long lastArticleId, Pageable pageable);
}
