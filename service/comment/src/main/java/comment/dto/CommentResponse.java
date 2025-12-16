package comment.dto;

import comment.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long commentId;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;
    @Builder.Default
    private List<CommentResponse> children = new ArrayList<>();

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .writerNickname(comment.getWriterNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public void addChild(CommentResponse child) {
        this.children.add(child);
    }
}
