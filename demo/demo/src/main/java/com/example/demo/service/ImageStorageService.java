package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final Path uploadPath;

    public ImageStorageService(
            @Value("${app.upload.dir}")String uploadDir
    ){
        /*
         * 설정 파일에 지정한 업로드 폴더를
         * 절대경로로 변환해 보관한다.
         */
        this.uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public String store(MultipartFile file) {

        /*
         * 1. 빈 파일 검사
         */
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지가 비어 있습니다.");
        }

        /*
         * 2. 파일 크기 제한
         *
         * 설정 파일의 크기 제한과 별개로
         * 서비스에서도 검증한다.
         */
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("이미지는 5MB 이하만 가능합니다.");
        }

        /*
         * 3. 파일 종류 확인
         *
         * 클라이언트가 전달한 Content-Type은
         * 위조될 수 있으므로 이것만으로
         * 실제 이미지 파일인지 보장할 수 없다.
         */
        String contentType = file.getContentType();

        String extension = switch (
                contentType == null ? "" : contentType
                ) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 이미지 형식입니다."
            );
        };

        /*
         * 4. 저장 파일명 생성
         *
         * 사용자가 올린 원본 파일명 대신 UUID 사용.
         * 파일명 중복과 경로 조작 위험을 줄인다.
         */
        String fileName = UUID.randomUUID() + extension;

        try {
            /*
             * 5. 업로드 디렉터리 생성
             *
             * 이미 존재하면 그대로 사용한다.
             */
            Files.createDirectories(uploadPath);

            Path target = uploadPath.resolve(fileName);

            /*
             * 6. 이미지 파일 저장
             *
             * CREATE_NEW는 같은 이름의 파일이
             * 존재하면 덮어쓰지 않고 실패하도록 한다.
             */
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }

            /*
             * 7. 실제 디스크 경로가 아니라
             * 공개 이미지 URL을 반환한다.
             */
            return "/uploads/items/" + fileName;

        } catch (IOException e) {
            throw new UncheckedIOException("이미지 저장에 실패했습니다.", e);
        }
    }

    //이 코드는 로컬 개발용 기본 구현. 실제 서비스에서는 파일의 확장자와
    // Content-Type만 믿지 말고 파일 시그니처를 검증하고,
    // 가능하면 이미지를 디코딩·재인코딩하는 과정까지 추가하는 것이 안전
}
