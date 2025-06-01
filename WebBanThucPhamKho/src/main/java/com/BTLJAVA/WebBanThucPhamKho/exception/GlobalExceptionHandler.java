 package com.BTLJAVA.WebBanThucPhamKho.exception; // Hoặc package của bạn

 import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
 import jakarta.persistence.EntityNotFoundException;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.annotation.ControllerAdvice;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.context.request.WebRequest;
 import java.util.stream.Collectors;

 @ControllerAdvice
 @Slf4j
 public class GlobalExceptionHandler {

     @ExceptionHandler(EntityNotFoundException.class)
     public ResponseEntity<ResponseData<Object>> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
         log.warn("API Error - EntityNotFoundException: {} on path {}", ex.getMessage(), request.getDescription(false));
         ResponseData<Object> responseData = ResponseData.builder()
                 .status(HttpStatus.NOT_FOUND.value())
                 .message(ex.getMessage())
                 .error(ex.getClass().getSimpleName())
                 .build();
         return new ResponseEntity<>(responseData, HttpStatus.NOT_FOUND);
     }

     @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
     public ResponseEntity<ResponseData<Object>> handleIllegalArgumentOrStateException(RuntimeException ex, WebRequest request) {
         log.warn("API Error - Business Rule Violation: {} on path {}", ex.getMessage(), request.getDescription(false));
         ResponseData<Object> responseData = ResponseData.builder()
                 .status(HttpStatus.BAD_REQUEST.value())
                 .message(ex.getMessage())
                 .error(ex.getClass().getSimpleName())
                 .build();
         return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
     }

     @ExceptionHandler(AccessDeniedException.class)
     public ResponseEntity<ResponseData<Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
         log.warn("API Error - AccessDeniedException: {} on path {}", ex.getMessage(), request.getDescription(false));
         ResponseData<Object> responseData = ResponseData.builder()
                 .status(HttpStatus.FORBIDDEN.value())
                 .message("Bạn không có quyền thực hiện hành động này.")
                 .error(ex.getClass().getSimpleName())
                 .build();
         return new ResponseEntity<>(responseData, HttpStatus.FORBIDDEN);
     }


     @ExceptionHandler(MethodArgumentNotValidException.class)
     public ResponseEntity<ResponseData<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
         log.warn("API Error - Validation Exception: {} on path {}", ex.getMessage(), request.getDescription(false));
         String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                 .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                 .collect(Collectors.joining("; "));
         ResponseData<Object> responseData = ResponseData.builder()
                 .status(HttpStatus.BAD_REQUEST.value())
                 .message("Dữ liệu không hợp lệ: " + errorMessage)
                 .error("ValidationFailure")
                 .build();
         return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
     }

     @ExceptionHandler(Exception.class) // Handler chung nhất cho các lỗi không mong muốn khác
     public ResponseEntity<ResponseData<Object>> handleAllUncaughtException(Exception ex, WebRequest request) {
         log.error("API Error - Uncaught Exception: {} on path {}", ex.getMessage(), request.getDescription(false), ex);
         ResponseData<Object> responseData = ResponseData.builder()
                 .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                 .message("Đã có lỗi không mong muốn xảy ra ở phía server.")
                 .error(ex.getClass().getSimpleName())
                 .build();
         return new ResponseEntity<>(responseData, HttpStatus.INTERNAL_SERVER_ERROR);
     }
 }