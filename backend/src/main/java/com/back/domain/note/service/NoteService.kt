package com.back.domain.note.service

import com.back.domain.book.wrote.entity.Wrote
import com.back.domain.bookmarks.entity.Bookmark
import com.back.domain.bookmarks.repository.BookmarkRepository
import com.back.domain.member.member.entity.Member
import com.back.domain.note.dto.BookDto
import com.back.domain.note.entity.Note
import com.back.domain.note.repository.NoteRepository
import com.back.global.exception.ServiceException
import lombok.RequiredArgsConstructor
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*
import java.util.function.Supplier

@Service
@RequiredArgsConstructor
class NoteService (
    private val noteRepository: NoteRepository,
    private val bookmarkRepository: BookmarkRepository
) {
    fun findBookmarkById(id: Int): Bookmark? {
        return bookmarkRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("${id}번 북마크가 없습니다.")
    }

    fun write(bookmarkId: Int, title: String, content: String, page: String?, member: Member): Note {
        val bookmark = bookmarkRepository.findByIdOrNull(bookmarkId)
            ?: throw NoSuchElementException("${bookmarkId}번 북마크가 없습니다.")

        val note = Note(member, title, content, page, bookmark)
        bookmark.getNotes().add(note)

        return note
    }

    fun flush() {
        noteRepository.flush()
    }

    fun modify(note: Note, title: String, content: String, page: String?) {
        note.modify(title, content, page)
    }

    fun count(): Long {
        return noteRepository.count()
    }

    fun delete(bookmark: Bookmark, note: Note) {
        bookmark.notes.remove(note)
    }

    fun findNoteById(bookmark: Bookmark, id: Int): Note {
        return bookmark.notes.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("${id}번 노트가 존재하지 않습니다.")
    }

    fun checkNotePageCURD(bookmark: Bookmark, actor: Member?, str: String, resultCode: String) {
        if (bookmark.member != actor) {
            throw ServiceException(resultCode, "${bookmark.getId()}번 북마크의 노트 $str 권한이 없습니다.")
        }
    }

    fun getBookInfo(bookmark: Bookmark): BookDto {
        val book = bookmark.book

        val imageUrl = book.imageUrl
        val title = book.title
        val category = book.category.name

        val authors = book.authors.map { it.author.name }

        return BookDto(imageUrl, title, authors, category)
    }
}
