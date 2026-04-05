package org.simpleapps.saveable.domain.usecases

import org.simpleapps.saveable.domain.category.Category
import org.simpleapps.saveable.domain.repository.IMainRepository

class GetCategoriesUseCase(
    private val repository: IMainRepository
) {
    suspend operator fun invoke(): List<Category> {
        return repository.getAllCategories()
    }
}