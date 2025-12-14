package comment.service;

import comment.dto.CommentRequest;
import comment.dto.CommentResponse;
import comment.model.Comment;
import comment.repository.CommentRepository;
import common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Transactional
    public Long createComment(Long userId, String nickname, CommentRequest request) {
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .id(snowflakeIdGenerator.nextId())
                .articleId(request.getArticleId())
                .userId(userId)
                .writerNickname(nickname)
                .content(request.getContent())
                .parent(parent)
                .build();

        return commentRepository.save(comment).getId();
    }

    public List<CommentResponse> getComments(Long articleId) {
        List<Comment> comments = commentRepository.findAllByArticleId(articleId);
        return convertToHierarchy(comments);
    }

    private List<CommentResponse> convertToHierarchy(List<Comment> comments) {
        Map<Long, CommentResponse> map = new HashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        for (Comment comment : comments) {
            map.put(comment.getId(), CommentResponse.from(comment));
        }

        for (Comment comment : comments) {
            CommentResponse dto = map.get(comment.getId());
            if (comment.getParent() != null) {
                CommentResponse parentDto = map.get(comment.getParent().getId());
                if (parentDto != null) {
                    parentDto.addChild(dto);
                }
            } else {
                roots.add(dto);
            }
        }

        return roots;
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        commentRepository.delete(comment);
    }
}
