package com.example.aigateway.controller;

import com.example.aigateway.model.RunbookSearchResult;
import com.example.aigateway.service.RunbookSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools")
public class RunbookToolController {

  private final RunbookSearchService runbookSearchService;

  public RunbookToolController(RunbookSearchService runbookSearchService) {
    this.runbookSearchService = runbookSearchService;
  }

  @GetMapping("/runbooks/search")
  public RunbookSearchResult search(@RequestParam("query") String query) {
    return runbookSearchService.search(query);
  }
}
