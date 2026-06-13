package com.bank.controller;

import com.bank.dto.TransferRequest;
import com.bank.dto.TransferResponse;
import com.bank.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request,
                                                     Authentication auth) {
        return ResponseEntity.ok(transferService.transfer(request, auth.getName()));
    }

    @PostMapping("/{transferGroupId}/cancel")
    public ResponseEntity<TransferResponse> cancel(
    		@PathVariable("transferGroupId") String transferGroupId,
                          Authentication auth) {
        return ResponseEntity.ok(transferService.cancelTransfer(transferGroupId, auth.getName()));
    }
}