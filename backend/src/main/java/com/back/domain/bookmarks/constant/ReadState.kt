package com.back.domain.bookmarks.constant;

public enum ReadState {
    WISH, READING, READ;

    public ReadState getState(String state) {
        if(state.equals("WISH")){ return WISH; }
        if(state.equals("READING")){ return READING; }
        if(state.equals("READ")){ return READ; }
        return null;
    }
}
