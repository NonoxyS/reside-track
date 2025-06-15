package dev.nonoxy.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.nonoxy.core.database.entities.StudentEntity
import dev.nonoxy.core.database.relations.StudentWithRoom

@Dao
interface StudentDao {

    @Query("SELECT * FROM students")
    suspend fun getAllStudents(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: Long): StudentEntity?

    @Insert
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Transaction
    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentWithRoom(studentId: Long): StudentWithRoom?

    @Query("SELECT * FROM students WHERE roomId = :roomId")
    suspend fun getStudentsByRoom(roomId: Long): List<StudentEntity>

    @Query("SELECT * FROM students WHERE streamNumber = :streamNumber")
    suspend fun getStudentsByStream(streamNumber: Int): List<StudentEntity>

    @Query("SELECT * FROM students WHERE checkOutDateEpochMillis >= :timestamp")
    suspend fun getStudentsCheckingOutAfter(timestamp: Long): List<StudentEntity>

    @Query("SELECT COUNT(*) FROM students WHERE roomId = :roomId")
    suspend fun countStudentsInRoom(roomId: Long): Int
}
