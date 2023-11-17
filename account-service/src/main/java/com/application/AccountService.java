package com.application;

import AccountServiceGrpcLib.AccountServiceGrpc;
import AccountServiceGrpcLib.CheckUserExistRequest;
import AccountServiceGrpcLib.CheckUserExistResponse;
import com.application.user.UserService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class AccountService extends AccountServiceGrpc.AccountServiceImplBase {

    private final UserService userService;

    public AccountService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void checkUserExist(CheckUserExistRequest request, StreamObserver<CheckUserExistResponse> responseObserver){
        log.info("🟢 gRPC account service checkUserExist: {}", request.getUsername());

        boolean exist = userService.checkUserExist(request.getUsername());

        CheckUserExistResponse response = CheckUserExistResponse.newBuilder().setExist(exist).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
