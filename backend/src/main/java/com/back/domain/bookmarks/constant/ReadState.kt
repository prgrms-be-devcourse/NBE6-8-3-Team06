package com.back.domain.bookmarks.constant

enum class ReadState {
    WISH, READING, READ;

    fun getState(state: String): ReadState? {
        if (state == "WISH") {
            return ReadState.WISH
        }
        if (state == "READING") {
            return ReadState.READING
        }
        if (state == "READ") {
            return ReadState.READ
        }
        return null
    }
}
