package com.back.domain.book.author.entity;

import com.back.domain.book.wrote.entity.Wrote;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Author extends BaseEntity {
    private String name;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wrote> books = new ArrayList<>();

    public Author(String name) {
        this.name = name;
    }
}
