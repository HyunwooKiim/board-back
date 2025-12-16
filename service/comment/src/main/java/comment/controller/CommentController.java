package comment.controller;

import comment.dto.CommentRequest;
import comment.dto.CommentResponse;
import comment.service.CommentService;
import common.dto.ApiResponse;
import common.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    private record UserInfo(Long userId, String nickname) {
    }

    private UserInfo resolveUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new UserInfo(1L, "테스트사용자");
        }
        String token = authHeader.substring(7);
        String username = jwtTokenProvider.getUsername(token);
        return new UserInfo(1L, username);
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> createComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CommentRequest request) {
        UserInfo user = resolveUser(authHeader);
        Long commentId = commentService.createComment(user.userId(), user.nickname(), request);
        return ApiResponse.created(Map.of("commentId", commentId));
    }

    @GetMapping
    public ApiResponse<List<CommentResponse>> getComments(@RequestParam Long articleId) {
        List<CommentResponse> responses = commentService.getComments(articleId);
        return ApiResponse.ok(responses);
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long commentId) {
        UserInfo user = resolveUser(authHeader);
        commentService.deleteComment(user.userId(), commentId);
        return ApiResponse.ok(null);
    }
}
