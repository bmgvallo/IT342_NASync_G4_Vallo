package com.citu.nasync_backend.chain;

import com.citu.nasync_backend.dto.request.RegisterUserRequest;
import com.citu.nasync_backend.entity.Branch;
import com.citu.nasync_backend.repository.BranchRepository;

public class BranchResolutionHandler extends UserRegistrationHandler {

    private final BranchRepository branchRepository;

    public BranchResolutionHandler(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public void handle(RegisterUserRequest request, RegistrationContext context) {
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));
            context.setBranch(branch);
        }
        if (next != null) next.handle(request, context);
    }
}