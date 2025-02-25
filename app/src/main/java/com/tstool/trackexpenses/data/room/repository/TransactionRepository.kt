package com.tstool.trackexpenses.data.room.repository

import com.tstool.trackexpenses.data.room.entity.TransactionEntity
import com.tstool.trackexpenses.data.room.local.dao.TransactionDao

class TransactionRepository(private val transactionDao: TransactionDao) {
    suspend fun getAllTransactions(): List<TransactionEntity> = transactionDao.getAll()
    suspend fun getTransactionById(id: Int): TransactionEntity = transactionDao.getById(id)
    suspend fun searchTransactions(query: String): List<TransactionEntity> = transactionDao.search("%$query%")
    suspend fun insertTransaction(transaction: TransactionEntity): Long = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.update(transaction)
    suspend fun deleteTransaction(transaction: TransactionEntity) = transactionDao.delete(transaction)
}