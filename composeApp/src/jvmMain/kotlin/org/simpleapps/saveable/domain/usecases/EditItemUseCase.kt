package org.simpleapps.saveable.domain.usecases

import org.simpleapps.saveable.domain.repository.IMainRepository

class EditItemUseCase(
    private val repository: IMainRepository
) {
    suspend operator fun invoke(id: Long, newValue: String): Int {
        return repository.editItem(id, newValue)
    }
}