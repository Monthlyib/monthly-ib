package com.monthlyib.server.advice;

import com.monthlyib.server.constant.ErrorCode;
import com.monthlyib.server.dto.ErrorResponse;
import com.monthlyib.server.exception.ServiceLogicException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse methodArgumentNotValidHandle(
            MethodArgumentNotValidException e
    ) {
        return ErrorResponse.of(e.getBindingResult());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse methodArgumentTypeMismatchHandle(
            MethodArgumentTypeMismatchException e
    ) {
        return ErrorResponse.of(ErrorCode.ARGUMENT_MISMATCH_BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse httpMediaTypeNotSupportedExceptionHandle(
            HttpMediaTypeNotSupportedException e
    ) {
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse constraintViolationExceptionHandler(
            ConstraintViolationException e
    ) {
        return ErrorResponse.of(e.getConstraintViolations());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse httpRequestMethodNotSupportedExceptionHandler(
            HttpRequestMethodNotSupportedException e
    ) {
        return ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse httpMessageNotReadableExceptionHandler(
            HttpMessageNotReadableException e
    ) {
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse illegalArgumentExceptionHandler(
            IllegalArgumentException e
    ) {
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse missingServletRequestParameterExceptionHandler(
            MissingServletRequestParameterException e
    ) {
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse exceptionHandler(Exception e) {
//        WebHookDto dto = getServerErrorWebHookDto(e);
//        webHookService.callEvent(dto);
        e.printStackTrace();
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity serviceLogicExceptionHandler(
            ServiceLogicException e
    ) {
        if (e.getErrorCode().getStatus() == 500) {
//            WebHookDto dto = getServerErrorWebHookDto(e.getErrorCode());
//            webHookService.callEvent(dto);
            e.printStackTrace();
        }
        return new ResponseEntity<>(
                ErrorResponse.of(e.getErrorCode(), e.getMessage()),
                HttpStatus.valueOf(e.getErrorCode().getStatus()));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    protected ResponseEntity<ErrorResponse> handleMultipartException(MaxUploadSizeExceededException e) {
        log.error("handleMaxUploadSizeExceededException", e);
        final ErrorResponse response = ErrorResponse.of(ErrorCode.MAX_FILE_SIZE_EXCEEDED);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

//    private static WebHookDto getServerErrorWebHookDto(ErrorCode ec) {
//        String name = ec.name();
//        String message = ec.getMessage();
//        int status = ec.getStatus();
//        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
//        String format = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        return new WebHookDto(" 📢  " +
//                status + " ❗   " +
//                message +'\n' +"⏱️  KR Time: "+
//                format
//        );
//    }

//    private static WebHookDto getServerErrorWebHookDto(Exception e) {
//        String name = e.getClass().getSimpleName();
//        String message = e.getMessage();
//        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
//        String format = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        return new WebHookDto(" 📢   500  " +
//                name + '\n' +" ❗Message:  " +
//                message +'\n' +"⏱️  KR Time: "+
//                format
//        );
//    }
}
