package ru.otus.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dao.AuthorDao;
import ru.otus.dao.BookDao;
import ru.otus.dao.GenreDao;
import ru.otus.domain.Author;
import ru.otus.domain.Book;
import ru.otus.domain.Genre;
import ru.otus.dto.BookDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookDao bookDao;
    private final AuthorDao authorDao;
    private final GenreDao genreDao;

    public BookServiceImpl(BookDao bookDao, AuthorDao authorDao, GenreDao genreDao) {
        this.bookDao = bookDao;
        this.authorDao = authorDao;
        this.genreDao = genreDao;
    }

    @Transactional
    @Override
    public BookDto insert(BookDto bookDto) {
        Book book = bookDto.fromDto();
        populateAuthorAndGenres(book);
        return new BookDto(bookDao.save(book));
    }

    @Transactional
    @Override
    public BookDto update(BookDto bookDto) {
        Book book = bookDto.fromDto();
        populateAuthorAndGenres(book);
        return new BookDto(bookDao.save(book));
    }

    @Transactional
    @Override
    public boolean deleteById(long id) {
        return bookDao.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<BookDto> getById(long id) {
        return bookDao.getById(id).map(BookDto::new);
    }

    @Transactional(readOnly = true) // чтобы могли извлечься LAZY-сущности, которые не инициализируются в dao
    @Override
    public List<BookDto> getAll() {
        return bookDao.getAll().stream().map(BookDto::new).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return bookDao.count();
    }

    private void populateAuthorAndGenres(Book book) {
        populateAuthor(book);
        populateGenres(book);
    }

    private void populateAuthor(Book book) {
        book.setAuthor(getBookAuthor(book));
    }

    private void populateGenres(Book book) {
        book.setGenres(getBookGenres(book));
    }

    private Author getBookAuthor(Book book) {
        return authorDao.getByName(book.getAuthor().getName()).orElse(book.getAuthor());
    }

    private List<Genre> getBookGenres(Book book) {
        List<Genre> genres = new ArrayList<>();
        List<String> genreTitles = book.getGenres().stream().map(Genre::getTitle).collect(Collectors.toList());
        Map<String, Genre> genresFound = genreDao.getByTitles(genreTitles);
        book.getGenres().forEach(genre -> {
            Genre found = genresFound.get(genre.getTitle());
            genres.add(Objects.requireNonNullElse(found, genre));
        });
        return genres;
    }

}