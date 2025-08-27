package com.back.domain.book.category.entity;

import com.back.domain.book.book.entity.Book;
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
public class Category extends BaseEntity {
    String name;

    @OneToMany(mappedBy = "category")
    private List<Book> books = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }
}
