package com.workintech.s17d2.rest;

import com.workintech.s17d2.model.*;
import com.workintech.s17d2.tax.Taxable;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    public Map<Integer, Developer> developers;
    private final Taxable taxable;

    public DeveloperController(Taxable taxable) {
        this.taxable = taxable;
    }

    @PostConstruct
    public void init() {
        developers = new HashMap<>();
    }

    @GetMapping
    public List<Developer> getAllDevelopers() {
        return new ArrayList<>(developers.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Developer> getDeveloperById(@PathVariable int id) {
        Developer developer = developers.get(id);
        return developer != null ? ResponseEntity.ok(developer) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Developer> addDeveloper(@RequestBody Developer developer) {
        double netSalary = developer.getSalary();
        switch (developer.getExperience()) {
            case JUNIOR -> netSalary -= (netSalary * taxable.getSimpleTaxRate()) / 100;
            case MID -> netSalary -= (netSalary * taxable.getMiddleTaxRate()) / 100;
            case SENIOR -> netSalary -= (netSalary * taxable.getUpperTaxRate()) / 100;
        }

        Developer dev;
        if (developer.getExperience() == Experience.JUNIOR) {
            dev = new JuniorDeveloper(developer.getId(), developer.getName(), netSalary);
        } else if (developer.getExperience() == Experience.MID) {
            dev = new MidDeveloper(developer.getId(), developer.getName(), netSalary);
        } else {
            dev = new SeniorDeveloper(developer.getId(), developer.getName(), netSalary);
        }

        developers.put(dev.getId(), dev);
        return new ResponseEntity<>(dev, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Developer> updateDeveloper(@PathVariable int id, @RequestBody Developer updated) {
        if (!developers.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }
        developers.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable int id) {
        developers.remove(id);
        return ResponseEntity.ok().build();
    }
}
