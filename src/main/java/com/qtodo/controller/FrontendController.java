package com.qtodo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

	 @GetMapping(value = {"/ang", "/ang/"})
	 public String forward() {
	    return "forward:/ang/index.html";
	 }
}
