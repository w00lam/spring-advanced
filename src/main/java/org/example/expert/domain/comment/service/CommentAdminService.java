package org.example.expert.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.tran${DB_USERNAME}ction.annotation.Tran${DB_USERNAME}ctional;

@Service
@RequiredArgsConstructor
public class CommentAdminService {

    private final CommentRepository commentRepository;

    @Tran${DB_USERNAME}ctional
    public void deleteComment(long commentId) {
        commentRepository.deleteById(commentId);
    }
}
