package org.simpleapps.saveable.domain.usecases

import org.simpleapps.saveable.domain.repository.IMainRepository

class DeleteItemUseCase(
    private val repository: IMainRepository
) {
    suspend operator fun invoke(id: Long): Int {
        return repository.deleteItem(id)
    }
}