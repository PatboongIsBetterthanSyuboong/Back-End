package com.example.bitcomputer.controller;

import com.example.bitcomputer.jwt.TokenInfo;
import com.example.bitcomputer.model.WaitingDTO;
import com.example.bitcomputer.service.WaitingService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenInfo> registerWaiting(@RequestBody WaitingDTO waitingDTO) {
        try {
            // 필수 필드 검증
            if (waitingDTO.getPatientId() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // 접수 등록
            TokenInfo tokenInfo = waitingService.registerWaiting(waitingDTO);

            if (tokenInfo != null) {
                return ResponseEntity.ok(tokenInfo);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
