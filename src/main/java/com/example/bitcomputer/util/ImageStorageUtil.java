package com.example.bitcomputer.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class ImageStorageUtil {
    
    @Value("${image.storage.path:BitComputer/images}")
    private String baseImagePath;
    
    /**
     * 프로젝트 루트 경로 가져오기
     */
    private Path getProjectRoot() {
        // 현재 작업 디렉토리에서 BitComputer 폴더 찾기
        Path currentPath = Paths.get("").toAbsolutePath();
        
        // 현재 경로에서 BitComputer 폴더 찾기
        Path bitComputerPath = currentPath.resolve("BitComputer");
        if (Files.exists(bitComputerPath) && Files.isDirectory(bitComputerPath)) {
            return bitComputerPath;
        }
        
        // 상위 디렉토리에서 찾기
        Path parent = currentPath.getParent();
        if (parent != null) {
            Path parentBitComputer = parent.resolve("BitComputer");
            if (Files.exists(parentBitComputer) && Files.isDirectory(parentBitComputer)) {
                return parentBitComputer;
            }
        }
        
        // 기본값: 현재 디렉토리에 BitComputer 폴더 생성
        Path defaultPath = currentPath.resolve("BitComputer");
        return defaultPath;
    }
    
    /**
     * 이미지를 저장하고 상대 경로를 반환
     * 
     * @param file 업로드된 이미지 파일
     * @param folderId 저장할 폴더 ID (예: 환자 ID 또는 요청 ID)
     * @param subFolder "original" 또는 "overlay"
     * @return 저장된 이미지의 상대 경로 (예: "images/1/original/view1_frontal.jpg")
     */
    public String saveImage(MultipartFile file, String folderId, String subFolder) throws IOException {
        // 프로젝트 루트 기준으로 이미지 폴더 경로 생성
        Path projectRoot = getProjectRoot();
        Path folderPath = projectRoot.resolve("images").resolve(folderId).resolve(subFolder);
        
        // 폴더가 없으면 생성
        Files.createDirectories(folderPath);
        
        // 원본 파일명 가져오기
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "image_" + UUID.randomUUID().toString() + ".jpg";
        }
        
        // 파일 저장 경로
        Path filePath = folderPath.resolve(originalFilename);
        
        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 상대 경로 반환 (슬래시로 통일)
        String relativePath = Paths.get("images", folderId, subFolder, originalFilename)
                .toString()
                .replace("\\", "/");
        
        return relativePath;
    }
    
    /**
     * 이미지 파일 삭제
     */
    public boolean deleteImage(String relativePath) {
        try {
            Path projectRoot = getProjectRoot();
            Path filePath = projectRoot.resolve(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 이미지 파일 존재 여부 확인
     */
    public boolean imageExists(String relativePath) {
        try {
            Path projectRoot = getProjectRoot();
            Path filePath = projectRoot.resolve(relativePath);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }
}
