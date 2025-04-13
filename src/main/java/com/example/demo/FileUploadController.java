package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Controller
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    // 업로드된 파일을 저장할 디렉토리 경로 (프로젝트 루트 기준)
    // 실제 운영 환경에서는 application.properties 등 외부 설정 파일에서 관리하는 것이 좋습니다.
    private static final String UPLOAD_DIR = "uploaded-files/";

    //
    /**
     * 루트 경로 ("/") 요청 시 home.html 페이지를 보여줍니다.
     * 업로드 디렉토리가 없으면 생성합니다.
     * @return 보여줄 템플릿 이름 ("home")
     */
    @GetMapping("/")
    public String homePage(Model model) {
        log.info("루트 페이지 요청됨. 업로드 디렉토리 확인 및 생성 시도: {}", UPLOAD_DIR);
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성 성공: {}", uploadPath);
            } catch (IOException e) {
                log.error("업로드 디렉토리 생성 실패: {}", uploadPath, e);
                model.addAttribute("error", "서버 준비 중 오류가 발생했습니다. 관리자에게 문의하세요.");
            }
        }
        return "home";
    }
    /**
     * "/upload" 경로로 들어오는 POST 요청 (파일 업로드)을 처리합니다.
     * 파일을 지정된 디렉토리에 저장합니다. (확장자 검사 없음)
     * @param file 업로드된 파일 객체
     * @param redirectAttributes 리다이렉트 시 메시지 전달 객체
     * @return 리다이렉트할 경로 ("redirect:/")
     */
    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("peFile") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String originalFilename = file.getOriginalFilename(); // 원본 파일명은 로깅 및 메시지 표시에 사용
        log.info("파일 업로드 요청 받음: {}", originalFilename);

        // 업로드된 파일이 실제로 존재하는지에 대한 유효성 검사는
        // 웹html에서 애초에 파일을 선택하지않을 경우, 업로드 버튼이 동작하지 못하므로 생략

        // 1. 업로드된 파일 저장 시도
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            // 디렉토리 존재 확인 및 생성
            if (!Files.exists(uploadPath)) {
                log.info("업로드 디렉토리가 존재하지 않아 생성 시도: {}", uploadPath);
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성 (UUID 사용) - 원본 파일명에서 확장자를 가져오지 않음
            // 원본 파일명을 그대로 사용하거나 다른 방식 사용 가능
            // String uniqueFileName = UUID.randomUUID().toString();
            // 필요하다면 원본 파일명을 포함: uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
            // Path filePath = uploadPath.resolve(uniqueFileName); // 저장할 전체 경로
            Path filePath = uploadPath.resolve(originalFilename);


            log.info("파일 저장 시도: {}", filePath);

            // 파일 저장 실행
            file.transferTo(filePath);

            log.info("파일 저장 성공: {}", filePath);

            // 3. 파일 저장 성공 메시지 설정
            //redirectAttributes.addFlashAttribute("message",
            //        "파일이 성공적으로 업로드되어 서버에 저장되었습니다: " + originalFilename + " (저장명: " + uniqueFileName + ")");
            redirectAttributes.addFlashAttribute("message",
                    "파일이 성공적으로 업로드되어 서버에 저장되었습니다: " + originalFilename + " (저장명: " + originalFilename + ")");

            // --- 여기 이후에 저장된 파일 경로(filePath)를 가지고 다음 로직 진행 ---

        } catch (IOException e) {
            // 4. 파일 저장 중 오류 발생 시
            log.error("파일 업로드 및 저장 중 오류 발생: {}", originalFilename, e);
            redirectAttributes.addFlashAttribute("error",
                    "파일 업로드 중 오류가 발생했습니다. 서버 로그를 확인해주세요.");
        } catch (Exception e) {
            // 5. 기타 예상치 못한 오류 처리
            log.error("파일 처리 중 예상치 못한 오류 발생: {}", originalFilename, e);
            redirectAttributes.addFlashAttribute("error",
                    "파일 처리 중 예상치 못한 오류가 발생했습니다.");
        }

        // 처리가 끝나면 다시 홈 페이지("/")로 리다이렉트
        return "redirect:/";
    }
}
