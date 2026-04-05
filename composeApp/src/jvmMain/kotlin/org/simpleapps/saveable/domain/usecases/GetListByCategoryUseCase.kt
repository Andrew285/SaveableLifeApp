package org.simpleapps.saveable.domain.usecases

import org.simpleapps.saveable.domain.SaveableItem
import org.simpleapps.saveable.domain.repository.IMainRepository

class GetListByCategoryUseCase(
    private val repository: IMainRepository
) {
    suspend operator fun invoke(categoryName: String): List<SaveableItem> {
        return repository.getItemsByCategory(categoryName)
    }
}