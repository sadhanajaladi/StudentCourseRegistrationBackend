
package com.studentregistration.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document> findByOwnerEmail(String ownerEmail);

boolean existsByIdAndOwnerEmail(Long id, String ownerEmail);
java.util.Optional<Document> findByIdAndOwnerEmail(
        Long id, String ownerEmail);

}
