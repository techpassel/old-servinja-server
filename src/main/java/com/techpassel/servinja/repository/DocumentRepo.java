package com.techpassel.servinja.repository;

import com.techpassel.servinja.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentRepo extends JpaRepository<Document, Integer> {
    @Query("from Document where user_id=?1")
    List<Document> getDocumentsByUserId(int userId);
}
