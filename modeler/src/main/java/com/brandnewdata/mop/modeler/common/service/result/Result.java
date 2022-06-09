package com.brandnewdata.mop.modeler.common.service.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口返回数据格式
 * @author caiwillie
 */
@Data
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 成功标志
	 */
	private boolean success = true;

	/**
	 * 返回处理消息
	 */
	private String message = "操作成功！";

	/**
	 * 返回代码
	 */
	private Integer code = 0;
	
	/**
	 * 返回数据对象 data
	 */
	private T result;
	
	/**
	 * 时间戳
	 */
	private long timestamp = System.currentTimeMillis();

	public Result() {
		
	}
	

	public static<T> Result<T> success() {
		Result<T> ret = new Result<T>();
		ret.setSuccess(true);
		ret.message = ResultCode.SUCCESS.message;
		ret.code = ResultCode.SUCCESS.code;
		return ret;
	}

	public static<T> Result<T> success(T data) {
		Result<T> ret = new Result<T>();
		ret.setSuccess(true);
		ret.code = ResultCode.SUCCESS.code;
		ret.result = data;
		ret.message = ResultCode.SUCCESS.message;
		return ret;
	}

	public static<T> Result<T> success(T data, String message) {
		Result<T> ret = new Result<T>();
		ret.setSuccess(true);
		ret.code = ResultCode.SUCCESS.code;
		ret.result = data;
		ret.message = message;
		return ret;
	}

	public static <T> Result<T> error() {
		Result<T> ret = new Result<>();
		ret.setSuccess(false);
		ret.code = ResultCode.INTERNAL_SERVER_ERROR.code;
		ret.message = ResultCode.INTERNAL_SERVER_ERROR.message;
		return ret;
	}
}