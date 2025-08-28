package com.back.global.jpa.querydsl

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.Expressions
import org.springframework.data.domain.Sort

class QuerydslUtil {
    companion object{
        fun <T> toOrderSpecifiers(sort: Sort, qType: EntityPath<T>):List<OrderSpecifier<*>>{
            val specifiers = mutableListOf<OrderSpecifier<*>>()
            sort.forEach { order ->
                val direction = if (order.isAscending) Order.ASC else Order.DESC
                val path = Expressions.path(Comparable::class.java, qType, order.property)
                specifiers.add(OrderSpecifier(direction, path))
            }
            return specifiers
        }

        fun <T> buildKeywordPredicate(
            keywordType: String?,
            keyword: String?,
            qType: EntityPath<T>
        ): BooleanBuilder?{
            val builder = BooleanBuilder()
            if (keywordType.isNullOrBlank() || keyword.isNullOrBlank()){
                return builder
            }
            try{
                val path = Expressions.path(Comparable::class.java, qType, keywordType)
                val expression = Expressions.stringPath(path.toString()).containsIgnoreCase(keyword)
                builder.and(expression)
                return builder

            }catch (e: RuntimeException) {
                return null
            }
        }
    }
}