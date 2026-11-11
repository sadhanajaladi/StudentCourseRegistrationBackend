package com.studentregistration.document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // Get document version history
    @GetMapping("/{documentId}/versions")
    public List<DocumentVersion> getVersionHistory(@PathVariable Long documentId) {
        log.info("[DocumentController] Version history requested for document ID: {}", documentId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        documentService.checkDocumentReadAccess(documentId, authentication);

        return documentService.getVersionHistory(documentId);
    }

    // Upload a new document
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadDocument(
            @RequestParam String name,
            @RequestParam DocumentType type,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        log.info("[DocumentController] Uploading new document: '{}', type: {}", name, type);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String ownerEmail = authentication.getName();

        Document document = documentService.uploadFirstVersion(
                name,
                type,
                ownerEmail,
                file
        );

        return Map.of(
                "message", "Document uploaded successfully",
                "documentId", document.getId(),
                "name", document.getName(),
                "type", document.getType(),
                "currentVersion", document.getCurrentVersion()
        );
    }

    // Upload a new version
    @PostMapping(
            value = "/{documentId}/versions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> uploadNewVersion(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        log.info("[DocumentController] Uploading new version for document ID: {}", documentId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uploadedBy = authentication.getName();

        DocumentVersion version = documentService.uploadNewVersion(
                documentId,
                file,
                uploadedBy
        );

        return Map.of(
                "message", "New document version uploaded successfully",
                "documentId", documentId,
                "versionId", version.getId(),
                "versionNumber", version.getVersionNumber(),
                "originalFileName", version.getOriginalFileName()
        );
    }

    // Download a specific document version
    @GetMapping("/{documentId}/versions/{versionNumber}/download")
    public ResponseEntity<Resource> downloadDocumentVersion(
            @PathVariable Long documentId,
            @PathVariable Integer versionNumber) {

        log.info("[DocumentController] Download requested for doc ID: {}, version: {}", documentId, versionNumber);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        documentService.checkDocumentReadAccess(documentId, authentication);

        try {
            DocumentVersion version = documentService.getDocumentVersion(documentId, versionNumber);

            Path filePath = Paths.get(version.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());
            System.out.println("Database file path: " + version.getFilePath());
            System.out.println("Resolved file path: " + filePath);
            System.out.println("File exists: " + Files.exists(filePath));
            System.out.println("File readable: " + Files.isReadable(filePath));
            System.out.println("Absolute path: " + filePath.toAbsolutePath());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stored document file not found"
                );
            }

            String filename = Paths.get(version.getOriginalFileName()).getFileName().toString();
            MediaType mediaType = filename.toLowerCase().endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            (mediaType.equals(MediaType.APPLICATION_PDF) ? "inline" : "attachment")
                                    + "; filename=\"" + filename + "\""
                    )
                    .body(resource);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to download document",
                    e
            );
        }
    }

    // Download the latest document version
    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadLatestDocumentVersion(
            @PathVariable Long documentId) {

        log.info("[DocumentController] Download latest requested for doc ID: {}", documentId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        documentService.checkDocumentReadAccess(documentId, authentication);

        try {
            DocumentVersion version = documentService.getLatestDocumentVersion(documentId);

            Path filePath = Paths.get(version.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stored document file not found"
                );
            }

            String filename = Paths.get(version.getOriginalFileName()).getFileName().toString();
            MediaType mediaType = filename.toLowerCase().endsWith(".pdf")
                    ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            (mediaType.equals(MediaType.APPLICATION_PDF) ? "inline" : "attachment")
                                    + "; filename=\"" + filename + "\""
                    )
                    .header(
                            "X-Document-Version",
                            String.valueOf(version.getVersionNumber())
                    )
                    .body(resource);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to download document",
                    e
            );
        }
    }

    // Checkout document - owner only
    @PostMapping("/{documentId}/checkout")
    public Map<String, Object> checkOutDocument(@PathVariable Long documentId) {
        log.info("[DocumentController] Checkout requested for document ID: {}", documentId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Document document = documentService.checkOutDocument(documentId, userEmail);

        return Map.of(
                "message", "Document checked out successfully",
                "documentId", document.getId(),
                "checkedOut", document.getCheckedOut(),
                "checkedOutBy", document.getCheckedOutBy()
        );
    }

    // Check-in document - owner only
    @PostMapping(
            value = "/{documentId}/checkin",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> checkInDocument(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        log.info("[DocumentController] Checkin requested for document ID: {}", documentId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        DocumentVersion version = documentService.checkInDocument(
                documentId,
                file,
                userEmail
        );

        return Map.of(
                "message", "Document checked in successfully",
                "documentId", documentId,
                "versionNumber", version.getVersionNumber(),
                "originalFileName", version.getOriginalFileName(),
                "checkedOut", false
        );
    }

    // Get logged-in student's documents
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<Document> getMyDocuments() {
        log.info("[DocumentController] Student fetching own documents");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String ownerEmail = authentication.getName();

        return documentService.getMyDocuments(ownerEmail);
    }

    // Admin can view all documents
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Document> getAllDocuments() {
        log.info("[DocumentController] Admin fetching all documents");

        return documentService.getAllDocuments();
    }
}