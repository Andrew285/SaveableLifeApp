package org.simpleapps.saveable.domain.usecases

import org.simpleapps.saveable.domain.repository.IMainRepository

class AddCategoryUseCase(
    private val repository: IMainRepository
) {
    suspend operator fun invoke(categoryName: String) {
        repository.addCategory(categoryName)
    }
}