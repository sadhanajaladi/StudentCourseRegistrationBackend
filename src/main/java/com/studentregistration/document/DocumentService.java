package com.studentregistration.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentStorageService documentStorageService;
    private final DocumentAnnotationRepository annotationRepository;
    
    public DocumentService(
            DocumentRepository documentRepository,
            DocumentVersionRepository documentVersionRepository,
            DocumentStorageService documentStorageService,
            DocumentAnnotationRepository annotationRepository) {

        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.documentStorageService = documentStorageService;
        this.annotationRepository = annotationRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentVersion> getVersionHistory(Long documentId) {

        if (!documentRepository.existsById(documentId)) {
            throw new IllegalArgumentException(
                    "Document not found with ID: " + documentId);
        }

        return documentVersionRepository
                .findByDocumentIdOrderByVersionNumberDesc(documentId);
    }
    

	@Transactional
	public Document createDocument(
	        String name,
	        DocumentType type,
	        String ownerEmail) {
	
	    Document document = new Document();
	
	    document.setName(name);
	    document.setType(type);
	    document.setOwnerEmail(ownerEmail);
	    document.setCurrentVersion(1);
	
	    return documentRepository.save(document);
	}

	@Transactional
	public DocumentVersion createFirstVersion(
	        Document document,
	        String filePath,
	        String originalFileName,
	        String contentType,
	        Long fileSize,
	        String uploadedBy) {
	
	    DocumentVersion version = new DocumentVersion();
	
	    version.setDocument(document);
	    version.setVersionNumber(1);
	    version.setFilePath(filePath);
	    version.setOriginalFileName(originalFileName);
	    version.setContentType(contentType);
	    version.setFileSize(fileSize);
	    version.setUploadedBy(uploadedBy);
	
	    return documentVersionRepository.save(version);
	}
	

	@Transactional
	public Document uploadFirstVersion(
	        String name,
	        DocumentType type,
	        String ownerEmail,
	        MultipartFile file) throws IOException {
	
	    String filePath = documentStorageService.storeFile(file);
	
	    Document document = createDocument(
	            name,
	            type,
	            ownerEmail);
	
	    createFirstVersion(
	            document,
	            filePath,
	            file.getOriginalFilename(),
	            file.getContentType(),
	            file.getSize(),
	            ownerEmail);
	
	    return document;
	}
	

@Transactional
public DocumentVersion uploadNewVersion(
        Long documentId,
        MultipartFile file,
        String uploadedBy) throws IOException {
	

if (!documentRepository.existsByIdAndOwnerEmail(
        documentId, uploadedBy)) {
    throw new org.springframework.security.access.AccessDeniedException(
            "You are not allowed to modify this document.");
}


    // 1. Find the existing document
    Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Document not found with ID: " + documentId));

    // 2. Store the new file without overwriting the old file
    String filePath = documentStorageService.storeFile(file);

    // 3. Calculate the next version number
    int nextVersion = document.getCurrentVersion() + 1;

    // 4. Create a database record for this version
    DocumentVersion version = new DocumentVersion();

    version.setDocument(document);
    version.setVersionNumber(nextVersion);
    version.setFilePath(filePath);
    version.setOriginalFileName(file.getOriginalFilename());
    version.setContentType(file.getContentType());
    version.setFileSize(file.getSize());
    version.setUploadedBy(uploadedBy);

    // 5. Save the version record
    DocumentVersion savedVersion =
            documentVersionRepository.save(version);

    // 6. Update the document's current version
    document.setCurrentVersion(nextVersion);
    documentRepository.save(document);

    return savedVersion;
}
@Transactional(readOnly = true)
public DocumentVersion getDocumentVersion(
        Long documentId,
        Integer versionNumber) {

    if (!documentRepository.existsById(documentId)) {
        throw new IllegalArgumentException(
                "Document not found with ID: " + documentId);
    }

    return documentVersionRepository
            .findByDocumentIdOrderByVersionNumberDesc(documentId)
            .stream()
            .filter(version ->
                    version.getVersionNumber()
                            .equals(versionNumber))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                    "Version " + versionNumber
                    + " not found for document ID: " + documentId));
}


@Transactional(readOnly = true)
public DocumentVersion getLatestDocumentVersion(Long documentId) {

    if (!documentRepository.existsById(documentId)) {
        throw new IllegalArgumentException(
                "Document not found with ID: " + documentId);
    }

    return documentVersionRepository
            .findByDocumentIdOrderByVersionNumberDesc(documentId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                    "No versions found for document ID: " + documentId));
}


@Transactional
public Document checkOutDocument(Long documentId, String userEmail) {

    // 1. Find the document
    Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Document not found with ID: " + documentId));

    // 2. Verify ownership
    if (!document.getOwnerEmail().equals(userEmail)) {
        throw new org.springframework.security.access.AccessDeniedException(
                "You are not allowed to check out this document.");
    }

    // 3. Check whether it is already checked out
    if (Boolean.TRUE.equals(document.getCheckedOut())) {
        throw new IllegalArgumentException(
                "Document is already checked out by "
                        + document.getCheckedOutBy());
    }

    // 4. Mark it as checked out
    document.setCheckedOut(true);
    document.setCheckedOutBy(userEmail);

    // 5. Save the change
    return documentRepository.save(document);
}


@Transactional
public DocumentVersion checkInDocument(
        Long documentId,
        MultipartFile file,
        String userEmail) throws IOException {

    Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Document not found with ID: " + documentId));

    if (!document.getOwnerEmail().equals(userEmail)) {
        throw new org.springframework.security.access.AccessDeniedException(
                "You are not allowed to check in this document.");
    }

    if (!Boolean.TRUE.equals(document.getCheckedOut())) {
        throw new IllegalArgumentException(
                "Document is not checked out.");
    }

    if (!userEmail.equals(document.getCheckedOutBy())) {
        throw new org.springframework.security.access.AccessDeniedException(
                "Only the user who checked out the document can check it in.");
    }

    // Save the edited file as a new version
    DocumentVersion version =
            uploadNewVersion(documentId, file, userEmail);

    // Release the checkout lock
    document.setCheckedOut(false);
    document.setCheckedOutBy(null);
    documentRepository.save(document);

    return version;
}



@Transactional(readOnly = true)
public boolean ownsDocument(Long documentId, String ownerEmail) {
    return documentRepository
            .existsByIdAndOwnerEmail(documentId, ownerEmail);
}

@Transactional(readOnly = true)
public List<Document> getMyDocuments(String ownerEmail) {
    return documentRepository.findByOwnerEmail(ownerEmail);
}

@Transactional(readOnly = true)
public List<Document> getAllDocuments() {
    return documentRepository.findAll();
}

public void checkDocumentReadAccess(
        Long documentId,
        Authentication authentication) {

    boolean isAdmin = authentication.getAuthorities()
            .stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

    boolean isOwner = documentRepository.existsByIdAndOwnerEmail(
            documentId,
            authentication.getName()
    );

    if (!isAdmin && !isOwner) {
        throw new AccessDeniedException(
                "You are not allowed to view this document."
        );
    }
}


// Create an annotation - document owner only
@Transactional
public DocumentAnnotation addAnnotation(
        Long documentId,
        Integer versionNumber,
        CreateAnnotationRequest request,
        Authentication authentication) {

    checkDocumentReadAccess(documentId, authentication);

    boolean isOwner = documentRepository.existsByIdAndOwnerEmail(
            documentId,
            authentication.getName()
    );

    if (!isOwner) {
        throw new AccessDeniedException(
                "Only the document owner can create annotations."
        );
    }

    // Check whether the requested document version exists
    getDocumentVersion(documentId, versionNumber);

    // Validate annotation details
    if (request.getType() == null
            || request.getPageNumber() == null
            || request.getPageNumber() < 1
            || !validCoordinate(request.getX())
            || !validCoordinate(request.getY())
            || !validCoordinate(request.getWidth())
            || !validCoordinate(request.getHeight())
            || request.getWidth() <= 0
            || request.getHeight() <= 0
            || request.getX() + request.getWidth() > 1
            || request.getY() + request.getHeight() > 1) {

        throw new IllegalArgumentException(
                "Invalid annotation type, page number or coordinates."
        );
    }

    DocumentAnnotation annotation = new DocumentAnnotation();

    annotation.setDocumentId(documentId);
    annotation.setVersionNumber(versionNumber);
    annotation.setType(request.getType());
    annotation.setPageNumber(request.getPageNumber());

    annotation.setX(request.getX());
    annotation.setY(request.getY());
    annotation.setWidth(request.getWidth());
    annotation.setHeight(request.getHeight());

    annotation.setCreatedBy(authentication.getName());

    return annotationRepository.save(annotation);
}

// Validate normalized coordinates
private boolean validCoordinate(Double value) {
    return value != null
            && Double.isFinite(value)
            && value >= 0.0
            && value <= 1.0;
}

// Retrieve annotations for a particular document version
@Transactional(readOnly = true)
public List<DocumentAnnotation> getAnnotations(
        Long documentId,
        Integer versionNumber,
        Authentication authentication) {

    checkDocumentReadAccess(documentId, authentication);

    getDocumentVersion(documentId, versionNumber);

    return annotationRepository
            .findByDocumentIdAndVersionNumberOrderByCreatedAtDesc(
                    documentId,
                    versionNumber
            );
}

// Count annotations belonging to a document
@Transactional(readOnly = true)
public long getAnnotationCount(
        Long documentId,
        Authentication authentication) {

    checkDocumentReadAccess(documentId, authentication);

    return annotationRepository.countByDocumentId(documentId);
}


}
