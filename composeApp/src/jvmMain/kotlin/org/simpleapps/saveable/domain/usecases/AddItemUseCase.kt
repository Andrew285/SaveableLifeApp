package org.simpleapps.saveable.domain.usecases

import org.simpleapps.saveable.domain.repository.IMainRepository

class AddItemUseCase(
    private val repository: IMainRepository
) {
    suspend operator fun invoke(categoryName: String, content: String) {
        repository.addItem(categoryName, content)
    }
}