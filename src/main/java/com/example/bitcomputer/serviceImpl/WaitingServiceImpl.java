package com.example.bitcomputer.serviceImpl;

import com.example.bitcomputer.Repository.WaitingRepository;
import com.example.bitcomputer.entity.Waiting;
import com.example.bitcomputer.jwt.JwtTokenProvider;
import com.example.bitcomputer.jwt.TokenInfo;
import com.example.bitcomputer.model.WaitingDTO;
import com.example.bitcomputer.service.WaitingService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        waiting.setDeptId(waitingDTO.getDeptId() > 0 ? waitingDTO.getDeptId() : 1); // 기본값 1으로 설정
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

    @Override
    public List<WaitingDTO> getWaitingList() {
        List<Waiting> waitingList = waitingRepository.findAll();

        return waitingList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TokenInfo updateWaitingState(int patientId) {
        // 상태에 관계없이 최신 대기 정보를 찾음 (completed가 아닌 경우)
        Optional<Waiting> optionalWaiting = waitingRepository.findFirstByPatientIdOrderByIdDesc(patientId);

        if (optionalWaiting.isEmpty()) {
            throw new IllegalArgumentException("해당 환자의 대기 정보를 찾을 수 없습니다.");
        }

        Waiting waiting = optionalWaiting.get();

        // 이미 완료된 경우는 변경하지 않음
        if ("completed".equals(waiting.getState())) {
            throw new IllegalArgumentException("이미 완료된 진료입니다.");
        }

        waiting.setState("completed");

        Waiting updatedWaiting = waitingRepository.save(waiting);

        // JWT 토큰 생성
        String patientIdStr = String.valueOf(updatedWaiting.getPatientId());
        String accessToken = jwtTokenProvider.generateAccessToken(patientIdStr);
        String refreshToken = jwtTokenProvider.generateRefreshToken(patientIdStr);

        return new TokenInfo("Bearer", accessToken, refreshToken);

    }

    @Override
    @Transactional
    public TokenInfo updateWaitingStateToHold(int patientId) {
        // 상태에 관계없이 최신 대기 정보를 찾음 (completed가 아닌 경우)
        Optional<Waiting> optionalWaiting = waitingRepository.findFirstByPatientIdOrderByIdDesc(patientId);

        if (optionalWaiting.isEmpty()) {
            throw new IllegalArgumentException("해당 환자의 대기 정보를 찾을 수 없습니다.");
        }

        Waiting waiting = optionalWaiting.get();

        // 이미 완료된 경우는 변경하지 않음
        if ("completed".equals(waiting.getState())) {
            throw new IllegalArgumentException("이미 완료된 진료는 보류로 변경할 수 없습니다.");
        }

        waiting.setState("hold");

        Waiting updatedWaiting = waitingRepository.save(waiting);

        // JWT 토큰 생성
        String patientIdStr = String.valueOf(updatedWaiting.getPatientId());
        String accessToken = jwtTokenProvider.generateAccessToken(patientIdStr);
        String refreshToken = jwtTokenProvider.generateRefreshToken(patientIdStr);

        return new TokenInfo("Bearer", accessToken, refreshToken);
    }

    private WaitingDTO convertToDTO(Waiting waiting) {
        WaitingDTO dto = new WaitingDTO();
        dto.setId(waiting.getId());
        dto.setPatientId(waiting.getPatientId());
        dto.setDeptId(waiting.getDeptId());
        dto.setSymptom(waiting.getSymptom());
        dto.setEntryDate(waiting.getEntryDate());
        dto.setState(waiting.getState());
        return dto;
    }


}
