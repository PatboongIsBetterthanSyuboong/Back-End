package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.WaitingRepository;
import com.example.bitcomputer.entity.Waiting;
import com.example.bitcomputer.jwt.JwtTokenProvider;
import com.example.bitcomputer.jwt.TokenInfo;
import com.example.bitcomputer.model.WaitingDTO;
import com.example.bitcomputer.service.WaitingService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class WaitingServiceImpl implements WaitingService {
    private final WaitingRepository waitingRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public WaitingServiceImpl(WaitingRepository waitingRepository, JwtTokenProvider jwtTokenProvider) {
        this.waitingRepository = waitingRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public TokenInfo registerWaiting(WaitingDTO waitingDTO) {
        Waiting waiting = new Waiting();
        waiting.setPatientId(waitingDTO.getPatientId());

        waiting.setSymptom(waitingDTO.getSymptom());
        waiting.setEntryDate(LocalDateTime.now());
        waiting.setState(waitingDTO.getState() != null ? waitingDTO.getState() : "waiting");

        // 저장
        Waiting savedWaiting = waitingRepository.save(waiting);

        // JWT 토큰 생성
        String patientIdStr = String.valueOf(savedWaiting.getPatientId());
        String accessToken = jwtTokenProvider.generateAccessToken(patientIdStr);
        String refreshToken = jwtTokenProvider.generateRefreshToken(patientIdStr);

        return new TokenInfo("Bearer", accessToken, refreshToken);

    }
}
