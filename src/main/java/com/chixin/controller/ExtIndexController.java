package com.chixin.controller;

import com.chixin.note.CxControl;
import com.chixin.note.CxRequestMapping;
import com.chixin.note.CxResponseBody;

import java.util.Arrays;

@CxControl
@CxRequestMapping("/chixin")
public class ExtIndexController {
	/*跳转到页面*/
	@CxRequestMapping("/yemian")
	public String test() {
		return "yemian";
	}
	/*返回json*/
	@CxRequestMapping("/json")
	@CxResponseBody
	public Object json() {
		return Arrays.asList("json String");
	}

}
