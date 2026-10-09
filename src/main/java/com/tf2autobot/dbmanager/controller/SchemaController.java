package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.service.SchemaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/schema")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping("/sku-names")
    public Map<String, String> skuNames(@RequestParam List<String> skus) {
        return schemaService.resolveNames(skus);
    }
}
