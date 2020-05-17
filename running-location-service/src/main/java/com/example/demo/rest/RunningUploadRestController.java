package com.example.demo.rest;

import com.example.demo.model.Location;
import com.example.demo.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class RunningUploadRestController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@RequestBody List<Location> locations) {
        locationService.saveRunningLocations(locations);
    }

    @PostMapping("/purge")
    public void purge() {
        locationService.deleteAll();
    }

    @GetMapping("/running/{movementType}")
    public Page<Location> findByMovementType(@PathVariable String movementType,
                                             @RequestParam(name = "page") int page,
                                             @RequestParam(name = "size") int size) {
        return locationService.findByRunnerMovementType(movementType, PageRequest.of(page, size));
    }

    @GetMapping("/running/runningId/{runningId}")
    public Page<Location> findByRunningId(@PathVariable String runningId,
                                          @RequestParam(name = "page") int page,
                                          @RequestParam(name = "size") int size) {
        return locationService.findByRunningId(runningId, PageRequest.of(page, size));
    }
}
