package com.back.domain.note.repository

import com.back.domain.note.entity.Note
import org.springframework.data.jpa.repository.JpaRepository

interface NoteRepository : JpaRepository<Note, Int> {

}
