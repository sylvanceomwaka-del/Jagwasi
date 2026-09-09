package com.jagwasi.domain.usecase

class RestockUseCase {
    suspend operator fun invoke(): Result<Unit> {
        // TODO: implement restock logic
        return Result.Success(Unit)
    }
}
