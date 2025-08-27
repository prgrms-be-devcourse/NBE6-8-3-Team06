package com.back.domain.note.service;

import com.back.domain.book.book.entity.Book;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.note.dto.BookDto;
import com.back.domain.note.entity.Note;
import com.back.domain.note.repository.NoteRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final BookmarkRepository bookmarkRepository;

    public Optional<Bookmark> findBookmarkById(int id) {
        return bookmarkRepository.findById(id);
    }

    public Note write(int bookmarkId, String title, String content, String page, Member member) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new NoSuchElementException("%d번 북마크가 없습니다.".formatted(bookmarkId)));

        Note note = new Note(title, content, page, bookmark, member);
        bookmark.getNotes().add(note);

        return note;
    }

    public void flush() {
        noteRepository.flush();
    }

    public void modify(Note note, String title, String content, String page) {
        note.setTitle(title);
        note.setContent(content);
        note.setPage(page);
    }

    public long count() {
        return noteRepository.count();
    }

    public void delete(Bookmark bookmark, Note note) {
        bookmark.getNotes().remove(note);
    }

    public Optional<Note> findNoteById(Bookmark bookmark, int id) {
        return bookmark
                .getNotes()
                .stream()
                .filter(note -> note.getId() == id)
                .findFirst();
    }

    public void checkNotePageCURD(Bookmark bookmark, Member actor, String str, String resultCode) {
        if (!bookmark.getMember().equals(actor)) {
            throw new ServiceException(resultCode, "%d번 북마크의 노트 %s 권한이 없습니다.".formatted(bookmark.getId(), str));
        }
    }

    public BookDto getBookInfo(Bookmark bookmark) {
        Book book = bookmark.getBook();

        String imageUrl = book.getImageUrl();
        String title = book.getTitle();
        String category = book.getCategory().getName();

        List<String> authors = book.getAuthors()
                .stream()
                .map(wrote -> wrote.getAuthor().getName())
                .toList();

        return new BookDto(imageUrl, title, authors, category);
    }
}
