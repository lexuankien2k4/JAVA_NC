package com.BTLJAVA.WebBanThucPhamKho.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ResponseData<T> {
    private Integer status;
    private String message;
    private T data;
    private String error;
}
