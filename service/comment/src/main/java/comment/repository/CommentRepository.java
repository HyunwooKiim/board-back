package comment.repository;

import comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId ORDER BY c.createdAt ASC")
    List<Comment> findAllByArticleId(@Param("articleId") Long articleId);
}
