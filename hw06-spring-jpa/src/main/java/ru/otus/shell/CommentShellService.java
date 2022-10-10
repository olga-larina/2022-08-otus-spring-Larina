package ru.otus.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.dto.BookShortDto;
import ru.otus.dto.CommentDto;
import ru.otus.service.CommentService;

import java.util.List;
import java.util.Optional;

@ShellComponent
public class CommentShellService {

    private final CommentService commentService;

    public CommentShellService(CommentService commentService) {
        this.commentService = commentService;
    }

    // Пример использования: ic --t 'Новинка' --ib 1
    @ShellMethod(value = "Insert new comment", key = {"insert-comment", "ic"})
    public String insertComment(@ShellOption(help = "Comment text", value = {"--text", "--t"}) String commentText,
                                @ShellOption(help = "Book id", value = {"--id-book", "--ib"}) long bookId) {
        CommentDto comment = new CommentDto(commentText, new BookShortDto(bookId));
        return commentService.insert(comment).toString();
    }

    // Пример использования: uc --i 9 --t 'Бестселлер' --ib 1
    @ShellMethod(value = "Update comment", key = {"update-comment", "uc"})
    public String updateComment(@ShellOption(help = "Comment id", value = {"--id", "--i"}) long commentId,
                                @ShellOption(help = "Comment text", value = {"--text", "--t"}) String commentText,
                                @ShellOption(help = "Book id", value = {"--id-book", "--ib"}) long bookId) {
        CommentDto comment = new CommentDto(commentId, commentText, new BookShortDto(bookId));
        return commentService.update(comment).toString();
    }

    @ShellMethod(value = "Get comment by id", key = {"get-comment", "gc"})
    public String getCommentById(@ShellOption(help = "Book id", value = {"--id", "--i"}) long bookId) {
        Optional<CommentDto> book = commentService.getById(bookId);
        return book.map(CommentDto::toString).orElse(null);
    }

    @ShellMethod(value = "Get all comments for book", key = {"get-book-comments", "gcb"})
    public List<CommentDto> getCommentByBookId(@ShellOption(help = "Book id", value = {"--id-book", "--ib"}) long bookId) {
        return commentService.getByBookId(bookId);
    }

    @ShellMethod(value = "Delete comment by id", key = {"delete-comment", "dc"})
    public boolean deleteCommentById(@ShellOption(help = "Comment id", value = {"--id", "--i"}) long commentId) {
        return commentService.deleteById(commentId);
    }

}
