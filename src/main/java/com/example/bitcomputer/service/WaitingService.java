package com.example.bitcomputer.service;

import com.example.bitcomputer.jwt.TokenInfo;
import com.example.bitcomputer.model.WaitingDTO;

public interface WaitingService {
    TokenInfo registerWaiting(WaitingDTO waitingDTO);
}
